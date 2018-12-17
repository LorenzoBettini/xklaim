/*
 * Created on Jan 24, 2006
 */
package org.mikado.imc.protocols;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Stores SessionStarter references, so that it is possible to close all the
 * SessionStarters contained.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class SessionStarters {
    /**
     * The actual collection. Given a specific SessionId there may be more than
     * on SessionStarter associated to it
     */
    protected Hashtable<SessionId, Vector<SessionStarter>> sessionStarters = new Hashtable<SessionId, Vector<SessionStarter>>();

    /**
     * Whether we are already in closing mode
     */
    protected boolean closing = false;

    /**
     * Add a SessionStarter in this collection.
     * 
     * @param sessionStarter
     * 
     * @throws ProtocolException
     *             In case we are trying to add a SessionStarter while we're
     *             closing all the SessionStarters
     */
    synchronized public void addSessionStarter(SessionStarter sessionStarter)
            throws ProtocolException {
        if (closing)
            throw new ProtocolException("already closing or closed");

        Vector<SessionStarter> v = sessionStarters.get(sessionStarter
                .getLocalSessionId());
        if (v == null) {
            v = new Vector<SessionStarter>();
            sessionStarters.put(sessionStarter.getLocalSessionId(), v);
        }
        v.addElement(sessionStarter);
    }

    /**
     * Removes a SessionStarter from this collection and closes it. If it is not
     * found in the collection an exception is thrown.
     * 
     * @param sessionStarter
     *            The SessionStarter to remove and close.
     * 
     * @throws ProtocolException
     */
    synchronized public void removeAndCloseSessionStarter(
            SessionStarter sessionStarter) throws ProtocolException {
        /*
         * in this case there's no need to throw an exception, since we're
         * trying to remove a SessionStarter that will be removed in close
         * method anyway
         */
        if (closing)
            return;

        SessionId sessionId = sessionStarter.getLocalSessionId();
        Vector<SessionStarter> v = sessionStarters.get(sessionId);
        if (v == null) {
            throw new ProtocolException("SessionStarter not found");
        }

        /* we must find the exact reference */
        boolean found = false;
        for (int i = 0; i < v.size(); ++i) {
            if (v.get(i) == sessionStarter) {
                v.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new ProtocolException("SessionStarter not found");
        }

        /* also remove the vector if it is empty */
        if (v.size() == 0) {
            sessionStarters.remove(sessionId);
        }

        sessionStarter.close();
    }

    /**
     * Closes all the SessionStarters stored in this collection. Once this
     * method is called, this collection cannot be used anymore for adding new
     * SessionStarters.
     */
    synchronized public void close() {
        closing = true;

        Enumeration<Vector<SessionStarter>> vectors = sessionStarters
                .elements();
        while (vectors.hasMoreElements()) {
            Enumeration<SessionStarter> starters = vectors.nextElement()
                    .elements();
            while (starters.hasMoreElements()) {
                try {
                    starters.nextElement().close();
                } catch (ProtocolException e) {
                    /* we ignore this error and go on */
                }
            }
        }
    }

    /**
     * Closes all the SessionStarters that deal with the specified SessionId.
     * 
     * @param sessionId
     */
    public synchronized void closeSessions(SessionId sessionId) {
        Vector<SessionStarter> v = sessionStarters.get(sessionId);

        if (v == null)
            return;
        
        Enumeration<SessionStarter> starters = v.elements();
        while (starters.hasMoreElements()) {
            try {
                starters.nextElement().close();
            } catch (ProtocolException e) {
                /* we ignore this error and go on */
            }
        }
    }
}
