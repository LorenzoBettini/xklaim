/*
 * Created on Apr 14, 2005
 */
package org.mikado.imc.protocols;

import java.util.HashMap;

/**
 * Associates to a connection protocol, such "TCP", "UDP" a specific
 * SessionStarterFactory. The identifier of a protocol is a string; the
 * comparison is made in non-sensitive way.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class SessionStarterTable {
    /**
     * the table storing the associations: key = string (e.g., "TCP") value =
     * the protocol layer factory for that connection protocol
     */
    protected HashMap<String, SessionStarterFactory> sessionStarterFactories = new HashMap<String, SessionStarterFactory>();

    /**
     * Returns the SessionStarterFactory associated to the specified string or
     * throws a ProtocolException if there's no association.
     * 
     * @param id
     * @return The SessionStarterFactory associated to the specified string or
     *         null if there's no association.
     */
    public SessionStarterFactory getSessionStarterFactory(String id)
            throws ProtocolException {
        SessionStarterFactory sessionStarterFactory = sessionStarterFactories
                .get(id.toUpperCase());

        if (sessionStarterFactory == null) {
            throw new ProtocolException("unregistered connection protocol: "
                    + id);
        }

        return sessionStarterFactory;
    }

    /**
     * Associates a SessionStarterFactory to the specified protocol id. If a
     * previous layer was associated with the specified id it is returned.
     * 
     * @param id
     * @param sessionStarterFactory
     * @return The SessionStarterFactory that was previously associated to the
     *         specified id.
     */
    public SessionStarterFactory associateSessionStarterFactory(String id,
            SessionStarterFactory sessionStarterFactory) {
        return sessionStarterFactories.put(id.toUpperCase(),
                sessionStarterFactory);
    }

    /**
     * Creates a SessionStarter by using the SessionStarterFactory associated to
     * the connection protocol of the specified SessionId or throws a
     * ProtocolException if there's no association.
     * 
     * @param localSessionId
     *            The local session identifier.
     * @param remoteSessionId
     *            The remote session identifier.
     * @return the created SessionStarter
     * @throws ProtocolException
     */
    public SessionStarter createSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) throws ProtocolException {
        if (localSessionId == null && remoteSessionId == null)
            throw new ProtocolException(
                    "at least one session identifier must be specified");

        if (localSessionId != null
                && remoteSessionId != null
                && !(localSessionId.getConnectionProtocolId()
                        .equals(remoteSessionId.getConnectionProtocolId())))
            throw new ProtocolException(
                    "using two different kinds of session ids: "
                            + localSessionId + " and " + remoteSessionId);

        String connProto = (localSessionId != null ? localSessionId
                .getConnectionProtocolId() : remoteSessionId
                .getConnectionProtocolId());

        return getSessionStarterFactory(connProto)
                .createSessionStarter(localSessionId, remoteSessionId);
    }

    /**
     * Creates a SessionStarter by using the SessionStarterFactory associated to
     * the specified connection protocol id or throws a ProtocolException if
     * there's no association.
     * 
     * @param connProtoId
     * @return the created SessionStarter
     * @throws ProtocolException
     */
    public SessionStarter createSessionStarter(String connProtoId)
            throws ProtocolException {
        return getSessionStarterFactory(connProtoId).createSessionStarter(null,
                null);
    }
}
