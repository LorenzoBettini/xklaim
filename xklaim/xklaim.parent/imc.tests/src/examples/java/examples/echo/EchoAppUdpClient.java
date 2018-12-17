/*
 * Created on Jan 20, 2005
 *
 */
package examples.echo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.Node;

/**
 * Simple client application using CONNECT/DISCONNECT protocol states and
 * echo protocol state.  This relies on UDP.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EchoAppUdpClient {
	/**
	 * @author Lorenzo Bettini
	 * @version $Revision: 1.1 $
	 */
	public class SendStringState extends ProtocolStateSimple {
		
		/* (non-Javadoc)
		 * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object, org.mikado.imc.protocols.UnMarshaler)
		 */
		public void enter(Object param, TransmissionChannel transmissionChannel)
				throws ProtocolException {
			Marshaler marshaler = createMarshaler();
			String tosend = "HELLO";
			try {
				System.out.println("sending string: " + tosend);
				marshaler.writeStringLine(tosend);
				releaseMarshaler(marshaler);
				System.out.println("waiting for response...");
				UnMarshaler unMarshaler2 = createUnMarshaler();
				String received;
				received = unMarshaler2.readStringLine();
				System.out.println("received: " + received);
			} catch (IOException e) {
				throw new ProtocolException(e);
			}
		}
}
	
    protected Node node;
    protected int port;
    
    /**
     * Creates a new EchoApp object.
     */
    public EchoAppUdpClient(int port) {
        this.port = port;
        node = new Node();
    }
    
    public void start() throws ProtocolException {
    	Protocol protocol = new Protocol(new SendStringState());
        try {
            node.connect(new IpSessionId("localhost", 9999, "udp"), protocol);
        } catch (UnknownHostException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Starts the application on port 9999.
     *
     * @param args 
     * @throws ProtocolException
     */
    public static void main(String[] args) throws ProtocolException {
        EchoAppUdpClient echoApp = new EchoAppUdpClient(9999);
        echoApp.start();
    }
}
