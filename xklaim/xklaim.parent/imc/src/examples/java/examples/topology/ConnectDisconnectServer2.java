/*
 * Created on May 24, 2005
 */
package examples.topology;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.topology.ConnectionManagementState;
import org.mikado.imc.topology.SessionManager;

/**
 * A server that uses ConnectionManagementState for accepting a connection. This
 * is the correct version, since the initial and the final state share the same
 * SessionManager.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ConnectDisconnectServer2 {
    public static void main(String[] args) throws Exception {
        SessionStarter sessionStarter = new TcpSessionStarter(new IpSessionId(
                "localhost", 9999), null);
        Protocol protocol = new Protocol();
        SessionManager sessionManager = new SessionManager();
        ConnectionManagementState initialState = new ConnectionManagementState(
                "ECHO");
        initialState.setSessionManager(sessionManager);
        protocol.setState(Protocol.START, initialState);
        protocol.setState("ECHO", new EchoProtocolState(Protocol.END));
        ConnectionManagementState finalState = new ConnectionManagementState();
        finalState.setSessionManager(sessionManager);
        protocol.setState(Protocol.END, finalState);

        System.out.println("waiting for connections...");
        Session session = protocol.accept(sessionStarter);
        System.out.println("established session: " + session);

        protocol.start();

        System.out.println("protocol terminated");
    }
}
