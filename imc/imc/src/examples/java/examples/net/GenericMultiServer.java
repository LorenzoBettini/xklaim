/*
 * Created on Mar 14, 2005
 */
package examples.net;

import java.io.IOException;

import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 * A generic server that is independent from the low level communication layer.
 * This server accepts multiple sessions, and each session is then handled by a
 * dedicated thread.
 */
public class GenericMultiServer {
    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.3 $
     * 
     * Handles a session with a specific client.
     */
    public class ClientHandler extends Thread {
        private ProtocolStack protocolStack;

        ClientHandler(ProtocolStack protocolStack) {
            this.protocolStack = protocolStack;
        }

        public void run() {
            try {
                String session = protocolStack.getSession().toString();
                while (true) {
                    UnMarshaler unMarshaler;
                    unMarshaler = protocolStack.createUnMarshaler();
                    System.out.println("(" + session + ")" + " read line: "
                            + unMarshaler.readStringLine());

                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public GenericMultiServer(String id) throws ProtocolException,
            IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionId sessionId = SessionId.parseSessionId(id);
        System.out.println("accepting session " + sessionId + " ...");
        SessionStarter sessionStarter = sessionStarterTable
                .createSessionStarter(sessionId, null);

        while (true) {
            ProtocolStack protocolStack = new ProtocolStack();
            Session session = protocolStack.accept(sessionStarter);
            System.out.println("established session " + session);
            new ClientHandler(protocolStack).start();
        }
    }

    public static void main(String[] args) throws Exception {
        String id = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";

        if (args.length > 0)
            id = args[0];

        new GenericMultiServer(id);
    }
}
