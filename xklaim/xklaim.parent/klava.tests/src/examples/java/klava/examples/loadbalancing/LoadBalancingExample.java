/*
 * Created on Apr 3, 2006
 */
package klava.examples.loadbalancing;

import org.mikado.imc.common.IMCException;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;

/**
 * Creates a load balancing system net and spawns many processes
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LoadBalancingExample {

    /**
     * @param args
     * @throws IMCException
     * @throws KlavaException
     * @throws KlavaMalformedPhyLocalityException
     */
    public static void main(String[] args)
            throws KlavaMalformedPhyLocalityException, KlavaException,
            IMCException {
        PhysicalLocality loadBancingServerLoc = new PhysicalLocality(
                "tcp-127.0.0.1:9999");
        LoadBalancingNode loadBalancingNode = new LoadBalancingNode(
                "load balancing node", loadBancingServerLoc, null);

        new ProcessorNode("processor 1", loadBancingServerLoc, 2);
        new ProcessorNode("processor 2", loadBancingServerLoc, 2);
        new ProcessorNode("processor 3", loadBancingServerLoc, 3);
        new ProcessorNode("processor 4", loadBancingServerLoc, 1);
                
        /* now spawn some processes on the load balancing system */
        for (int i = 0; i < 30; ++i)
            loadBalancingNode.out(new Tuple(new BusyLoopProcess()));
    }
}
