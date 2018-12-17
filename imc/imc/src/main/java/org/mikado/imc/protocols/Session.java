/*
 * Created on May 13, 2005
 */
package org.mikado.imc.protocols;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 * 
 * Represents a new started session.
 */
public class Session {
    /**
     * The ProtocolLayer handling this session.
     */
    protected ProtocolLayer protocolLayer;

    /**
     * The session id that identifies the remote host end
     */
    SessionId remote;

    /**
     * The session id that identifies the local host end
     */
    SessionId local;

    /** Whether this Session was closed. */
    private boolean closed = false;

    /**
     * @param protocolLayer
     * @param local
     * @param remote
     */
    public Session(ProtocolLayer protocolLayer, SessionId local,
            SessionId remote) {
        this.protocolLayer = protocolLayer;
        this.local = local;
        this.remote = remote;
    }

    /**
     * @return Returns the protocolLayer.
     */
    public final ProtocolLayer getProtocolLayer() {
        return protocolLayer;
    }

    /**
     * @return The identifier of the local end of this session.
     */
    public SessionId getLocalEnd() {
        return local;
    }

    /**
     * @return The identifier of the remote end of this session
     */
    public SessionId getRemoteEnd() {
        return remote;
    }

    /**
     * @param sessionId
     * @return Whether the passed SessionId is either the local or the remote
     *         session id of this Session.
     */
    public boolean containsSessionId(SessionId sessionId) {
        return ((local != null && local.equals(sessionId)) || (remote != null && remote
                .equals(sessionId)));
    }

    /**
     * Returns a string version of Session, i.e., local "->" remote
     * 
     * @return The string version of this IpSession
     */
    public String toString() {
        return local.toString() + "->" + remote.toString();
    }

    /**
     * Closes this section by closing the underlying ProtocolLayer.
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        setClosed(true);

        if (protocolLayer != null)
            protocolLayer.doClose();
    }

    /**
     * @return Returns the closed status.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed
     *            The closed status to set.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
