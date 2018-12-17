/*
 * Created on Jan 23, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.PhysicalLocality;

/**
 * A coordinator that performs an accept (possibily in a loop)
 * 
 * @author Lorenzo Bettini
 */
public class AcceptNodeCoordinator extends KlavaNodeCoordinator {
    /**
     * The locality to accept connections. If null, uses the main locality of
     * the node.
     */
    protected PhysicalLocality physicalLocality = null;

    /**
     * The locality of the remote accepted node. Notice that each time the
     * accept is performed this will be overwritten, so if this coordinator is
     * used in loop mode, keep this in mind.
     */
    PhysicalLocality remote;

    /**
     * Continuously wait for incoming connections
     */
    boolean loop = false;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param physicalLocality
     *            The locality to accept connections.
     */
    public AcceptNodeCoordinator(PhysicalLocality physicalLocality) {
        this.physicalLocality = physicalLocality;
    }

    /**
     * @param physicalLocality
     *            The locality to accept connections.
     * @param name
     */
    public AcceptNodeCoordinator(PhysicalLocality physicalLocality, String name) {
        super(name);
        this.physicalLocality = physicalLocality;
    }

    /**
     * @param name
     */
    public AcceptNodeCoordinator(String name) {
        super(name);
    }

    /**
     * 
     */
    public AcceptNodeCoordinator() {
    }

    /**
     * @param physicalLocality
     * @param loop
     */
    public AcceptNodeCoordinator(PhysicalLocality physicalLocality, boolean loop) {
        this.physicalLocality = physicalLocality;
        this.loop = loop;
    }

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        while (true) {
            remote = new PhysicalLocality();
            boolean success = false;

            if (physicalLocality == null) {
                /* use the node's main locality */
                success = accept(remote);
            } else {
                success = accept(physicalLocality, remote);
            }

            if (success) {
                success(remote);
            }

            if (!loop)
                break;
        }

    }

    /**
     * Called if the accept succeeded. Default is empty, subclasses should
     * specialize this method.
     * 
     * @param remote
     *            The accepted locality
     * @throws KlavaException
     */
    protected void success(PhysicalLocality remote) throws KlavaException {

    }

    /**
     * @return Returns the loop.
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * @param loop
     *            The loop to set.
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * @return Returns the remote.
     */
    public PhysicalLocality getRemote() {
        return remote;
    }

}
