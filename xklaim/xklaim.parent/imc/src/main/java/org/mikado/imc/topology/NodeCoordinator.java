/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.topology;

import java.io.Serializable;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;

/**
 * A node process that can execute privileged actions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.21 $
 */
public abstract class NodeCoordinator extends Thread implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** The proxy for the node. */
    protected transient NodeCoordinatorProxy nodeCoordinatorProxy;

    /**
     * This is used to generate unique process name when no explicit name is
     * specified.
     */
    protected static int nextId = 0;

    /**
     * The default name of the process is created by using the process class
     * name and an unique (incremented) number.
     * 
     * Creates a new NodeCoordinator object.
     */
    public NodeCoordinator() {
        setName(getClass().getName() + "-" + getNextId());
    }

    /**
     * @param name
     */
    public NodeCoordinator(String name) {
        super(name);
    }

    /**
     * @param sessionId
     * @param protocol
     * 
     * @throws ProtocolException
     */
    public void acceptAndStart(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        nodeCoordinatorProxy.acceptAndStart(sessionId, protocol);
    }

    /**
     * @param sessionId
     * @param protocol
     * @throws ProtocolException
     */
    public void connect(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        nodeCoordinatorProxy.connect(sessionId, protocol);
    }

    /**
     * @return Returns the nodeCoordinatorProxy.
     */
    protected NodeCoordinatorProxy getNodeCoordinatorProxy() {
        return nodeCoordinatorProxy;
    }

    /**
     * @param nodeCoordinatorProxy
     *            The nodeCoordinatorProxy to set.
     */
    protected void setNodeCoordinatorProxy(
            NodeCoordinatorProxy nodeCoordinatorProxy) throws IMCException {
        this.nodeCoordinatorProxy = nodeCoordinatorProxy;
    }

    /**
     * This is the method invoked to actually start the process. Derived classes
     * must provide an implementation for it.
     * 
     * @throws IMCException
     */
    public abstract void execute() throws IMCException;

    /**
     * Method used to start the process. This will implicitly call execute().
     */
    public final void run() {
        try {
            execute();
        } catch (IMCException e) {
            e.printStackTrace();
        } finally {
            /* finally remove me from the list of running processes */
            if (nodeCoordinatorProxy != null)
                nodeCoordinatorProxy.removeNodeCoordinator(this);
        }
    }

    /**
     * @param sessionId
     * 
     * @return the ProtocolStack corresponding to the accepted Session
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept(SessionId sessionId) throws ProtocolException {
        return nodeCoordinatorProxy.accept(sessionId);
    }

    /**
     * @param protocolStack
     * 
     * @throws ProtocolException
     */
    public void disconnect(ProtocolStack protocolStack)
            throws ProtocolException {
        nodeCoordinatorProxy.disconnect(protocolStack);
    }

    /**
     * @return the EventManager
     */
    public EventManager getEventManager() {
        return nodeCoordinatorProxy.getEventManager();
    }

    /**
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        nodeCoordinatorProxy.setEventManager(eventManager);
    }

    /**
     * @param sessionId
     * @return the ProtocolStack corresponding to the established Session
     * 
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionId sessionId) throws ProtocolException {
        return nodeCoordinatorProxy.connect(sessionId);
    }

    /**
     * @param sessionId
     * @param protocol
     * @return the Protocol corresponding to the accepted Session
     * @throws ProtocolException
     */
    public Protocol accept(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        return nodeCoordinatorProxy.accept(sessionId, protocol);
    }

    /**
     * @param nodeLocation
     * @return the ProtocolStack
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return nodeCoordinatorProxy.getNodeStack(nodeLocation);
    }

    /**
     * @param nodeCoordinator
     * @throws IMCException
     */
    public void addNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws IMCException {
        nodeCoordinatorProxy.addNodeCoordinator(nodeCoordinator);
    }

    /**
     * @param nodeProcess
     * @throws IMCException
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        nodeCoordinatorProxy.addNodeProcess(nodeProcess);
    }

    /**
     * @param nodeCoordinator
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws InterruptedException, IMCException {
        nodeCoordinatorProxy.executeNodeCoordinator(nodeCoordinator);
    }

    /**
     * @param nodeProcess
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        nodeCoordinatorProxy.executeNodeProcess(nodeProcess);
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinatorProxy#accept(org.mikado.imc.protocols.SessionStarter)
     */
    public ProtocolStack accept(SessionStarter sessionStarter)
            throws ProtocolException {
        return nodeCoordinatorProxy.accept(sessionStarter);
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinatorProxy#acceptAndStart(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public void acceptAndStart(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        nodeCoordinatorProxy.acceptAndStart(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinatorProxy#accept(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public Protocol accept(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        return nodeCoordinatorProxy.accept(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinatorProxy#connect(org.mikado.imc.protocols.SessionStarter)
     */
    public ProtocolStack connect(SessionStarter sessionStarter)
            throws ProtocolException {
        return nodeCoordinatorProxy.connect(sessionStarter);
    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinatorProxy#connect(org.mikado.imc.protocols.SessionStarter,
     *      org.mikado.imc.protocols.Protocol)
     */
    public void connect(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        nodeCoordinatorProxy.connect(sessionStarter, protocol);
    }

    /**
     * @see org.mikado.imc.topology.Node#createSessionStarter(org.mikado.imc.protocols.SessionId,
     *      org.mikado.imc.protocols.SessionId)
     */
    public SessionStarter createSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) throws ProtocolException {
        return nodeCoordinatorProxy.createSessionStarter(localSessionId,
                remoteSessionId);
    }

    /**
     * This is called to tell the process that it should terminate.
     * 
     * Subclasses can override it in order to make the process terminate somehow
     * (e.g., by closing a stream used by the process, or a socket, etc.).
     * 
     * The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void close() throws IMCException {

    }

    /**
     * Returns the next id (incremented)
     * 
     * @return Returns the nextId.
     */
    synchronized protected static int getNextId() {
        return ++nextId;
    }

    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard output of the
     * node.
     * 
     * @param s
     *            The string to print
     */
    protected void SystemOutPrint(String s) {
        nodeCoordinatorProxy.SystemOutPrint(getName() + ": " + s);
    }

    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard error of the
     * node.
     * 
     * @param s
     *            The string to print
     */
    protected void SystemErrPrint(String s) {
        nodeCoordinatorProxy.SystemErrPrint(getName() + ": " + s);
    }

    /**
     * @see org.mikado.imc.topology.NodeProcessProxy#closeSessions(org.mikado.imc.protocols.SessionId)
     */
    public void closeSessions(SessionId sessionId) {
        nodeCoordinatorProxy.closeSessions(sessionId);
    }
}
