/*
 * Created on Jan 20, 2005
 *
 */
package examples.echo;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.HTTPTunnelProtocolLayer;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.topology.AcceptNodeCoordinator;
import org.mikado.imc.topology.Node;

/**
 * Simple application using CONNECT/DISCONNECT protocol states and
 * echo protocol state.  All this is tunneled through HTTP.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EchoHTTPApp {
    protected Node node;
    protected int port;
    
    /**
     * Creates a new EchoApp object.
     */
    public EchoHTTPApp(int port) {
        this.port = port;
        node = new Node();
    }
    
    public void start() throws IMCException {
        node.addNodeCoordinator(new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() {
                        Protocol protocol = new Protocol();
                        try {
                            protocol.getProtocolStack().insertLayer(new HTTPTunnelProtocolLayer());
                            protocol.setState(Protocol.START, new EchoProtocolState(Protocol.END));                            
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                            return null;
                        }
                        return protocol;
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
        EchoHTTPApp echoApp = new EchoHTTPApp(9999);
        echoApp.start();
    }
}
