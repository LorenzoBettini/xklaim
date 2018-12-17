/*
 * Created on Jan 19, 2005
 *
 */
package examples.linda;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.topology.AcceptNodeCoordinator;
import org.mikado.imc.topology.Node;

public class LindaApp {
    protected Node node;
    protected TupleSpace tupleSpace = new TupleSpace(); // the share tuple space
    protected int port; // listening port
    
    public LindaApp(int port) {
        this.port = port;
        node = new Node();
    }
    
    public void start() throws IMCException {
        //node.accept(port, new Protocol(new LindaProtocolState(tupleSpace)));
        node.addNodeCoordinator(new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() {
                        return new Protocol(new LindaProtocolState(tupleSpace));
                    }
                }, new IpSessionId(port)));
    }

    public static void main(String[] args) throws IMCException {
        System.out.println("LindaApp started");
        LindaApp lindaApp = new LindaApp(9999);
        lindaApp.start();
    }
}
