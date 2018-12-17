/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

import org.mikado.imc.protocols.Session;


/**
 * An event related to a Session (Session request, established, etc.)
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class SessionEvent extends Event {
    /** Type of event. */
    public SessionEventType type;

    /** Session of the event. */
    protected Session session;
    
    /**
     * request types are not used for the moment.
     */
    public enum SessionEventType {
        CONNECTION_REQUEST, CONNECTION, DISCONNECTION_REQUEST, DISCONNECTION
    }

    /**
     * Constructs a SessionEvent.
     *
     * @param source Source of event.
     * @param type Type of event.
     * @param session Session of the connected/disconnected node.
     */
    public SessionEvent(Object source, SessionEventType type, Session session) {
        super(source);
        this.type = type;
        this.session = session;
    }

    /**
     * Returns the session.
     *
     * @return Returns the session.
     */
    public final Session getSession() {
        return session;
    }

    /**
     * Returns a string with information about the session.
     *
     * @return A string with information about the session.
     */
    public String toString() {
        return type + ": " + getSession().toString();
    }

    /**
     * @return Returns the type.
     */
    public SessionEventType getType() {
        return type;
    }
}
