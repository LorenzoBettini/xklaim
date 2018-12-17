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
 * @version $Revision: 1.2 $
 * 
 * A generic server that is independent from the low level communication layer.
 * This server accepts only one session.
 * 
 * It uses bindForAccept.
 */
public class GenericServerBind {
    public GenericServerBind(String protoId, String sessionIdentifier) throws ProtocolException, IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionStarter sessionStarter = sessionStarterTable.createSessionStarter(protoId);
        SessionId sessionId = null;
        System.out.println("specified session identifier: " + sessionIdentifier);
        if (sessionIdentifier == null)
            sessionId = sessionStarter.bindForAccept(null);
        else
            sessionId = 
                sessionStarter.bindForAccept(SessionId.parseSessionId(sessionIdentifier));
        System.out.println("accepting session on " + sessionId + " ...");
        ProtocolStack protocolStack = new ProtocolStack();
        Session session = protocolStack.accept(sessionStarter);
        System.out.println("established session " + session);

        while (true) {
            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            System.out.println("read line: " + unMarshaler.readStringLine());
        }
    }

    public static void main(String[] args) throws Exception {
        String protoId = "tcp";
        String sessionId = null;

        if (args.length > 0)
            protoId = args[0];
        if (args.length > 1)
            sessionId = protoId + SessionId.PROTO_SEPARATOR + args[1];

        new GenericServerBind(protoId, sessionId);
    }
}
