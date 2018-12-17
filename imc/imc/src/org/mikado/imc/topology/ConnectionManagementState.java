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
 * Protocol state that manages connection and disconnection requests.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class ConnectionManagementState extends ProtocolStateSimple {
    /** The manager of the connections */
    protected SessionManager sessionManager;

    /**
     * The incoming string representing a connection request, default "CONNECT"
     */
    protected String connection_string = "CONNECT";

    /**
     * The incoming string representing a disconnection request, default
     * "DISCONNECT"
     */
    protected String disconnection_string = "DISCONNECT";

    /** The outgoing string stating that the operation succeeded. */
    protected String ok_string = "OK";

    /** The outgoing string stating that the operation failed. */
    protected String fail_string = "FAIL";

    /**
     * Constructs a ConnectionManagementState.
     */
    public ConnectionManagementState() {
    }

    /**
     * Constructs a ConnectionManagementState that uses a specific next state.
     * 
     * @param next_state
     */
    public ConnectionManagementState(String next_state) {
        super(next_state);
    }

    /**
     * Tries to add a connection delegating to SessionManager. If the connection
     * manager is not set, always returns true.
     * 
     * @param protocolStack
     *            The protocol layer associated to the connection.
     * 
     * @return true if the addition succeeds.
     * @throws ProtocolException
     */
    protected boolean addConnection(ProtocolStack protocolStack)
            throws ProtocolException {
        if (sessionManager == null) {
            return true;
        }

        if (sessionManager.addSession(protocolStack)) {
            generate(SessionManager.EventClass, new SessionEvent(this,
                    SessionEventType.CONNECTION, protocolStack.getSession()));

            return true;
        }

        return false;
    }

    /**
     * Tries to remove a connection delegating to SessionManager. If the
     * connection manager is not set, always returns true.
     * 
     * @param sessionId
     * 
     * @return true if the remove succeeds.
     */
    protected boolean removeConnection(Session sessionId) {
        if (sessionManager == null) {
            return true;
        }

        if (sessionManager.removeSession(sessionId)) {
            generate(SessionManager.EventClass, new SessionEvent(this,
                    SessionEventType.DISCONNECTION, sessionId));

            return true;
        }

        return false;
    }

    /**
     * Waits for a connection or a disconnection string, and communicates it to
     * the Connection Manager; it then leaves the state. It a non recognized
     * string is received, it closes the communication (to avoid loops).
     * 
     * @param param
     * @param transmissionChannel
     * 
     * @throws ProtocolException
     */
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {

        try {
            if (transmissionChannel == null) {
                transmissionChannel = new TransmissionChannel(
                        createUnMarshaler());
            }

            String received = transmissionChannel.readStringLine();
            boolean ok = false;

            if (received.equals(connection_string)) {
                if (addConnection(protocolStack)) {
                    ok = true;
                }
            } else if (received.equals(disconnection_string)) {
                if (removeConnection(getSession())) {
                    ok = true;
                }
            }

            Marshaler marshaler = createMarshaler();

            if (ok) {
                marshaler.writeStringLine(ok_string);
            } else {
                marshaler.writeStringLine(fail_string);
            }

            releaseMarshaler(marshaler);
            
            /* if something went wrong, close the connection */
            if (!ok) {
                removeConnection(getSession());
                close();
            }

            if (received.equals(disconnection_string)) {
                try {
                    // do not close the connection immediately:
                    // try to give time to the end-point to receive result
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }

                close();
            }
        } catch (IOException e) {
            e.printStackTrace();

            // if we got here it means that somehow we cannot communicate
            // with the endpoint and we assume that connection is lost.
            removeConnection(getSession());
            close();

            throw new ProtocolException(e);
        }
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#closed()
     */
    @Override
    public void closed() throws ProtocolException {
        Session session = getSession();

        if (session != null)
            removeConnection(session);
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
}
