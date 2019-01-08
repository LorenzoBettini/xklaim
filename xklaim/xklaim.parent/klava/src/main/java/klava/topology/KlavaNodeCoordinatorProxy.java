/*
 * Created on Jan 12, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;

import org.mikado.imc.topology.NodeCoordinatorProxy;


/**
 * A specialized proxy for coordinators
 * 
 * @author Lorenzo Bettini
 */
public class KlavaNodeCoordinatorProxy extends NodeCoordinatorProxy {
    /**
     * The klava node to delegate operations to.
     */
    protected KlavaNode klavaNode;
    
    /**
     * @param node
     */
    public KlavaNodeCoordinatorProxy(KlavaNode node) {
        super(node);
        klavaNode = node;
    }

    /**
     * @throws KlavaException 
     * @see klava.topology.KlavaNode#accept(klava.PhysicalLocality,
     *      klava.PhysicalLocality)
     */
    public boolean accept(PhysicalLocality local, PhysicalLocality remote) throws KlavaException {
        return klavaNode.accept(local, remote);
    }

    /**
     * @throws KlavaException 
     * @see klava.topology.KlavaNode#accept(klava.PhysicalLocality)
     */
    public boolean accept(PhysicalLocality remote)
            throws KlavaException {
        return klavaNode.accept(remote);
    }

    /**
     * @see klava.topology.KlavaNode#disconnected(klava.PhysicalLocality,
     *      klava.LogicalLocality)
     */
    public void disconnected(PhysicalLocality physicalLocality,
            LogicalLocality logicalLocality) throws KlavaException {
        klavaNode.disconnected(physicalLocality, logicalLocality);
    }

    /**
     * @see klava.topology.KlavaNode#disconnected(klava.PhysicalLocality)
     */
    public void disconnected(PhysicalLocality physicalLocality)
            throws KlavaException {
        klavaNode.disconnected(physicalLocality);
    }

    /**
     * @see klava.topology.KlavaNode#login(klava.Locality)
     */
    public boolean login(Locality remote) throws KlavaException {
        return klavaNode.login(remote);
    }

    /**
     * @see klava.topology.KlavaNode#logout(klava.Locality)
     */
    public boolean logout(Locality remote) throws KlavaException {
        return klavaNode.logout(remote);
    }

    /**
     * @throws KlavaException 
     * @see klava.topology.KlavaNode#register(klava.PhysicalLocality,
     *      klava.LogicalLocality)
     */
    public boolean register(PhysicalLocality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNode.register(remote, logical);
    }

    /**
     * @throws KlavaException 
     * @see klava.topology.KlavaNode#register(klava.PhysicalLocality,
     *      klava.PhysicalLocality, klava.LogicalLocality)
     */
    public boolean register(PhysicalLocality local, PhysicalLocality remote,
            LogicalLocality logical) throws KlavaException {
        return klavaNode.register(local, remote, logical);
    }

    /**
     * @see klava.topology.KlavaNode#subscribe(klava.Locality,
     *      klava.LogicalLocality)
     */
    public boolean subscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNode.subscribe(remote, logical);
    }

    /**
     * @see klava.topology.KlavaNode#unsubscribe(klava.Locality,
     *      klava.LogicalLocality)
     */
    public boolean unsubscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNode.unsubscribe(remote, logical);
    }

    /**
     * @see klava.topology.KlavaNode#newloc()
     */
    public PhysicalLocality newloc() throws KlavaException {
        return klavaNode.newloc();
    }

    /**
     * @see klava.topology.KlavaNode#eval(klava.topology.KlavaNodeCoordinator)
     */
    public void eval(KlavaNodeCoordinator klavaNodeCoordinator) throws KlavaException {
        klavaNode.eval(klavaNodeCoordinator);
    }

    /**
     * @see klava.topology.KlavaNode#closeSessions(klava.PhysicalLocality)
     */
    public void closeSessions(PhysicalLocality physicalLocality) {
        klavaNode.closeSessions(physicalLocality);
    }

    /**
     *@see klava.topology.KlavaNode#addToEnvironment(LogicalLocality, PhysicalLocality)
     */
    public boolean addToEnvironment(LogicalLocality logicalLocality, PhysicalLocality physicalLocality) {
        return klavaNode.addToEnvironment(logicalLocality, physicalLocality);
    }
}
