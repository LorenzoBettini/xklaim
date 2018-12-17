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
 * Simple server application using CONNECT/DISCONNECT protocol states and
 * echo protocol state.  This relies on UDP.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EchoAppUdp {
    protected Node node;
    protected int port;
    
    /**
     * Creates a new EchoApp object.
     */
    public EchoAppUdp(int port) {
        this.port = port;
        node = new Node();
    }
    
    public void start() throws IMCException {
        node.addNodeCoordinator(new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() {
                        return new Protocol(new EchoProtocolState(Protocol.END));
                    }
                }, new IpSessionId(port, "udp")));
    }

    /**
     * Starts the application on port 9999.
     *
     * @param args 
     * @throws IMCException 
     */
    public static void main(String[] args) throws IMCException {
        EchoAppUdp echoApp = new EchoAppUdp(9999);
        echoApp.start();
    }
}
