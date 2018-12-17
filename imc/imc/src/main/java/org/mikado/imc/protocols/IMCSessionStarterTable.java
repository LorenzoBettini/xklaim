/*
 * Created on May 2, 2005
 */
package org.mikado.imc.protocols;

import org.mikado.imc.protocols.pipe.LocalSessionStarter;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.protocols.udp.UdpSessionStarter;

/**
 * The default SessionStarterTable.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class IMCSessionStarterTable extends SessionStarterTable {

    /**
     * Builds a default SessionStarterTable with associations for tcp and udp
     */
    public IMCSessionStarterTable() {
        associateSessionStarterFactory("tcp", new SessionStarterFactory() {
            public SessionStarter createSessionStarter(SessionId localSessionId, SessionId remoteSessionId)
                    throws ProtocolException {
                return new TcpSessionStarter(localSessionId, remoteSessionId);
            }
        });
        associateSessionStarterFactory("udp", new SessionStarterFactory() {
            public SessionStarter createSessionStarter(SessionId localSessionId, SessionId remoteSessionId)
                    throws ProtocolException {
                return new UdpSessionStarter(localSessionId, remoteSessionId);
            }
        });
        associateSessionStarterFactory("pipe", new SessionStarterFactory() {
            public SessionStarter createSessionStarter(SessionId localSessionId, SessionId remoteSessionId)
                    throws ProtocolException {
                return new LocalSessionStarter(localSessionId, remoteSessionId);
            }
        });
    }

}
