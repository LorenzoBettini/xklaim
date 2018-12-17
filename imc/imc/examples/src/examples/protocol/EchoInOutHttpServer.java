/*
 * Created on Mar 16, 2005
 */
package examples.protocol;

import java.io.IOException;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.HTTPTunnelProtocolLayer;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.PutGetProtocolLayer;
import org.mikado.imc.protocols.SessionId;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 * 
 * Echoes the received line.  Tunnels through HTTP 
 */
public class EchoInOutHttpServer {
	public EchoInOutHttpServer(String host) throws ProtocolException, IOException {
		Protocol protocol = new Protocol(new EchoProtocolState(Protocol.END));
        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.insertLayer(new PutGetProtocolLayer());
        protocolStack.insertLayer(new HTTPTunnelProtocolLayer());
		new GenericServer(host, protocol, protocolStack);
	}

	public static void main(String[] args) throws Exception {
		String host = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";
		
		if (args.length > 0)
			host = args[0];
		
		new EchoInOutHttpServer(host);
	}
}
