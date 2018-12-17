/*
 * Created on Jan 25, 2006
 */
package klava.topology;

import java.util.Enumeration;
import java.util.Vector;

import klava.KlavaException;
import klava.PhysicalLocality;

import org.mikado.imc.common.IMCException;

/**
 * A specialization of Net that performs register instead of accept.
 * 
 * This net is called logical since it uses logical localities.
 * 
 * @author Lorenzo Bettini
 */
public class LogicalNet extends Net {

    /**
     * @param localities
     *            The localities where we listen for incoming subscription
     *            request
     * @throws IMCException
     */
    public LogicalNet(Vector<PhysicalLocality> localities) throws IMCException {
        super(localities);
    }

    /**
     * @param loc
     *            The locality where we listen for incoming subscription request
     * @throws IMCException
     */
    public LogicalNet(PhysicalLocality loc) throws IMCException {
        super(loc);
    }

    /**
     * Starts the RegisterNodeCoordinator
     * 
     * @see klava.topology.Net#startAccept()
     */
    @Override
    protected void startAccept() throws IMCException {
        Enumeration<PhysicalLocality> locs = localities.elements();
        while (locs.hasMoreElements()) {
            RegisterNodeCoordinator registerNodeCoordinator = new RegisterNodeCoordinator(
                    locs.nextElement());
            registerNodeCoordinator.setLoop(true);
            addNodeCoordinator(registerNodeCoordinator);
        }
    }

    /**
     * @param args
     * @throws IMCException
     * @throws KlavaException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        Vector<PhysicalLocality> localities = new Vector<PhysicalLocality>();

        if (args.length == 0) {
            PhysicalLocality physicalLocality = new PhysicalLocality(
                    "localhost:9999");
            System.err.println("syntax: locality [localities...]");
            System.err.println("using default: " + physicalLocality);
            localities.addElement(physicalLocality);
        }

        for (int i = 0; i < args.length; ++i) {
            localities.addElement(new PhysicalLocality(args[i]));
        }

        new LogicalNet(localities);
    }

}
