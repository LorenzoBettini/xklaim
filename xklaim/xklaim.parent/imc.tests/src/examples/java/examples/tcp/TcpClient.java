/*
 * Created on Mar 14, 2005
 */
package examples.tcp;

import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 * Starts a TCP session by using a TcpSessionStarter.
 */
public class TcpClient {

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 9999;

        if (args.length > 0)
            host = args[0];
        if (args.length > 1)
            port = Integer.parseInt(args[1]);

        UnMarshaler console = new IMCUnMarshaler(System.in);
        IpSessionId sessionId = new IpSessionId(host, port);
        System.out.println("opening connection to " + sessionId);
        ProtocolStack protocolStack = new ProtocolStack();
        Session session = protocolStack.connect(new TcpSessionStarter(null,
                sessionId));
        System.out.println("established session " + session);

        while (true) {
            System.out.print("insert line: ");
            String line = console.readStringLine();
            Marshaler marshaler = protocolStack.createMarshaler();
            marshaler.writeStringLine(line);
            protocolStack.releaseMarshaler(marshaler);
        }
    }
}
