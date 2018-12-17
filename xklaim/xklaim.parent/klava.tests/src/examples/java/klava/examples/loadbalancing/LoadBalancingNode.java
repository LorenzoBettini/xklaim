/*
 * Created on Mar 31, 2006
 */
package klava.examples.loadbalancing;

import org.mikado.imc.common.IMCException;

import klava.KInteger;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;
import klava.examples.gui.NodeWithScreen;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaProcess;
import klava.topology.KlavaProcessVar;

/**
 * The node performing the load balancing
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class LoadBalancingNode extends NodeWithScreen {
    /**
     * The NodeCoordinator that registers processors and initializes their
     * credits
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class RegisterProcessors extends KlavaNodeCoordinator {
        /** Where we wait for subscription requests */
        PhysicalLocality registerLocality;

        /**
         * @param physicalLocality
         */
        public RegisterProcessors(PhysicalLocality registerLocality) {
            this.registerLocality = registerLocality;
        }

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                PhysicalLocality processorPhysicalLocality = new PhysicalLocality();
                LogicalLocality processorLogicalLocality = new LogicalLocality();
                register(registerLocality, processorPhysicalLocality,
                        processorLogicalLocality);

                /* wait for the initial credits, within 3 seconds */
                KInteger initialCredits = new KInteger();
                if (!in_t(new Tuple(processorLogicalLocality, initialCredits),
                        self, 3000)) {
                    SystemErrPrint("credits not received from "
                            + processorPhysicalLocality + "("
                            + processorLogicalLocality + ")\n");
                    return;
                }

                try {
                    credits.in(lockTuple);
                    out(new Tuple("initial credits from "
                            + processorLogicalLocality + ": " + initialCredits
                            + "\n"), screenLoc);
                    credits.out(new Tuple(processorLogicalLocality,
                            initialCredits));
                    credits.out(lockTuple);
                } catch (InterruptedException e) {
                    throw new KlavaException(e);
                }
            }
        }

    }

    /**
     * Waits for a credit from a processor node and updates the priority queue.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class CreditReceiver extends KlavaProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                LogicalLocality processorLocality = new LogicalLocality();
                in(new Tuple(ProcessorNode.creditString, processorLocality),
                        self);

                try {
                    /* lock the database for updating */
                    credits.in(lockTuple);

                    KInteger currentCredits = new KInteger(); // formal
                    if (credits.in_nb(new Tuple(processorLocality,
                            currentCredits))) {
                        /* update the number of credits */
                        credits.out(new Tuple(processorLocality, new KInteger(
                                currentCredits.intValue() + 1)));
                        out(new Tuple("updated credits for "
                                + processorLocality + "\n"), screenLoc);
                    } else {
                        /* better error handling */
                        out(new Tuple("unknown processor " + processorLocality
                                + "\n"), screenLoc);
                    }

                    /* release the database */
                    credits.out(lockTuple);
                } catch (InterruptedException e) {
                    throw new KlavaException(e);
                }

            }
        }

    }

    /**
     * Wait for a process from clients and executes it at the processor with the
     * highest credit number (and updates the credits of that processor)
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class AcceptProcesses extends KlavaProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                KlavaProcessVar klavaProcessVar = new KlavaProcessVar();
                in(new Tuple(klavaProcessVar), self);
                out(new Tuple("received process "
                        + klavaProcessVar.klavaProcess.getName() + "\n"),
                        screenLoc);
                try {
                    /* lock the database */
                    credits.in(lockTuple);
                    LogicalLocality processorLoc = new LogicalLocality(); // formal
                    KInteger creditNum = new KInteger(); // formal
                    LogicalLocality idlestProcessor = null;
                    int maxCredits = 0;
                    Tuple creditTuple = new Tuple(processorLoc, creditNum);
                    while (credits.read_nb(creditTuple)) {
                        if (maxCredits < creditNum.intValue()) {
                            /* found another candidate */
                            maxCredits = creditNum.intValue();
                            idlestProcessor = new LogicalLocality(processorLoc);
                        }
                        /* reset the template for iteration */
                        creditTuple.resetOriginalTemplate();
                    }
                    /* if we found an idle processor */
                    if (idlestProcessor != null) {
                        out(new Tuple("executing process "
                                + klavaProcessVar.klavaProcess.getName()
                                + " at " + idlestProcessor + "\n"), screenLoc);
                        out(new Tuple(klavaProcessVar.klavaProcess),
                                idlestProcessor);
                    } else {
                        /* execute it locally */
                        out(new Tuple("executing process "
                                + klavaProcessVar.klavaProcess.getName()
                                + " locally\n"), screenLoc);
                        eval(klavaProcessVar.klavaProcess, self);
                    }
                    /* update the credits of the processor in the database */
                    credits.in(new Tuple(idlestProcessor, new KInteger(
                            maxCredits)));
                    credits.out(new Tuple(idlestProcessor, new KInteger(
                            maxCredits - 1)));
                    /* release lock on the database */
                    credits.out(lockTuple);
                } catch (InterruptedException e) {
                    throw new KlavaException(e);
                }
            }

        }

    }

    /**
     * This will store the associations (tuples) of the shape (processor
     * locality, num of credits). An additional tuple, ("LOCK") will be used to
     * guarantee synchronization when iterating over this tuple space.
     */
    TupleSpace credits = new TupleSpaceVector();

    /**
     * The tuple used to lock the credits database
     */
    Tuple lockTuple = new Tuple("LOCK");

    /**
     * @param nodeName
     * @param acceptLoc
     *            The physical locality where we accept connections from
     *            ProcessorNodes
     * @param serverLoc
     *            The locality of the server to subscribe to
     * @throws KlavaException
     * @throws IMCException
     */
    public LoadBalancingNode(String nodeName, PhysicalLocality acceptLoc,
            Locality serverLoc) throws KlavaException, IMCException {
        super(nodeName);
        /* since we always use this very same tuple */
        lockTuple.setHandleRetrieved(false);
        credits.out(lockTuple); // initialize the credit database
        addNodeCoordinator(new RegisterProcessors(acceptLoc));
        eval(new AcceptProcesses());
        eval(new CreditReceiver());

        if (serverLoc != null)
            subscribe(serverLoc, new LogicalLocality(nodeName));
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws KlavaMalformedPhyLocalityException
     * @throws IMCException
     */
    public static void main(String[] args)
            throws KlavaMalformedPhyLocalityException, KlavaException,
            IMCException {
        try {
            new LoadBalancingNode(args[0], new PhysicalLocality(args[1]),
                    new PhysicalLocality(args[2]));
        } catch (IndexOutOfBoundsException e) {
            System.err
                    .println("syntax: name receiver_locality server_locality");
            System.exit(1);
        }
    }

}
