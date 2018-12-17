/*
 * Created on Nov 24, 2005
 */
package org.mikado.imc.topology;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;

/**
 * A pair of SessionManager's: one for accepted sessions and one for established
 * sessions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class SessionManagers {
    /** The connection manager that stores the sessions we accepted. */
    public SessionManager incomingSessionManager;

    /** The connection manager that stores the sessions we established. */
    public SessionManager outgoingSessionManager;

    public SessionManagers() {
        incomingSessionManager = new SessionManager();
        outgoingSessionManager = new SessionManager();
    }

    /**
     * Closes both the accepted and the established sessions
     * 
     * @see org.mikado.imc.topology.SessionManager#close()
     */
    public void close() {
        outgoingSessionManager.close();
        incomingSessionManager.close();
    }

    /**
     * Returns the protocol stack associated to the specified node location. It
     * searches among the outgoing connections and then among the incoming
     * connections.
     * 
     * @see org.mikado.imc.topology.SessionManager#getNodeStack(org.mikado.imc.topology.NodeLocation)
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        ProtocolStack protocolStack = outgoingSessionManager
                .getNodeStack(nodeLocation);

        if (protocolStack == null) {
            protocolStack = incomingSessionManager.getNodeStack(nodeLocation);
        }

        return protocolStack;
    }

    /**
     * Whether the specified location corresponds to a local end of an outgoing
     * or incoming connection.
     * 
     * @param nodeLocation
     * @return Whether the specified location corresponds to a local end of an
     *         outgoing or incoming connection.
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return outgoingSessionManager.isLocal(nodeLocation)
                || incomingSessionManager.isLocal(nodeLocation);
    }

    /**
     * Sets the EventManager of both SessionManager's.
     * 
     * @see org.mikado.imc.topology.SessionManager#setEventManager(org.mikado.imc.events.EventManager)
     */
    public void setEventManager(EventManager eventManager) {
        outgoingSessionManager.setEventManager(eventManager);
        incomingSessionManager.setEventManager(eventManager);
    }

    /**
     * Removes the specified Session from both the established and accepted
     * sessions.
     * 
     * @param session
     */
    public void removeSession(Session session) {
        outgoingSessionManager.removeSession(session);
        incomingSessionManager.removeSession(session);
    }

    /**
     * Closes all the Sessions that include the specified SessionId.
     * 
     * @param sessionId
     */
    public void closeSessions(SessionId sessionId) {
        outgoingSessionManager.closeSessions(sessionId);
        incomingSessionManager.closeSessions(sessionId);
    }
}
