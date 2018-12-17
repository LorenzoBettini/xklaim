/*
 * Created on Mar 31, 2006
 */
package klava.examples.loadbalancing;

import klava.KInteger;
import klava.KString;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.examples.gui.NodeWithScreen;
import klava.topology.KlavaProcess;
import klava.topology.KlavaProcessVar;

/**
 * A node representing a Processor of the load balancing system
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ProcessorNode extends NodeWithScreen {
    /**
     * Waits for a process to execute (and send a credit back)
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.3 $
     */
    public class Executor extends KlavaProcess {

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
                /* wait for a process... */
                in(new Tuple(klavaProcessVar), self);
                out(new Tuple("executing "
                        + klavaProcessVar.klavaProcess.getName() + "...\n"),
                        screenLoc);
                /* execute it locally */
                eval(klavaProcessVar.klavaProcess, self);
                /* send a credit back */
                out(new Tuple("sending credit back...\n"), screenLoc);
                out(creditTuple, loadBalancingServerLoc);
            }
        }

    }

    public final static KString creditString = new KString("CREDIT");

    /**
     * The tuple to send a credit back
     */
    public Tuple creditTuple;

    /**
     * The locality of the load balancing server
     */
    Locality loadBalancingServerLoc;

    /**
     * The logical locality with which this node is known to the load balancing
     * server
     */
    LogicalLocality myLogicalLocality;

    /**
     * @param nodeName
     * @param serverLoc
     *            The locality of the main load balancing server
     * @param initialCredits
     *            The number of credits to send at the beginning
     * @throws KlavaException
     */
    public ProcessorNode(String nodeName, Locality serverLoc, int initialCredits)
            throws KlavaException {
        super(nodeName);
        myLogicalLocality = new LogicalLocality(nodeName);
        subscribe(serverLoc, myLogicalLocality);
        loadBalancingServerLoc = serverLoc;
        creditTuple = new Tuple(creditString, myLogicalLocality);

        /* send the initial credits */
        out(new Tuple(myLogicalLocality, new KInteger(initialCredits)),
                serverLoc);

        out(new Tuple("registered at " + serverLoc + " as " + nodeName + "\n"),
                screenLoc);

        /* start the executor process */
        eval(new Executor(), self);
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws KlavaMalformedPhyLocalityException
     */
    public static void main(String[] args)
            throws KlavaMalformedPhyLocalityException, KlavaException {
        try {
            new ProcessorNode(args[0], new PhysicalLocality(args[1]), Integer
                    .parseInt(args[2]));
        } catch (IndexOutOfBoundsException e) {
            System.err
                    .println("syntax: processor_loc server_loc initial_credits");
            System.exit(1);
        }
    }

}
