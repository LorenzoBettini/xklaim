/*
 * Created on Apr 14, 2005
 */
package org.mikado.imc.protocols;

/**
 * Either for accepting an incoming session request or for establishing a
 * session (but not for both).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public abstract class SessionStarter {
    /**
     * The local session identifier.
     */
    protected SessionId localSessionId;

    /**
     * The remote session identifier.
     */
    protected SessionId remoteSessionId;

    /**
     * Whether some one already closed this SessionStarter.
     */
    private boolean closed = false;

    /**
     * Whether this starter is used for accept.
     */
    protected boolean usedForAccept = false;

    /**
     * Whether this starter is used for connect.
     */
    protected boolean usedForConnect = false;

    /**
     * A SessionStarter should be used either for accepting a Session request or
     * for establishing a Session. Thus only one of the two SessionIds must be
     * set (not both).
     * 
     * Thus, one of the passed parameters must be null.
     * 
     * @param localSessionId
     *            The local session identifier.
     * @param remoteSessionId
     *            The remote session identifier.
     */
    public SessionStarter(SessionId localSessionId, SessionId remoteSessionId) {
        this.localSessionId = localSessionId;
        this.remoteSessionId = remoteSessionId;
    }

    /**
     * 
     */
    public SessionStarter() {
    }

    /**
     * Accepts an incoming session.
     * 
     * @return The established session.
     * @throws ProtocolException
     */
    public abstract Session accept() throws ProtocolException;

    /**
     * Establishes a session.
     * 
     * @return The established session.
     * @throws ProtocolException
     */
    public abstract Session connect() throws ProtocolException;

    /**
     * Closes this starter, but not sessions created through this starter.
     * 
     * @throws ProtocolException
     */
    protected abstract void doClose() throws ProtocolException;

    /**
     * Closes this starter, but not sessions created through this starter. This
     * simply sets the status of this starter as closed, and then relies on the
     * doClose method that must be implemented in the subclasses.
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        closed = true;
        doClose();
    }

    /**
     * If this starter is closed, then throws an exception.
     * 
     * Otherwise it simply returns.
     * 
     * @throws ProtocolException
     */
    protected void checkClosed() throws ProtocolException {
        if (closed)
            throw new ProtocolException("SessionStarter closed");
    }

    /**
     * Binds this SessionStarter to the specified SessionId for accept. It then
     * returns the SessionId after binding (this may be different from the
     * passed one due to some transformation, e.g., name resolution for IP
     * addresses).
     * 
     * The specified SessionId can be null; in that case, the SessionStarter
     * must be able to create a fresh SessionId for binding.
     * 
     * @param sessionId
     * @return the SessionId after binding
     * @throws ProtocolException
     */
    public abstract SessionId bindForAccept(SessionId sessionId)
            throws ProtocolException;

    /**
     * Returns the SessionId associated with this SessionStarter
     * 
     * @return the SessionId associated with this SessionStarter
     */
    public SessionId getLocalSessionId() {
        return localSessionId;
    }

    /**
     * @param sessionId
     *            The sessionId to set.
     */
    public void setLocalSessionId(SessionId sessionId) {
        this.localSessionId = sessionId;
    }

    /**
     * Check that the local SessionId is set (otherwise throws an exception)
     * 
     * @throws UnboundSessionStarterException
     */
    protected void checkLocalSessionId() throws UnboundSessionStarterException {
        if (getLocalSessionId() == null)
            throw new UnboundSessionStarterException(
                    "unspecified local session identifier");
    }

    /**
     * Check that the remote SessionId is set (otherwise throws an exception)
     * 
     * @throws UnboundSessionStarterException
     */
    protected void checkRemoteSessionId() throws UnboundSessionStarterException {
        if (getRemoteSessionId() == null)
            throw new UnboundSessionStarterException(
                    "unspecified remote session identifier");
    }

    /**
     * @return Returns the remoteSessionId.
     */
    public SessionId getRemoteSessionId() {
        return remoteSessionId;
    }

    /**
     * @param sessionId
     * @return Whether the passed SessionId is either the local or the remote
     *         session id of this SessionStarter.
     */
    public boolean containsSessionId(SessionId sessionId) {
        return ((localSessionId != null && localSessionId.equals(sessionId)) || (remoteSessionId != null && remoteSessionId
                .equals(sessionId)));
    }

    /**
     * @param remoteSessionId
     *            The remoteSessionId to set.
     */
    public void setRemoteSessionId(SessionId remoteSessionId) {
        this.remoteSessionId = remoteSessionId;
    }

    /**
     * @return Returns the closed.
     */
    public boolean isClosed() {
        return closed;
    }
}
