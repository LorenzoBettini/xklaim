/*
 * Created on Feb 24, 2006
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.SessionId;

/**
 * A specialized NodeCoordinator that only performs a connect to a specified
 * SessionId and starts the specified Protocol.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ConnectNodeCoordinator extends NodeCoordinator {
    /**
     * The SessionId to connect to
     */
    protected SessionId sessionId;

    /**
     * The protocol to execute when connection was successfully established.
     */
    protected Protocol protocol;

    /**
     * @see org.mikado.imc.topology.NodeCoordinator#execute()
     */
    @Override
    public void execute() throws IMCException {
        connect(sessionId, protocol);
    }

    /**
     * @param protocol
     *            The protocol to execute when connection was successfully
     *            established.
     * @param sessionId
     *            The SessionId to connect to
     */
    public ConnectNodeCoordinator(Protocol protocol, SessionId sessionId) {
        this.sessionId = sessionId;
        this.protocol = protocol;
    }

}
