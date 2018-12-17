/*
 * Created on Jan 11, 2005
 *
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
 * A server waiting for incoming connections.
 * 
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class ConnectionServer {
    /**
     * Some one else has closed this server.
     */
    protected boolean closed = false;

    /** The connection manager. */
    protected SessionManager sessionManager;

    /** The id used for listening for incoming sessions. */
    protected SessionId sessionId;

    /**
     * The default SessionStarter used to accept sessions.
     */
    protected SessionStarter sessionStarter;

    /**
     * The table used to create a SessionStarter given a connection protocol id.
     * Default to IMCSessionStarterTable
     */
    protected SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();

    /**
     * Constructs a ConnectionServer.
     * 
     * @param sessionManager
     * @param sessionId
     *            The id used for listening for incoming connections
     * @throws ProtocolException
     */
    public ConnectionServer(SessionManager sessionManager, SessionId sessionId)
            throws ProtocolException {
        this.sessionManager = sessionManager;
        this.sessionId = sessionId;
        sessionStarter = sessionStarterTable.createSessionStarter(sessionId, null);
        this.sessionManager.addSessionStarter(sessionStarter);
    }

    /**
     * Constructs a ConnectionServer.
     * 
     * @param sessionManager
     * @throws ProtocolException
     */
    public ConnectionServer(SessionManager sessionManager)
            throws ProtocolException {
        this.sessionManager = sessionManager;
    }

    /**
     * Stops listening for incoming sessions.
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        closed = true;

        if (sessionStarter != null)
            sessionManager.removeSessionStarter(sessionStarter);
    }

    /**
     * Accepts an incoming session and starts the passed protocol on the
     * established session. and
     * returns a protocol that can be started on the established session. The
     * passed protocol will be used to handle session. The returned protocol
     * will have a start state (for dealing with connection protocol) and an end
     * state (for dealing with disconnection protocol). Thus the returned
     * protocol is different from the passed one (it will use the passed one).
     * 
     * @param protocol
     * @return A protocol composed with the passed one, ready to start
     * @throws ProtocolException
     */
    public Protocol accept(Protocol protocol) throws ProtocolException {
        return accept(sessionStarter, protocol);
    }

    /**
     * Accepts an incoming session by using the passed SessionStarter and
     * returns a protocol that can be started on the established session. The
     * passed protocol will be used to handle session. The returned protocol
     * will have a start state (for dealing with connection protocol) and an end
     * state (for dealing with disconnection protocol). Thus the returned
     * protocol is different from the passed one (it will use the passed one).
     * 
     * @param sessionStarter
     * @param protocol
     * @return A protocol composed with the passed one, ready to start
     * @throws ProtocolException
     */
    public Protocol accept(SessionStarter sessionStarter, Protocol protocol)
            throws ProtocolException {
        ConnectionManagementState conn_state = new ConnectionManagementState();
        conn_state.setSessionManager(sessionManager);

        ConnectionManagementState end_conn_state = new ConnectionManagementState(
                Protocol.END);
        end_conn_state.setSessionManager(sessionManager);

        ProtocolComposite protocolComposite = new ProtocolComposite(conn_state,
                end_conn_state, protocol);

        protocolComposite.setEventManager(sessionManager.getEventManager());
        protocolComposite.accept(sessionStarter);

        return protocolComposite;
    }

    /**
     * Accepts an incoming session and returns the associated ProtocolStack.
     * 
     * @return The protocol layer associated to the received connection.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept() throws ProtocolException {
        return accept(sessionStarter);
    }

    /**
     * Accepts an incoming session using the passed SessionStarter and returns
     * the associated ProtocolStack.
     * 
     * This method also updates the SessionManager.
     * 
     * @return The protocol layer associated to the received connection.
     * 
     * @throws ProtocolException
     */
    public ProtocolStack accept(SessionStarter sessionStarter)
            throws ProtocolException {
        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.accept(sessionStarter);

        /*
         * if we're here, i.e., no exception, we can add the session to the
         * SessionManager
         */
        sessionManager.addSession(protocolStack);

        return protocolStack;
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
