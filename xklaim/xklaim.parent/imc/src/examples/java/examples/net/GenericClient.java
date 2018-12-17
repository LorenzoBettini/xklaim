/*
 * Created on Mar 14, 2005
 */
package examples.net;

import java.io.IOException;

import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 * 
 * A generic client that is independent from the low level communication layer.
 */
public class GenericClient {
    public GenericClient(String host) throws ProtocolException, IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionId sessionId = SessionId.parseSessionId(host);
        System.out.println("creating session " + sessionId + " ...");
        ProtocolStack protocolStack = new ProtocolStack();
        Session session = protocolStack.connect(sessionStarterTable
                .createSessionStarter(null, sessionId));
        System.out.println("established session " + session);

        UnMarshaler console = new IMCUnMarshaler(System.in);
        while (true) {
            System.out.print("insert line: ");
            String line = console.readStringLine();
            Marshaler marshaler = protocolStack.createMarshaler();
            marshaler.writeStringLine(line);
            protocolStack.releaseMarshaler(marshaler);
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";

        if (args.length > 0)
            host = args[0];

        new GenericClient(host);
    }
}
