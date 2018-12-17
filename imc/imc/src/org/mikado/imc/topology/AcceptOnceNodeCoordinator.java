/*
 * Created on Jan 19, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;

/**
 * A node coordinator that accepts only one incoming session on a specific
 * SessionId.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class AcceptOnceNodeCoordinator extends NodeCoordinator {
    private Protocol protocol;

    private SessionId sessionId;

    /**
     * Creates a new AcceptNodeCoordinator object.
     * 
     * @param protocol
     *            The protocol that will be used for
     *            incoming connections.
     * @param sessionId
     *            The port to listen for incoming connections.
     */
    public AcceptOnceNodeCoordinator(Protocol protocol,
            SessionId sessionId) {
        this.protocol = protocol;
        this.sessionId = sessionId;
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinator#execute()
     */
    public void execute() throws IMCException {
        acceptAndStart(sessionId, protocol);
    }

    /**
     * Stops listening for incoming connections.
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        protocol.close();
    }
}
