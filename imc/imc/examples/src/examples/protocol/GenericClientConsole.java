/*
 * Created on Mar 24, 2005
 */
package examples.protocol;

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
 * Generic client console to communicates with a server.
 */
public class GenericClientConsole {
    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     * 
     * Reads bytes from a protocol stack.
     */
    public class ReceiverThread extends Thread {
        ProtocolStack protocolStack;

        /**
         * @param protocolStack
         */
        public ReceiverThread(ProtocolStack protocolStack) {
            this.protocolStack = protocolStack;
        }

        public void run() {
            try {
                while (true) {
                    UnMarshaler unMarshaler = protocolStack.createUnMarshaler();

                    while (unMarshaler.available() > 0) {
                        System.out.print((char) unMarshaler.readByte());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("lost connection with the server.");
                System.exit(0);
            }
        }
    }

    public GenericClientConsole(String host) throws ProtocolException,
            IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionId sessionId = SessionId.parseSessionId(host);
        System.out.println("creating session " + sessionId + " ...");
        ProtocolStack protocolStack = new ProtocolStack();
        Session session = protocolStack.connect(sessionStarterTable
                .createSessionStarter(null, sessionId));
        System.out.println("established session " + session);

        // start the thread that reads answers
        new ReceiverThread(protocolStack).start();

        UnMarshaler console = new IMCUnMarshaler(System.in);
        System.out
                .println("insert message terminated by a line containing only one .");

        while (true) {
            StringBuffer stringBuffer = new StringBuffer();

            while (true) {
                String line = console.readStringLine();
                if (line.equals("."))
                    break;
                stringBuffer.append(line + "\n");
            }
            Marshaler marshaler = protocolStack.createMarshaler();
            marshaler.writeBytes(stringBuffer.toString());
            protocolStack.releaseMarshaler(marshaler);
        }
    }

    public static void main(String[] args) throws ProtocolException,
            IOException {
        String host = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";

        if (args.length > 0)
            host = args[0];

        new GenericClientConsole(host);
    }
}
