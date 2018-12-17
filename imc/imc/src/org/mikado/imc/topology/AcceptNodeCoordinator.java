/*
 * Created on Jan 19, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;

/**
 * A node coordinator that continuosly accepts incoming sessions on the same
 * SessionId.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public class AcceptNodeCoordinator extends NodeCoordinator {
    private ProtocolFactory protocolFactory;

    private SessionId sessionId;

    private SessionStarter sessionStarter;

    /**
     * @param protocolFactory
     *            The protocol factory that will be used to create protocols for
     *            incoming connections.
     * @param sessionStarter
     *            The SessionStarter that will be used for accepting sessions
     */
    public AcceptNodeCoordinator(ProtocolFactory protocolFactory,
            SessionStarter sessionStarter) {
        this.protocolFactory = protocolFactory;
        this.sessionStarter = sessionStarter;
    }

    /**
     * Creates a new AcceptNodeCoordinator object.
     * 
     * @param protocolFactory
     *            The protocol factory that will be used to create protocols for
     *            incoming connections.
     * @param sessionId
     *            The port to listen for incoming connections.
     */
    public AcceptNodeCoordinator(ProtocolFactory protocolFactory,
            SessionId sessionId) {
        this.protocolFactory = protocolFactory;
        this.sessionId = sessionId;
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinator#execute()
     */
    public void execute() throws IMCException {
        /* if not null it has been passed to the constructor */
        if (sessionStarter == null)
            sessionStarter = createSessionStarter(sessionId, null);
        
        while (true) {
            Protocol protocol = protocolFactory.createProtocol();
            addNodeProcess(new ProtocolThread(accept(sessionStarter, protocol)));
        }
    }

    /**
     * Stops listening for incoming connections.
     * 
     * @throws ProtocolException
     */
    @Override
    public void close() throws ProtocolException {
        if (sessionStarter != null)
            sessionStarter.close();
    }
}
