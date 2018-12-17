/*
 * Created on May 24, 2005
 */
package examples.topology;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.topology.ConnectState;

import examples.protocol.EchoClientState;

/**
 * A client that uses ConnectState for establishing a connection.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ConnectDisconnectClient {
    public static void main(String[] args) throws Exception {
        SessionStarter sessionStarter = new TcpSessionStarter(null,
                new IpSessionId("localhost", 9999));
        Protocol protocol = new Protocol();
        protocol.setState(Protocol.START, new ConnectState("ECHO"));
        EchoClientState echoClientState = new EchoClientState(
                new IMCUnMarshaler(System.in), new IMCMarshaler(System.out));
        echoClientState.setNextState(Protocol.END);
        protocol.setState("ECHO", echoClientState);
        ConnectState disconnect = new ConnectState();
        disconnect.setDoConnect(false); // used for disconnection
        protocol.setState(Protocol.END, disconnect);

        System.out.println("establishing connection...");
        Session session = protocol.connect(sessionStarter);
        System.out.println("established session: " + session);

        protocol.start();

        System.out.println("protocol terminated");
    }
}
