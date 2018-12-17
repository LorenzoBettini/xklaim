/*
 * Created on Mar 14, 2005
 */
package examples.tcp;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 * Accepts a TCP session by using a TcpSessionStarter.
 */
public class TcpServer {

    public static void main(String[] args) throws Exception {
        int port = 9999;

        if (args.length > 0)
            port = Integer.parseInt(args[0]);

        ProtocolStack protocolStack = new ProtocolStack();

        System.out.println("accepting connections on port " + port);

        SessionStarter sessionStarter = new TcpSessionStarter(null,
                new IpSessionId(port));
        Session session = protocolStack.accept(sessionStarter);

        System.out.println("established session " + session);
        sessionStarter.close();
        // no more accepting sessions, but the established session is still up.

        UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
        while (true) {
            System.out.println("read line: " + unMarshaler.readStringLine());
        }
    }
}
