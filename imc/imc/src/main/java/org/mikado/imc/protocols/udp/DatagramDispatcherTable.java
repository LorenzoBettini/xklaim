/*
 * Created on Mar 2, 2006
 */
package org.mikado.imc.protocols.udp;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionIdBindException;
import org.mikado.imc.protocols.SessionIdException;

/**
 * Associates SessionId to DatagramDispatcher
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class DatagramDispatcherTable {
    /**
     * The sets of the SessionIds that are bound for accepts.
     * 
     * Notice that a DatagramDispatcher may be associated to a SessionId but the
     * same SessionId is not in this set: this means that a Session was accepted
     * with that SessionId, and now this SessionId is free for being reused for
     * further accepts.
     */
    protected Set<SessionId> boundSessionIds = Collections
            .synchronizedSet(new TreeSet<SessionId>());

    /**
     * The actual table that associates a SessionId to a DatagramDispatcher
     */
    protected Map<SessionId, DatagramDispatcher> table = Collections
            .synchronizedMap(new HashMap<SessionId, DatagramDispatcher>());

    /**
     * Returns the DatagramDispatcher associated to the specified SessionId.
     * 
     * Such DatagramDispatcher will be used for accepting new connection
     * requests.
     * 
     * If there's no such association, creates a DatagramSocket with that
     * SessionId and associates a brand new DatagramDispatcher (that is also
     * started).
     * 
     * @param sessionId
     * @return the DatagramDispatcher associated to the specified SessionId
     * @throws ProtocolException
     */
    public DatagramDispatcher getDispatcherForAccept(
            SessionId sessionId) throws ProtocolException {
        /*
         * in this case there's already a DatagramDispatcher for this SessionId
         */
        if (sessionId != null) {
            IpSessionId ipSessionId = getIpSessionId(sessionId);

            DatagramDispatcher datagramDispatcher = table.get(ipSessionId);

            if (datagramDispatcher != null) {
                if (!datagramDispatcher.isNotAccepting()) {
                    /*
                     * there's already a DatagramDispatcher used for accepting,
                     * then we must raise an exception
                     */
                    throw new SessionIdBindException("already accepting on "
                            + sessionId + "(" + ipSessionId + ")");
                }

                /*
                 * otherwise sets this dispatcher for accepting (since it was
                 * not accepting anymore
                 */
                datagramDispatcher.startAccept();

                return datagramDispatcher;
            }
        }

        DatagramSocket datagramSocket = null;

        try {
            if (sessionId != null) {
                InetSocketAddress inetSocketAddress = IpSessionId
                        .parseAddress(sessionId.toString());

                try {
                    datagramSocket = new DatagramSocket(inetSocketAddress);
                } catch (BindException e) {
                    throw new SessionIdBindException(sessionId);
                }
            } else {
                /* let the system choose a port */
                datagramSocket = new DatagramSocket();
            }

            /*
             * and reconstruct the session id (since it might have an empty
             * port)
             */
            sessionId = new IpSessionId(datagramSocket.getLocalAddress()
                    .getHostAddress(), datagramSocket.getLocalPort(), "udp");

            DatagramDispatcher datagramDispatcher = new DatagramDispatcher(
                    datagramSocket, sessionId, this);
            datagramDispatcher.start();
            table.put(sessionId, datagramDispatcher);

            return datagramDispatcher;
        } catch (UnknownHostException e) {
            throw new ProtocolException(e);
        } catch (SocketException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Returns the DatagramDispatcher associated to the specified SessionId or
     * null if there's no such association.
     * 
     * @param sessionId
     * @return the DatagramDispatcher associated to the specified SessionId
     * @throws SessionIdException
     */
    public DatagramDispatcher getDispatcher(SessionId sessionId)
            throws SessionIdException {
        /*
         * in this case there's already a DatagramDispatcher for this SessionId
         */
        if (sessionId != null) {
            IpSessionId ipSessionId = getIpSessionId(sessionId);

            return table.get(ipSessionId);
        }

        return null;
    }

    /**
     * Returns the DatagramDispatcher associated to the specified SessionId
     * (must not be null).
     * 
     * If there's no such association, creates a DatagramSocket with that
     * SessionId and associates a brand new DatagramDispatcher (that is also
     * started).
     * 
     * This method also binds the DatagramDispatcher to this SessionId; this
     * means that the same SessionId can not then be used for further accepts.
     * 
     * @param sessionId
     * @return the DatagramDispatcher associated to the specified SessionId
     * @throws ProtocolException
     */
    public DatagramDispatcher bindSessionId(SessionId sessionId)
            throws ProtocolException {
        if (sessionId == null)
            throw new ProtocolException("unspecified SessionId");

        if (boundSessionIds.contains(getIpSessionId(sessionId))) {
            throw new SessionIdBindException(sessionId);
        }

        DatagramDispatcher datagramDispatcher = getDispatcherForAccept(sessionId);

        /*
         * OK this id is free, so rely on getDispatcher, but we make sure to use
         * the actual SessionId of the dispatcher (logical name is translated
         * into IP address, so the SessionId might be different from the
         * original one
         */
        boundSessionIds.add(datagramDispatcher.getSessionId());

        return datagramDispatcher;
    }

    /**
     * Releases this dispatcher for accepts, thus it also unbinds the associated
     * SessionId.
     * 
     * Notice however, that the dispatcher is still running for the Sessions
     * that had already been established, thus it is still associated with the
     * SessionId.
     * 
     * @param datagramDispatcher
     * @throws ProtocolException
     */
    public void releaseDispatcher(
            DatagramDispatcher datagramDispatcher) throws ProtocolException {
        /* unbinds the SessionId */
        boundSessionIds.remove(datagramDispatcher.getSessionId());

        datagramDispatcher.stopAccept();

        System.out.println("*** released dispatcher for "
                + datagramDispatcher.getSessionId());
    }

    /**
     * Removes the association between a SessionId and a DatagramDispatcher.
     * 
     * This basically means that the underlying DatagramSocket is actually
     * closed.
     * 
     * @param datagramDispatcher
     */
    public void removeDispatcher(
            DatagramDispatcher datagramDispatcher) {
        /* unbinds the SessionId */
        boundSessionIds.remove(datagramDispatcher.getSessionId());

        table.remove(datagramDispatcher.getSessionId());

        System.out.println("### removed dispatcher for "
                + datagramDispatcher.getSessionId());
    }

    /**
     * Translate the passed SessionId into an IpSessionId
     * 
     * @param sessionId
     * @return the translated IpSessionId
     * @throws SessionIdException
     */
    static IpSessionId getIpSessionId(SessionId sessionId)
            throws SessionIdException {
        return IpSessionId.parseSessionId(sessionId.toString());
    }
}