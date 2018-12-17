/*
 * Created on Jan 13, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.events.SessionEvent;
import org.mikado.imc.events.SessionEvent.SessionEventType;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.TransmissionChannel;

import java.io.IOException;


/**
 * Protocol state that establishes a connection or a disconnection requests.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class ConnectState extends ProtocolStateSimple {
    /** The manager of the connections */
    protected SessionManager sessionManager;

    /** The string representing a connection request, default "CONNECT" */
    protected String connection_string = "CONNECT";

    /** The string representing a disconnection request, default "DISCONNECT" */
    protected String disconnection_string = "DISCONNECT";

    /** The string stating that the operation succeeded. */
    protected String ok_string = "OK";

    /** The string stating that the operation failed. */
    protected String fail_string = "FAIL";

    /**
     * Whether this state is used for a connection (otherwise it is used for a
     * disconnection).  Default: used for a connection.
     */
    protected boolean doConnect = true;

    /**
     * Creates a new ConnectState object.
     */
    public ConnectState() {
    }

    /**
     * Constructs a ConnectState.
     *
     * @param next_state The next state of this state.
     */
    public ConnectState(String next_state) {
        super(next_state);
    }

    /**
     * Constructs a ConnectState.
     *
     * @param next_state The next state if a connection or disconnection
     *        request succeeds.
     * @param connection_string String sent for a connection.
     * @param disconnection_string String sent for a disconnection.
     * @param ok_string String received for ok.
     * @param fail_string String received for failure.
     */
    public ConnectState(String next_state, String connection_string,
        String disconnection_string, String ok_string, String fail_string) {
        super(next_state);
        this.connection_string = connection_string;
        this.disconnection_string = disconnection_string;
        this.ok_string = ok_string;
        this.fail_string = fail_string;
    }

    /**
     * Constructs a ConnectState that uses a specific SessionManager.
     *
     * @param sessionManager
     */
    public ConnectState(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Sends a connection or a disconnection string, and communicates the
     * result to the Connection Manager; it then leaves the state.  It does
     * not leave the state until a valid connection or disconnection string is
     * received.
     *
     * @param param
     * @param transmissionChannel
     *
     * @throws ProtocolException
     */
    public void enter(Object param, TransmissionChannel transmissionChannel)
        throws ProtocolException {
        while (true) {
            try {
                String tosend = (isDoConnect() ? connection_string
                                                : disconnection_string);

                Marshaler marshaler = createMarshaler();

                marshaler.writeStringLine(tosend);

                releaseMarshaler(marshaler);

                if (transmissionChannel == null) {
                    transmissionChannel = new TransmissionChannel(createUnMarshaler());
                }

                String received = transmissionChannel.readStringLine();
                boolean ok = false;

                if (received.equals(ok_string)) {
                    ok = true;

                    if (isDoConnect()) {
                        addConnection(protocolStack);
                    } else {
                        removeConnection(protocolStack.getSession());
                    }
                }

                if (ok && !isDoConnect()) {
                    close();
                }

                if (ok) {
                    return;
                }

                transmissionChannel = null;
            } catch (IOException e) {
                e.printStackTrace();

                // if we got here it means that somehow we cannot communicate
                // with the endpoint and we assume that connection is lost.
                removeConnection(getSession());
                close();

                throw new ProtocolException(e);
            }
        }
    }
	
    /* (non-Javadoc)
	 * @see org.mikado.imc.protocols.ProtocolStateSimple#closed()
	 */
	@Override
	public void closed() throws ProtocolException {
		Session session = getSession();
		
		if (session != null)
			removeConnection(session);
	}

    /**
     * Is the state used for a connection?  Otherwise it is used for a
     * disconnection.
     *
     * @return Returns <tt>true</tt> if it is used for a connection,
     *         <tt>false</tt> otherwise.
     */
    public final boolean isDoConnect() {
        return doConnect;
    }

    /**
     * Sets whether the state is used for a connection.  Otherwise it is used
     * for a disconnection.
     *
     * @param do_connect <tt>true</tt> if it is used for a connection,
     *        <tt>false</tt> otherwise.
     */
    public final void setDoConnect(boolean do_connect) {
        this.doConnect = do_connect;
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
     * @param sessionManager The sessionManager to set.
     */
    public final void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Notifies the connection manager of a new connection.
     *
     * @param protocolStack The protocol layer associated to the connection.
     *
     * @return <tt>true</tt> if there's no connection manager, or the result of
     *         the addConnection delegated to the connection manager.
     * @throws ProtocolException 
     */
    public boolean addConnection(ProtocolStack protocolStack) throws ProtocolException {
        if (sessionManager == null) {
            return true;
        }

        if (sessionManager.addSession(protocolStack)) {
            generate(SessionManager.EventClass,
                new SessionEvent(this, SessionEventType.CONNECTION,
                    protocolStack.getSession()));

            return true;
        }

        return false;
    }

    /**
     * Notifies the connection manager of a disconnection.
     *
     * @param sessionId The session id of the connection to remove.
     *
     * @return <tt>true</tt> if there's no connection manager, or the result of
     *         the removeConnection delegated to the connection manager.
     */
    public boolean removeConnection(Session sessionId) {
        if (sessionManager == null) {
            return true;
        }

        if (sessionManager.removeSession(sessionId)) {
            generate(SessionManager.EventClass,
                new SessionEvent(this, SessionEventType.DISCONNECTION,
                    sessionId));

            return true;
        }

        return false;
    }
}
