/*
 * Created on Mar 14, 2005
 */
package examples.protocol;

import java.io.IOException;

import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.Session;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 * 
 * A generic server that is independent from the low level communication layer.
 * It also executes a passed protocol.
 */
public class GenericServer {
    public GenericServer(String host, Protocol protocol)
            throws ProtocolException, IOException {
        this(host, protocol, new ProtocolStack());
    }

    public GenericServer(String host, Protocol protocol,
            ProtocolStack protocolStack) throws ProtocolException, IOException {
        SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();
        SessionId sessionId = SessionId.parseSessionId(host);
        System.out.println("accepting session " + sessionId + " ...");
        Session session = protocolStack.accept(sessionStarterTable
                .createSessionStarter(sessionId, null));
        System.out.println("established session " + session);
        System.out.println("starting protocol... ");
        protocol.setProtocolStack(protocolStack);
        protocol.start();
        protocol.close();
        System.out.println("protocol terminated");
    }
}
