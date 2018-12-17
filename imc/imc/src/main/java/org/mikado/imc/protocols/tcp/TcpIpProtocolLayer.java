/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols.tcp;

import java.io.IOException;

import java.net.Socket;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;

/**
 * This protocol layer wraps a TCP/IP session
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class TcpIpProtocolLayer extends ProtocolLayerEndPoint {
	/** The wrapped socket */
	protected Socket socket;

	/**
	 * Creates a new TcpIpProtocolLayer object.
	 */
	public TcpIpProtocolLayer() {
	}

	/**
	 * Creates a new TcpIpProtocolLayer object. It creates an UnMarshaler and
	 * Marshaler with the input and output stream of the passed socket.
	 * 
	 * @param sock
	 *            the Socket to wrap
	 * 
	 * @throws IOException
	 */
	public TcpIpProtocolLayer(Socket sock) throws IOException {
		super(new IMCUnMarshaler(sock.getInputStream()), new IMCMarshaler(sock
				.getOutputStream()));
		socket = sock;
		// TODO: create the unmarshaler and marshaler with an abstract factory?
	}

	/**
	 * Sets the socket.
	 * 
	 * @param socket
	 * 
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public void setSocket(Socket socket) throws IOException, ProtocolException {
		setUnmarshaler(new IMCUnMarshaler(socket.getInputStream()));
		setMarshaler(new IMCMarshaler(socket.getOutputStream()));
		this.socket = socket;
		// TODO: check that the socket is not already set?
	}

	/**
	 * Closes the wrapped socket.  If it was used to
	 * accept a connection, it also releases the associated
	 * server socket.
	 * 
	 * @throws ProtocolException
	 */
	public void doClose() throws ProtocolException {
		if (socket != null) {
			try {
				socket.close();			
			} catch (IOException e) {
				throw new ProtocolException(e);
			}
		}
	}

    /**
     * @return Returns the socket.
     */
    public final Socket getSocket() {
        return socket;
    }
}
