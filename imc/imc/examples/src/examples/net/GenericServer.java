/*
 * Created on Mar 14, 2005
 */
package examples.net;

import java.io.IOException;

import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 * A generic server that is independent from the low level communication layer.
 * This server accepts only one session.
 */
public class GenericServer {
    public GenericServer(String id) throws ProtocolException, IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionId sessionId = SessionId.parseSessionId(id);
        System.out.println("accepting session " + sessionId + " ...");
        ProtocolStack protocolStack = new ProtocolStack();
        Session session = protocolStack.accept(sessionStarterTable
                .createSessionStarter(sessionId, null));
        System.out.println("established session " + session);

        while (true) {
            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            System.out.println("read line: " + unMarshaler.readStringLine());
        }
    }

    public static void main(String[] args) throws Exception {
        String id = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";

        if (args.length > 0)
            id = args[0];

        new GenericServer(id);
    }
}
