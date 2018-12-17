/*
 * Created on Jan 16, 2005
 */
package org.mikado.imc.topology;

import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolComposite;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarterTable;

/**
 * Provides connection functionalities.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public class ConnectionStarter {
    /** The connection mamager. */
    protected SessionManager sessionManager;

    /**
     * The table used to create a SessionStarter given a connection protocol id.
     * Default to IMCSessionStarterTable
     */
    protected SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();

    /**
     * Creates a new ConnectionStarter object.
     */
    public ConnectionStarter() {
    }

    /**
     * Creates a new ConnectionStarter object.
     * 
     * @param sessionManager
     */
    public ConnectionStarter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Tries to establish a session with the specified session identifier and if
     * it succeeds it associates this connection with the passed protocol. The
     * returned protocol will have a start state and an end state using
     * CONNECT/DISCONNECT protocol states. The returned protocol is then
     * different from the passed one (it will use the passed one).
     * 
     * @param sessionId
     * @param protocol
     * @return A protocol composed with the passed one, ready to start
     * @throws ProtocolException
     */
    public Protocol connect(SessionId sessionId, Protocol protocol)
            throws ProtocolException {
        return connect(sessionStarterTable.createSessionStarter(null, sessionId),
                protocol);
    }

    /**
     * Tries to establish a session using the specified SessionStarter and if it
     * succeeds it associates this session with the passed protocol and returns
     * a protocol that can be started. The returned protocol will have a start
     * state and an end state using CONNECT/DISCONNECT protocol states. The
     * returned protocol is then different from the passed one (it will use the
     * passed one).
     * 
     * @param sessionStarter
     * @param protocol
     * @return A protocol composed with the passed one, ready to start
     * @throws ProtocolException
     */
    public Protocol connect(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        // TODO: abstract from the specific connection type
        ConnectState connectState = new ConnectState();
        ConnectState disconnectState = new ConnectState();
        disconnectState.setDoConnect(false);

        ProtocolComposite protocolComposite = new ProtocolComposite(
                connectState, disconnectState, protocol);

        if (sessionManager != null) {
            connectState.setSessionManager(sessionManager);
            disconnectState.setSessionManager(sessionManager);

            protocolComposite.setEventManager(sessionManager.getEventManager());
        }

        protocolComposite.connect(sessionStarter);

        return protocolComposite;
    }

    /**
     * Tries to establish a session with the specified session identifier and if
     * it succeeds it returns the associated protocol stack.
     * 
     * @param sessionId
     *            the session identifier to connect to
     * @return The protocol stack associated to the established connection.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionId sessionId) throws ProtocolException {
        return connect(sessionStarterTable.createSessionStarter(null, sessionId));
    }

    /**
     * Tries to establish a session by using the specified SessionStarter and if
     * it succeeds it returns the associated protocol stack.
     * 
     * This method also updates the SessionManager.
     * 
     * @param sessionStarter
     * @return the associated ProtocolStack
     * @throws ProtocolException
     */
    public ProtocolStack connect(SessionStarter sessionStarter)
            throws ProtocolException {
        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.connect(sessionStarter);

        /*
         * if we're here, i.e., no exception, we can add the session to the
         * SessionManager
         */
        sessionManager.addSession(protocolStack);

        return protocolStack;
    }

    /**
     * Returns the connection manager.
     * 
     * @return Returns the sessionManager.
     */
    public final SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Sets the connection manager.
     * 
     * @param sessionManager
     *            The sessionManager to set.
     */
    public final void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
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
}
