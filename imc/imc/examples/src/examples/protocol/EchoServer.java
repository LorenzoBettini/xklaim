/*
 * Created on Mar 16, 2005
 */
package examples.protocol;

import java.io.IOException;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 * 
 * Echoes the received line. 
 */
public class EchoServer {
	public EchoServer(String host) throws ProtocolException, IOException {
		Protocol protocol = new Protocol(new EchoProtocolState(Protocol.END));
		new GenericServer(host, protocol);
	}

	public static void main(String[] args) throws Exception {
		String host = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";
		
		if (args.length > 0)
			host = args[0];
		
		new EchoServer(host);
	}
}
