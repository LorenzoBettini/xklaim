/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;

/**
 * A proxy for an actual node. This is used to restrict node accessibility to
 * node coordinators.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.12 $
 */
public class NodeCoordinatorProxy extends NodeProcessProxy {
    /**
     * Creates a new NodeCoordinatorProxy object.
     * 
     * @param node
     */
    public NodeCoordinatorProxy(Node node) {
        super(node);
    }

    /**
     * @param sessionId
     * @param protocol
     * 
     * @throws ProtocolException
     */
    public void acceptAndStart(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        node.acceptAndStart(sessionId, protocol);
    }

    /**
     * @param sessionId
     * @param protocol
     * @throws ProtocolException
     */
    public void connect(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        node.connect(sessionId, protocol);
    }

    /**
     * @param sessionId
     * 
     * @return the ProtocolStack corresponding to the accepted Session
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept(SessionId sessionId) throws ProtocolException {
        return node.accept(sessionId);
    }

    /**
     * @param protocolStack
     * 
     * @throws ProtocolException
     */
    public void disconnect(ProtocolStack protocolStack)
            throws ProtocolException {
        node.disconnect(protocolStack);
    }

    /**
     * @return the EventManager
     */
    public EventManager getEventManager() {
        return node.getEventManager();
    }

    /**
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        node.setEventManager(eventManager);
    }

    /**
     * @param sessionId
     * 
     * @return the ProtocolStack corresponding to the established Session
     * 
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionId sessionId) throws ProtocolException {
        return node.connect(sessionId);
    }

    /**
     * @param sessionId
     * @param protocol
     * @return the Protocol corresponding to the accepted Session
     * @throws ProtocolException
     */
    public Protocol accept(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        return node.accept(sessionId, protocol);
    }

    /**
     * @param nodeCoordinator
     * @throws IMCException
     */
    public void addNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws IMCException {
        node.addNodeCoordinator(nodeCoordinator);
    }

    /**
     * @param nodeCoordinator
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws InterruptedException, IMCException {
        node.executeNodeCoordinator(nodeCoordinator);
    }

    /**
     * @see org.mikado.imc.topology.Node#accept(org.mikado.imc.protocols.SessionStarter)
     */
    public ProtocolStack accept(SessionStarter sessionStarter)
            throws ProtocolException {
        return node.accept(sessionStarter);
    }

    /**
     * @see org.mikado.imc.topology.Node#acceptAndStart(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public void acceptAndStart(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        node.acceptAndStart(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.Node#accept(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public Protocol accept(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        return node.accept(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.Node#connect(org.mikado.imc.protocols.SessionStarter)
     */
    public ProtocolStack connect(SessionStarter sessionStarter)
            throws ProtocolException {
        return node.connect(sessionStarter);
    }

    /**
     * @see org.mikado.imc.topology.Node#connect(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public void connect(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        node.connect(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.Node#createSessionStarter(org.mikado.imc.protocols.SessionId,
     *      org.mikado.imc.protocols.SessionId)
     */
    public SessionStarter createSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) throws ProtocolException {
        return node.createSessionStarter(localSessionId, remoteSessionId);
    }

    /**
     * @see org.mikado.imc.topology.Node#removeNodeCoordinator(org.mikado.imc.topology.NodeCoordinator)
     */
    void removeNodeCoordinator(NodeCoordinator nodeCoordinator) {
        node.removeNodeCoordinator(nodeCoordinator);
    }
}
