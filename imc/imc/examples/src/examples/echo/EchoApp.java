/*
 * Created on Jan 20, 2005
 *
 */
package examples.echo;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.topology.AcceptNodeCoordinator;
import org.mikado.imc.topology.Node;

/**
 * Simple application using CONNECT/DISCONNECT protocol states and echo protocol
 * state.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EchoApp {
    protected Node node;

    protected int port;

    /**
     * Creates a new EchoApp object.
     */
    public EchoApp(int port) {
        this.port = port;
        node = new Node();
    }

    public void start() throws IMCException {
        node.addNodeCoordinator(new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() {
                        return new Protocol(new EchoProtocolState(Protocol.END));
                    }
                }, new IpSessionId(port)));
    }

    /**
     * Starts the application on port 9999.
     * 
     * @param args
     * @throws IMCException
     */
    public static void main(String[] args) throws IMCException {
        EchoApp echoApp = new EchoApp(9999);
        echoApp.start();
    }
}
