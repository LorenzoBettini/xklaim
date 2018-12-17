/*
 * Created on Jan 24, 2006
 */
package klava.topology;

import java.util.Enumeration;
import java.util.Vector;

import klava.KlavaException;
import klava.PhysicalLocality;

import org.mikado.imc.common.IMCException;


/**
 * A specialized node acting as a KlavaNet (i.e., always listening
 * for incoming connection requests)
 * 
 * @author Lorenzo Bettini
 */
public class Net extends KlavaNode {
    /**
     * The localities where we listen for incoming accept request
     */
    protected Vector<PhysicalLocality> localities;

    /**
     * @param localities The localities where we listen for incoming accept request
     * @throws IMCException 
     */
    public Net(Vector<PhysicalLocality> localities) throws IMCException {
        this.localities = localities;
        startAccept();
    }
    
    /**
     * @param loc The locality where we listen for incoming accept request
     * @throws IMCException 
     */
    public Net(PhysicalLocality loc) throws IMCException {
        localities = new Vector<PhysicalLocality>();
        localities.addElement(loc);
        startAccept();
    }
    
    /**
     * Starts accepting connections
     * @throws IMCException 
     */
    protected void startAccept() throws IMCException {
        Enumeration<PhysicalLocality> locs = localities.elements();
        while (locs.hasMoreElements()) {
            AcceptNodeCoordinator acceptNodeCoordinator = new AcceptNodeCoordinator(locs.nextElement());
            acceptNodeCoordinator.setLoop(true);
            addNodeCoordinator(acceptNodeCoordinator);
        }
    }
    
    /**
     * @param args
     * @throws IMCException 
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        Vector<PhysicalLocality> localities = new Vector<PhysicalLocality>();        
        
        if (args.length == 0) {
            PhysicalLocality physicalLocality = new PhysicalLocality("localhost:9999");
            System.err.println("syntax: locality [localities...]");
            System.err.println("using default: " + physicalLocality);
            localities.addElement(physicalLocality);
        }

        for (int i = 0; i < args.length; ++i) {
            localities.addElement(new PhysicalLocality(args[i]));
        }
        
        new Net(localities);
    }

}
