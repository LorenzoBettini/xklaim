/*
 * Created on Jan 8, 2005
 *
 */
package imctests.protocols;

import junit.framework.TestCase;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.LinePrinterProtocolLayer;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStackThread;
import org.mikado.imc.protocols.ProtocolState;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.tcp.TcpIpProtocolLayer;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.net.Socket;


/**
 * Tests the TcpIpProtocolLayer
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class TcpIpLayerTest extends TestCase {
    /** The thread listening for incoming connections */
    ServerThread serverThread;

    /**
     * The protocol state used by the server socket thread to read from the
     * incoming connection.
     */
    ProtocolState protocolState;

    /**
     * This stream is connected through a pipe to myout. It is used to read
     * lines written by the LinePrinterProtocolLayer
     */
    DataInputStream myin;

    /**
     * This stream is connected through a pipe to myin. It is used by the
     * LinePrinterProtocolLayer to write received lines.
     */
    DataOutputStream myout;

    /**
     * Constructor for TcpIpLayerTest.
     *
     * @param arg0
     */
    public TcpIpLayerTest(String arg0) {
        super(arg0);
    }

    /**
     * Initializes the pipe that connects myin and myout.
     *
     * @see TestCase#setUp()
     */
    protected synchronized void setUp() throws Exception {
        super.setUp();
        serverThread = null;

        PipedInputStream piped_in = new PipedInputStream();
        PipedOutputStream piped_out = new PipedOutputStream();

        piped_in.connect(piped_out);

        myout = new DataOutputStream(piped_out);
        myin = new DataInputStream(new BufferedInputStream(piped_in));
    }

    /**
     * If the serverSocketThread is active, it shuts it down
     *
     * @see TestCase#tearDown()
     */
    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        System.out.println("*** tear down ***");

        Thread.sleep(500);
    }

	/**
	 * Given a stack, connects it to the specified session identifier.
	 * 
	 * @param protocolStack
	 * @param sessionId
	 * @throws ProtocolException
	 */
	void connect_stack(ProtocolStack protocolStack, IpSessionId sessionId) throws ProtocolException {
		SessionStarter sessionStarter = new TcpSessionStarter(null, sessionId);
		protocolStack.connect(sessionStarter);
	}
	
    /**
     * Sends a line through the passed stack, then reads it back and compares
     * the two strings (that must be equal).
     *
     * @param stack The protocol layer used to send the string
     * @param s The string to send
     *
     * @throws ProtocolException
     * @throws IOException
     */
    public void send_and_receive(ProtocolStack stack, String s)
        throws ProtocolException, IOException {
        String sent = s + "\n";
        Marshaler marshaler = stack.createMarshaler();
        marshaler.writeBytes(sent);
        stack.releaseMarshaler(marshaler);
        System.out.print("send string: " + sent);
        stack.createUnMarshaler();

        String received = EchoStateTest.readLine(myin) + "\n";
        System.out.print("received string: " + received);
        assertEquals(sent, received);
    }

    /**
     * Tests the EchoProtocolState through a TCP/IP connection. The established
     * through a TcpIpConnection.
     *
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void testTCPEchoLayer() throws ProtocolException, IOException, InterruptedException {
        protocolState = new EchoProtocolState();
        serverThread = new ServerThread(9999);
        serverThread.start();

        Thread.sleep(500);
        ProtocolStack protocolStack = new ProtocolStack();
        LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer(myout);
        protocolStack.insertLayer(linePrinterProtocolLayer);
        connect_stack(protocolStack, new IpSessionId("localhost", 9999));
        send_and_receive(protocolStack, "Hello");
        send_and_receive(protocolStack, EchoStateTest.longString());
        protocolStack.close();
        serverThread.close();
    }
    
    /**
     * Tests the EchoProtocolState through a TCP/IP connection. The socket is
     * opened explicitly.
     *
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void testTCPEchoLayerManual() throws ProtocolException, IOException, InterruptedException {
        protocolState = new EchoProtocolState();
        ProtocolStack protocol = new ProtocolStack();
        protocol.insertLayer(new ProtocolLayerEndPoint());
        serverThread = new ServerThread(9999);
        serverThread.start();

        Thread.sleep(500);
        LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer(myout);
        ProtocolStack receiver = new ProtocolStack(linePrinterProtocolLayer);
        Socket socket = new Socket("localhost", 9999);
        receiver.setLowLayer(new TcpIpProtocolLayer(socket));
        send_and_receive(receiver, "Hello");
        send_and_receive(receiver, EchoStateTest.longString());
        receiver.close();
        serverThread.close();
    }

    /**
     * Sends a line through the passed protocol stack, then reads it back and
     * compares the two strings (that must be equal).
     *
     * @param protocol The protocol stack used to send the string
     * @param s The string to send
     *
     * @throws ProtocolException
     * @throws IOException
     */
    public synchronized void send_and_receive2(ProtocolStack protocol, String s)
        throws ProtocolException, IOException {
        String sent = s + "\n";
        Marshaler marshaler = protocol.createMarshaler();
        marshaler.writeBytes(sent);
        protocol.releaseMarshaler(marshaler);
        System.out.print("send string: " + sent);
        protocol.createUnMarshaler();

        String received = EchoStateTest.readLine(myin) + "\n";
        System.out.print("received string: " + received);
        assertEquals(sent, received);
    }

    /**
     * Tests the EchoProtocolState through a TCP/IP connection. The established
     * through a TcpIpConnection.  
     *
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void testTCPEchoLayer2() throws ProtocolException, IOException, InterruptedException {
        protocolState = new EchoProtocolState();
        serverThread = new ServerThread(9999);
        serverThread.start();

        Thread.sleep(500);
        LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer(myout);
        ProtocolStack receiver = new ProtocolStack(linePrinterProtocolLayer);
        connect_stack(receiver, new IpSessionId("localhost", 9999));
        send_and_receive2(receiver, "Hello");
        send_and_receive2(receiver, EchoStateTest.longString());
        receiver.close();
        serverThread.close();
    }

    /**
     * Sends a line through the passed stack, then reads it back and compares
     * the two strings (that must be equal). The strings received back are not
     * read explicitly through a stack (which is used only to send the
     * strings) but through an UnMarshaler.
     *
     * @param stack The stack used to send the strings
     * @param s The string to send
     * @param unMarshaler The UnMarshaler used to read the strings sent back.
     *
     * @throws ProtocolException
     * @throws IOException
     */
    public void send_and_receive_thread(ProtocolStack stack, String s,
        UnMarshaler unMarshaler) throws ProtocolException, IOException {
        String sent = s + "\n";
        Marshaler marshaler = stack.createMarshaler();
        marshaler.writeBytes(sent);
        stack.releaseMarshaler(marshaler);
        System.out.print("send string: " + sent);

        String received = unMarshaler.readStringLine() + "\n";
        System.out.print("received string: " + received);
        assertEquals(sent, received);
    }

    /**
     * Tests the EchoProtocolState through a TCP/IP connection. The established
     * through a TcpIpConnection. Strings sent back are read by a
     * ProtocolLayerThread, which uses a LinePrinterProtocolLayer.  The
     * LinePrinterProtocolLayer writes the received strings into a stream that
     * is connected through a pipe to a local UnMarshaler.
     *
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void testTCPEchoLayerThread() throws ProtocolException, IOException, InterruptedException {
        protocolState = new EchoProtocolState();
        serverThread = new ServerThread(9999);
        serverThread.start();

        Thread.sleep(500);
        PipedOutputStream pipe_out = new PipedOutputStream();
        DataOutputStream local_out = new DataOutputStream(pipe_out);
        PipedInputStream pipe_in = new PipedInputStream(pipe_out);
        UnMarshaler unMarshaler = new IMCUnMarshaler(pipe_in);
        LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer(local_out);

        ProtocolStack protocolStack = new ProtocolStack(linePrinterProtocolLayer);
        connect_stack(protocolStack, new IpSessionId("localhost", 9999));

        ProtocolStackThread thread = new ProtocolStackThread(protocolStack);
        thread.start();
        send_and_receive_thread(protocolStack, "Hello", unMarshaler);
        send_and_receive_thread(protocolStack,
            EchoStateTest.longString(), unMarshaler);
        protocolStack.close();
        serverThread.close();
    }

    /**
     * A thread listening for an incoming connection and then reading incoming
     * string lines.
     *
     * @author bettini
     */
    public class ServerThread extends Thread {
        /** The TcpIpServer used to wait for an incoming connection */
        ProtocolLayer server;
		
		/**
		 * The SessionStarter used to accept new sessions.
		 */
		SessionStarter sessionStarter;

        /**
         * Creates a new ServerSocketThread object.
         *
         * @param port The port on which it listens for incoming connections.
         *
         * @throws ProtocolException
         */
        public ServerThread(int port) throws ProtocolException {
        }

        /**
         * run method
         */
        public void run() {
            System.out.println("listening for connections...");

            try {
				IpSessionId ipSessionId = new IpSessionId(9999);
				sessionStarter = new TcpSessionStarter(ipSessionId, null);
				Session startedSession = sessionStarter.accept(); 
				server = startedSession.getProtocolLayer();
            	ProtocolStack protocolStack = new ProtocolStack(server);
                protocolState.setProtocolStack(protocolStack);
                System.out.println("received connection from " + startedSession);
                
                while (true)

                    try {
                        protocolState.enter(null, null);
                    } catch (ProtocolException e1) {
                        e1.printStackTrace();

                        return;
                    }
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }

        /**
         * close the incoming connection.
         *
         * @throws ProtocolException DOCUMENT ME!
         */
        public void close() throws ProtocolException {
        	if (server != null) {
				server.doClose();
        	}
        	            
			if (sessionStarter != null) {
				sessionStarter.close();
			}
        }
    }
}
