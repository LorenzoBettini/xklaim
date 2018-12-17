/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarterTable;

/**
 * Represents a generic node in a network.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.25 $
 */
public class Node implements Closeable {
    /**
     * The name of this node (it is not guaranteed to be unique).
     */
    protected String nodeName;

    /** The server listening for incoming connections. */
    protected ConnectionServer connectionServer;

    /** The object used to establish a connection. */
    protected ConnectionStarter connectionStarter;

    /** The SessionManagers for accepted and established sessions. */
    protected SessionManagers sessionManagers;

    /** The event manager of the node. */
    protected EventManager eventManager;

    /**
     * The table for creating a SessionStarter.
     */
    protected SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();

    /**
     * Contains references to running processes. This is accessible only from
     * within the framework itself.
     */
    protected ProcessContainer<Thread> processContainer = new ProcessContainer<Thread>();

    /**
     * The possible application this node belongs to.
     */
    protected Application application;

    /**
     * Creates a new Node object.
     */
    public Node() {
        nodeName = getClass().getName();
        init();
    }

    /**
     * Creates a new Node object.
     */
    public Node(String name) {
        nodeName = name;
        init();
    }

    /**
     * Initialized basic elements
     */
    private void init() {
        sessionManagers = new SessionManagers();
        eventManager = new EventManager();
        sessionManagers.setEventManager(eventManager);
        processContainer.setEventManager(eventManager);
    }

    /**
     * Returns the event manager of the node.
     * 
     * @return Returns the event manager of the node.
     */
    public final EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Sets the event manager of the node. It also ensures that all the objects
     * related to events in this node are update with this event manager.
     * 
     * @param eventManager
     *            The event manager to set.
     */
    public final void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        sessionManagers.setEventManager(eventManager);
        processContainer.setEventManager(eventManager);
    }

    /**
     * Connects this node to a remote host, and executes a protocol.
     * 
     * @param sessionId
     * @param protocol
     * 
     * @throws ProtocolException
     */
    public void connect(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        ConnectionStarter connectionStarter = new ConnectionStarter(
                sessionManagers.outgoingSessionManager);
        connectionStarter.setSessionStarterTable(getSessionStarterTable());
        connectionStarter.connect(sessionId, protocol).start();
    }

    /**
     * Connects this node to a remote host and returns the associated protocol
     * stack.
     * 
     * @param sessionId
     * 
     * @return The protocol stack associated to the established Session.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionId sessionId) throws ProtocolException {
        ConnectionStarter connectionStarter = new ConnectionStarter(
                sessionManagers.outgoingSessionManager);
        connectionStarter.setSessionStarterTable(getSessionStarterTable());
        return connectionStarter.connect(sessionId);
    }

    /**
     * Connects this node to a remote host by using the passed SessionStarter,
     * and executes a protocol.
     * 
     * @param sessionStarter
     * @param protocol
     * 
     * @throws ProtocolException
     */
    public void connect(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        ConnectionStarter connectionStarter = new ConnectionStarter(
                sessionManagers.outgoingSessionManager);
        connectionStarter.connect(sessionStarter, protocol).start();
    }

    /**
     * Connects this node to a remote host by using the passed SessionStarter
     * and returns the associated protocol stack.
     * 
     * @param sessionStarter
     * 
     * @return The protocol stack associated to the established Session.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionStarter sessionStarter)
            throws ProtocolException {
        ConnectionStarter connectionStarter = new ConnectionStarter(
                sessionManagers.outgoingSessionManager);

        return connectionStarter.connect(sessionStarter);
    }

    /**
     * Waits for an incoming connection and starts the passed protocol on the
     * received Session. Only waits for one connection.
     * 
     * @param sessionId
     *            The session id on which it listens for connections.
     * @param protocol
     *            The protocol to start on the connection.
     * 
     * @throws ProtocolException
     */
    public void acceptAndStart(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        Protocol protocol2 = accept(sessionId, protocol);
        protocol2.start();
    }

    /**
     * Waits for an incoming connection and connects the passed protocol on the
     * received connection. Only waits for one connection.
     * 
     * @param sessionId
     *            The session id on which it listens for connections.
     * @param protocol
     *            The protocol to start on the connection.
     * @return The protocol ready to start.
     * @throws ProtocolException
     */
    public Protocol accept(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        ConnectionServer connectionServer = new ConnectionServer(
                sessionManagers.incomingSessionManager, sessionId);
        connectionServer.setSessionStarterTable(getSessionStarterTable());
        Protocol p = connectionServer.accept(protocol);
        connectionServer.close();

        return p;
    }

    /**
     * Waits for an incoming connection and returns the associated protocol
     * stack. Only waits for one connection.
     * 
     * @param sessionId
     *            The session id on which it listens for connections.
     * 
     * @return The protocol stack associated with the received connection.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept(SessionId sessionId) throws ProtocolException {
        ConnectionServer connectionServer = new ConnectionServer(
                sessionManagers.incomingSessionManager, sessionId);
        connectionServer.setSessionStarterTable(getSessionStarterTable());
        ProtocolStack protocolStack = connectionServer.accept();
        connectionServer.close();

        return protocolStack;
    }

    /**
     * Waits for an incoming connection using the passed SessionStarter and
     * starts the passed protocol on the received Session.
     * 
     * @param sessionStarter
     *            The SessionStarter used for accepting connections.
     * @param protocol
     *            The protocol to start on the connection.
     * 
     * @throws ProtocolException
     */
    public void acceptAndStart(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        Protocol p = accept(sessionStarter, protocol);
        p.start();
    }

    /**
     * Waits for an incoming connection using the passed SessionStarter and
     * returns a Protocol modified with CONNECT/DISCONNECT state embedding the
     * passed protocol.
     * 
     * @param sessionStarter
     *            The SessionStarter used for accepting connections.
     * @param protocol
     *            The protocol to start on the connection.
     * @return a Protocol modified with CONNECT/DISCONNECT state embedding the
     *         passed protocol.
     * @throws ProtocolException
     */
    public Protocol accept(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        ConnectionServer connectionServer = new ConnectionServer(
                sessionManagers.incomingSessionManager);
        Protocol p = connectionServer.accept(sessionStarter, protocol);
        connectionServer.close();

        return p;
    }

    /**
     * Waits for an incoming connection using the passed SessionStarter and
     * returns the associated protocol stack. Only waits for one connection.
     * 
     * @param sessionStarter
     *            The SessionStarter used for accepting connections.
     * 
     * @return The protocol stack associated with the received connection.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept(SessionStarter sessionStarter)
            throws ProtocolException {
        ConnectionServer connectionServer = new ConnectionServer(
                sessionManagers.incomingSessionManager);
        ProtocolStack protocolStack = connectionServer.accept(sessionStarter);
        connectionServer.close();

        return protocolStack;
    }

    /**
     * Closes the session associated with the specified protocol stack.
     * 
     * @param protocolStack
     *            The protocol stack to disconnect.
     * 
     * @throws ProtocolException
     */
    public void disconnect(ProtocolStack protocolStack)
            throws ProtocolException {
        protocolStack.close();
    }

    /**
     * Spawns a node coordinator process in execution on this node. As soon as
     * the node coordinator is started, this method returns.
     * 
     * @param nodeCoordinator
     * @throws IMCException
     */
    public void addNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws IMCException {
        NodeCoordinatorProxy nodeProxy = createNodeCoordinatorProxy();
        nodeCoordinator.setNodeCoordinatorProxy(nodeProxy);
        processContainer.addElement(nodeCoordinator);
        nodeCoordinator.start();
    }

    /**
     * Removes a process from the process container (but does not interrupt it).
     * 
     * This method is intended to be used only from within the framework.
     * 
     * @param nodeCoordinator
     */
    void removeNodeCoordinator(NodeCoordinator nodeCoordinator) {
        processContainer.removeElement(nodeCoordinator);
    }

    /**
     * Creates a NodeCoordinatorProxy associated to the node (Factory Method)
     * 
     * @return the created NodeCoordinatorProxy
     */
    protected NodeCoordinatorProxy createNodeCoordinatorProxy() {
        return new NodeCoordinatorProxy(this);
    }

    /**
     * Spawns a node coordinator process in execution on this node and waits for
     * its termination.
     * 
     * @param nodeCoordinator
     * 
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeCoordinator(NodeCoordinator nodeCoordinator)
            throws InterruptedException, IMCException {
        addNodeCoordinator(nodeCoordinator);
        nodeCoordinator.join();
    }

    /**
     * Spawns a node process in execution on this node. As soon as the node
     * process is started, this method returns.
     * 
     * @param nodeProcess
     * @throws IMCException
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        NodeProcessProxy processProxy = createNodeProcessProxy();
        nodeProcess.setNodeProcessProxy(processProxy);
        processContainer.addElement(nodeProcess);
        nodeProcess.start();
    }

    /**
     * Removes a process from the process container (but does not interrupt it).
     * 
     * This method is intended to be used only from within the framework.
     * 
     * @param nodeProcess
     */
    void removeNodeProcess(NodeProcess nodeProcess) {
        processContainer.removeElement(nodeProcess);
    }

    /**
     * Creates a NodeProcessProxy associated to the node (Factory Method)
     * 
     * @return The create NodeProcessProxy
     */
    protected NodeProcessProxy createNodeProcessProxy() {
        return new NodeProcessProxy(this);
    }

    /**
     * Spawns a node process in execution on this node and waits for its
     * termination.
     * 
     * @param nodeProcess
     * 
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        addNodeProcess(nodeProcess);
        nodeProcess.join();
    }

    /**
     * Closes all the sessions.
     */
    public void closeSessions() {
        sessionManagers.close();
    }

    /**
     * Closes all the sessions and session starters that involve the passed
     * SessionId.
     * 
     * @param sessionId
     */
    public void closeSessions(SessionId sessionId) {
        sessionManagers.closeSessions(sessionId);
    }

    /**
     * Closes the node: closes all sessions and all processes running on this
     * node.
     * 
     * @see org.mikado.imc.topology.Closeable#close()
     */
    public void close() throws IMCException {
        closeSessions();

        try {
            /*
             * OK, now the processes should have already received their
             * exceptions, but we must make sure that everyone terminated.
             */
            processContainer.close();
        } finally {
            if (application != null)
                application.removeNode(this);
        }
    }

    /**
     * Returns the protocol stack associated to the specified node location. It
     * searches among the outgoing connections and then among the incoming
     * connections.
     * 
     * @param nodeLocation
     * 
     * @return the protocol stack associated to the specified node location
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return sessionManagers.getNodeStack(nodeLocation);
    }

    /**
     * Create a SessionStarter associated to the specified SessionIds.
     * 
     * @param localSessionId
     *            The local session identifier.
     * @param remoteSessionId
     *            The remote session identifier.
     * @return the created SessionStarter
     * @throws ProtocolException
     */
    public SessionStarter createSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) throws ProtocolException {
        return getSessionStarterTable().createSessionStarter(localSessionId,
                remoteSessionId);
    }

    /**
     * @return Returns the sessionStarterTable.
     */
    public final SessionStarterTable getSessionStarterTable() {
        return sessionStarterTable;
    }

    /**
     * @param sessionStarterTable
     *            The sessionStarterTable to set.
     */
    public final void setSessionStarterTable(
            SessionStarterTable sessionStarterTable) {
        this.sessionStarterTable = sessionStarterTable;
    }

    /**
     * @see org.mikado.imc.events.EventManager#addListener(java.lang.String,
     *      org.mikado.imc.events.EventListener)
     */
    public void addListener(String event, EventListener eventListener) {
        eventManager.addListener(event, eventListener);
    }

    /**
     * @see org.mikado.imc.events.EventManager#generate(java.lang.String,
     *      org.mikado.imc.events.Event)
     */
    public void generate(String event_id, Event event) {
        eventManager.generate(event_id, event);
    }

    /**
     * @see org.mikado.imc.events.EventManager#removeListener(java.lang.String,
     *      org.mikado.imc.events.EventListener)
     */
    public void removeListener(String event, EventListener eventListener) {
        eventManager.removeListener(event, eventListener);
    }

    /**
     * @return Returns the sessionManagers.
     */
    public SessionManagers getSessionManagers() {
        return sessionManagers;
    }

    /**
     * @param sessionManagers
     *            The sessionManagers to set.
     */
    public void setSessionManagers(SessionManagers sessionManagers) {
        this.sessionManagers = sessionManagers;
    }

    /**
     * Whether the specified location corresponds to a local end of one of the
     * connections of this node, i.e., whether the specified location represents
     * this node.
     * 
     * @param nodeLocation
     * @return whether the specified location represents this node
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return sessionManagers.isLocal(nodeLocation);
    }

    /**
     * @return Returns the nodeName.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName
     *            The nodeName to set.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return Returns the application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * @param application
     *            The application to set.
     * @throws IMCException
     */
    public void setApplication(Application application) throws IMCException {
        this.application = application;
    }

    /**
     * Invokes System.out.print.
     * 
     * Subclasses can override this in order to print somewhere else.
     * 
     * @param s
     *            The string to print
     */
    public void SystemOutPrint(String s) {
        System.out.print(s);
    }

    /**
     * Invokes System.err.print
     * 
     * Subclasses can override this in order to print somewhere else.
     * 
     * @param s
     *            The string to print
     */
    public void SystemErrPrint(String s) {
        System.err.print(s);
    }

    /**
     * @return Returns the processContainer.
     */
    public ProcessContainer<Thread> getProcessContainer() {
        return processContainer;
    }

    /**
     * @param processContainer
     *            The processContainer to set.
     */
    public void setProcessContainer(ProcessContainer<Thread> processContainer) {
        this.processContainer = processContainer;
    }
}
