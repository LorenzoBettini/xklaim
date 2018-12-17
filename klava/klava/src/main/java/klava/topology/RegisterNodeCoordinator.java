/*
 * Created on Jan 23, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;

/**
 * A coordinator that performs a register (possibily in a loop).
 * 
 * It is derived from AcceptNodeCoordinator only for code reuse.
 * 
 * @author Lorenzo Bettini
 */
public class RegisterNodeCoordinator extends AcceptNodeCoordinator {
    /**
     * @param physicalLocality
     *            The locality to accept connections.
     */
    public RegisterNodeCoordinator(PhysicalLocality physicalLocality) {
        super(physicalLocality);
    }

    /**
     * @param physicalLocality
     *            The locality to accept connections.
     * @param name
     */
    public RegisterNodeCoordinator(PhysicalLocality physicalLocality,
            String name) {
        super(physicalLocality, name);
    }

    /**
     * @param name
     */
    public RegisterNodeCoordinator(String name) {
        super(name);
    }

    /**
     * 
     */
    public RegisterNodeCoordinator() {
        super();
    }

    /**
     * @see klava.topology.AcceptNodeCoordinator#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        while (true) {
            PhysicalLocality remote = new PhysicalLocality();
            LogicalLocality logicalLocality = new LogicalLocality();
            boolean success = false;

            if (physicalLocality == null) {
                /* use the node's main locality */
                success = register(remote, logicalLocality);
            } else {
                success = register(physicalLocality, remote, logicalLocality);
            }

            if (success) {
                success(remote, logicalLocality);
            }

            if (!loop)
                break;
        }
    }

    /**
     * Called if the reister succeeded. Default is empty, subclasses should
     * specialize this method.
     * 
     * @param remote
     *            The accepted locality
     * @param logicalLocality
     *            The accepted logical locality
     * @throws KlavaException
     */
    protected void success(PhysicalLocality remote,
            LogicalLocality logicalLocality) throws KlavaException {
    }
}
