/*
 * Created on Jan 4, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.events.SessionEvent;
import org.mikado.imc.events.EventGenerator;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.SessionEvent.SessionEventType;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarters;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Manage the sessions.
 * 
 * @author $author$
 * @version $Revision: 1.9 $
 */
public class SessionManager implements EventGenerator {
    /** The string representing connection/disconnection events. */
    public static final String EventClass = "CONNECTION";

    /**
     * Stores all the current connections. key: Session, value: ProtocolLayer
     */
    Hashtable<Session, ProtocolStack> connections = new Hashtable<Session, ProtocolStack>();

    /**
     * To collect SessionStarter references
     */
    SessionStarters sessionStarters = new SessionStarters();

    /** The event manager. */
    EventManager eventManager;

    /**
     * Adds a session.
     * 
     * @param protocolStack
     *            the ProtocolStack representing the added Session.
     * 
     * @return <tt>false</tt> if the id is already present.
     * @throws ProtocolException
     */
    public synchronized boolean addSession(ProtocolStack protocolStack)
            throws ProtocolException {
        Session id = protocolStack.getSession();

        if (connections.containsKey(id)) {
            return false;
        }

        connections.put(id, protocolStack);
        System.out.println("added session: " + id);
        generateEvent(new SessionEvent(this, SessionEventType.CONNECTION, id));

        return true;
    }

    /**
     * Removes a Session
     * 
     * @param session
     *            The Session to remove.
     * 
     * @return <tt>false</tt> if the session is not present.
     */
    public synchronized boolean removeSession(Session session) {
        if (connections.remove(session) != null) {
            System.out.println("removed session: " + session);
            generateEvent(new SessionEvent(this,
                    SessionEventType.DISCONNECTION, session));

            return true;
        }

        return false;
    }

    /**
     * Stores the passed SessionStarter
     * 
     * @param sessionStarter
     * @throws ProtocolException
     */
    public synchronized void addSessionStarter(SessionStarter sessionStarter)
            throws ProtocolException {
        sessionStarters.addSessionStarter(sessionStarter);
    }

    /**
     * Removes and closes the passed SessionStarter
     * 
     * @param sessionStarter
     * @throws ProtocolException
     */
    public synchronized void removeSessionStarter(SessionStarter sessionStarter)
            throws ProtocolException {
        sessionStarters.removeAndCloseSessionStarter(sessionStarter);
    }

    /**
     * Closes all the registered sessions.
     */
    public synchronized void close() {
        Enumeration<ProtocolStack> sessions = connections.elements();

        while (sessions.hasMoreElements()) {
            try {
                sessions.nextElement().close();
            } catch (ProtocolException e) {
                // ignore it
            }
        }

        sessionStarters.close();
    }

    /**
     * Returns a string representation of sessions in the form of a set of
     * entries, enclosed in braces and separated by the ASCII characters ", "
     * (comma and space). Each entry is rendered as the key, an equals sign =,
     * and the associated element.
     * 
     * @return String representation of connections
     */
    public synchronized String toString() {
        return connections.toString();
    }

    /**
     * Sets the event manager.
     * 
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Returns the eventManager.
     * 
     * @return Returns the eventManager.
     */
    public final EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Generates an event.
     * 
     * @param event
     */
    protected synchronized void generateEvent(SessionEvent event) {
        if (eventManager == null) {
            return;
        }

        eventManager.generate(EventClass, event);
    }

    /**
     * Searches, among the registered sessions, for a session whose remote end
     * is the same of the specified node location. If it finds one, it returns
     * the protocol stack associated to that Session.
     * 
     * @param nodeLocation
     *            The node location
     * 
     * @return The protocol stack associated to a Session whose end point equals
     *         the specified node location, or null if none is found.
     */
    public synchronized ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        Enumeration<Session> sessions = connections.keys();

        while (sessions.hasMoreElements()) {
            Session session = sessions.nextElement();

            if (session.getRemoteEnd().sameId(nodeLocation)) {
                return connections.get(session);
            }
        }

        return null;
    }

    /**
     * Searches, among the registered sessions, for a session whose local end is
     * the same of the specified node location. If it finds one, it returns
     * true.
     * 
     * @param nodeLocation
     *            The node location
     * 
     * @return true if the specified id represents a local end.
     */
    public synchronized boolean isLocal(NodeLocation nodeLocation) {
        Enumeration<Session> sessions = connections.keys();

        while (sessions.hasMoreElements()) {
            Session session = sessions.nextElement();

            if (session.getLocalEnd().sameId(nodeLocation)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an Enumeration of all the ProtocolStacks that are contained here.
     * 
     * @return Enumeration of all the ProtocolStacks that are contained here.
     */
    public synchronized Enumeration<ProtocolStack> getStacks() {
        return connections.elements();
    }

    /**
     * Closes all the Sessions and SessionStarters that deal with the specified
     * SessionId.
     * 
     * @param sessionId
     */
    public synchronized void closeSessions(SessionId sessionId) {
        /* first, all the involved sessions */
        Enumeration<Session> sessions = connections.keys();

        while (sessions.hasMoreElements()) {
            Session session = sessions.nextElement();
            if (session.containsSessionId(sessionId)) {
                try {
                    session.close();
                } catch (ProtocolException e) {
                    /* ignore it: we have to go on */
                }
                removeSession(session);
            }
        }
        
        /* then all the involved SessionStarters */
        sessionStarters.closeSessions(sessionId);
    }

    /**
     * The number of stored Sessions.
     * 
     * @see java.util.Hashtable#size()
     */
    public int sessionSize() {
        return connections.size();
    }
}
