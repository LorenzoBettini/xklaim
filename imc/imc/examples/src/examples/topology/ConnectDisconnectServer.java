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

/**
 * A server that uses ConnectionManagementState for accepting a connection.
 * However, since no SessionManager is used to keep track of sessions, the final
 * state will accept also a CONNECT string (which is wrong).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ConnectDisconnectServer {
    public static void main(String[] args) throws Exception {
        SessionStarter sessionStarter = new TcpSessionStarter(new IpSessionId(
                "localhost", 9999), null);
        Protocol protocol = new Protocol();
        protocol
                .setState(Protocol.START, new ConnectionManagementState("ECHO"));
        protocol.setState("ECHO", new EchoProtocolState(Protocol.END));
        protocol.setState(Protocol.END, new ConnectionManagementState());

        System.out.println("waiting for connections...");
        Session session = protocol.accept(sessionStarter);
        System.out.println("established session: " + session);

        protocol.start();

        System.out.println("protocol terminated");
    }

}
