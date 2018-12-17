/*
 * Created on May 2, 2005
 */
package org.mikado.imc.protocols;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 * 
 * Abstract factory used to create a SessionStarter
 */
public interface SessionStarterFactory {
    /**
     * Creates a SessionStarter.
     * 
     * @param localSessionId
     *            The local session identifier.
     * @param remoteSessionId
     *            The remote session identifier.
     * @return the created SessionStarter
     * @throws ProtocolException
     */
    SessionStarter createSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) throws ProtocolException;
}
