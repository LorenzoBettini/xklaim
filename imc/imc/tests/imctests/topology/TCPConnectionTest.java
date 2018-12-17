/*
 * Created on Jan 4, 2005
 *
 */
package imctests.topology;

import junit.framework.TestCase;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.HTTPTunnelProtocolLayer;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.ConnectionServerThread;

import java.io.IOException;
import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Tests SessionManager and ConnectionServer
 * 
 * @author $author$
 * @version $Revision: 1.5 $
 */
public class TCPConnectionTest extends TestCase {
    /** The connection server. */
    ConnectionServerThread connectionServerThread;

    /** The connection manager used by the connection server. */
    SessionManager sessionManager;

    /**
     * @author bettini
     * 
     */
    public class EchoProtocolFactory implements ProtocolFactory {

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.newprotocols.ProtocolFactory#createProtocol()
         */
        public Protocol createProtocol() throws ProtocolException {
            Protocol protocol = new Protocol();
            protocol.setState(Protocol.START, new EchoProtocolState(
                    Protocol.END));
            return protocol;
        }

    }

    public class HTTPEchoProtocolFactory implements ProtocolFactory {

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.newprotocols.ProtocolFactory#createProtocol()
         */
        public Protocol createProtocol() throws ProtocolException {
            Protocol protocol = new Protocol();
            protocol.getProtocolStack().insertFirstLayer(
                    new HTTPTunnelProtocolLayer());
            protocol.setState(Protocol.START, new EchoProtocolState(
                    Protocol.END));
            return protocol;
        }

    }

    /**
     * Creates a new TCPConnectionTest object.
     */
    public TCPConnectionTest() {
    }

    /**
     * setUp
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        sessionManager = new SessionManager();
        connectionServerThread = new ConnectionServerThread(sessionManager,
                new EchoProtocolFactory(), new IpSessionId(9999));
        connectionServerThread.start();
        System.out.println("connection server started");
    }

    /**
     * tearDown
     * 
     * @throws Exception
     */
    public void tearDown() throws Exception {
        super.tearDown();
        connectionServerThread.close();
        System.out.println("connection server stopped");
        Thread.sleep(500);
    }

    /**
     * Sends a request through a protocol layer, waits for the answer and
     * compares the answer with the one expected.
     * 
     * @param req
     *            Request
     * @param layer
     *            ProtocolLayer used to send and receive
     * @param expect_ok
     *            Whether we expect OK or FAIL
     * 
     * @throws ProtocolException
     * @throws IOException
     */
    void send_request(String req, ProtocolLayer layer, boolean expect_ok)
            throws ProtocolException, IOException {
        Marshaler marshaler = layer.doCreateMarshaler(null);
        marshaler.writeStringLine(req);
        layer.doReleaseMarshaler(marshaler);
        System.out.println("sent: " + req);

        UnMarshaler unMarshaler = layer.doCreateUnMarshaler(null);
        String rec = unMarshaler.readStringLine();
        System.out.println("received: " + rec);

        if (expect_ok) {
            assertEquals("OK", rec);
        } else {
            assertEquals("FAIL", rec);
        }
    }

    /**
     * Sends some requests and tests the answers.
     * 
     * @param layer
     */
    void try_connect_and_disconnect_ok(ProtocolLayer layer) {
        try {
            send_request("CONNECT", layer, true);
            send_request("FAIL", layer, false); // we receive an echo so we
                                                // should receive FAIL
            send_request("DISCONNECT", layer, true);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }
    }

    /**
     * Sends some requests and tests the answers.
     * 
     * @param layer
     */
    void try_connect_and_disconnect_fail(ProtocolLayer layer) {
        try {
            send_request("connect", layer, false); // should be uppercase
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }
    }

    /**
     * Sends a request through a protocol stack, waits for the answer and
     * compares the answer with the one expected.
     * 
     * @param req
     *            Request
     * @param stack
     *            the ProtocolStack used to send and receive
     * @param expect_ok
     *            Whether we expect OK or FAIL
     * 
     * @throws ProtocolException
     * @throws IOException
     */
    void send_request(String req, ProtocolStack stack, boolean expect_ok)
            throws ProtocolException, IOException {
        Marshaler marshaler = stack.createMarshaler();
        marshaler.writeStringLine(req);
        stack.releaseMarshaler(marshaler);
        System.out.println("sent: " + req);

        UnMarshaler unMarshaler = stack.createUnMarshaler();
        String rec = unMarshaler.readStringLine();
        System.out.println("received: " + rec);

        if (expect_ok) {
            assertEquals("OK", rec);
        } else {
            assertEquals("FAIL", rec);
        }
    }

    /**
     * Sends some requests and tests the answers.
     * 
     * @param stack
     */
    void try_connect_and_disconnect_ok(ProtocolStack stack) {
        try {
            send_request("CONNECT", stack, true);
            send_request("FAIL", stack, false); // we receive an echo so we
                                                // should receive FAIL
            send_request("DISCONNECT", stack, true);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }
    }

    /**
     * Sends some requests and tests the answers.
     * 
     * @param stack
     */
    void try_connect_and_disconnect_fail(ProtocolStack stack) {
        try {
            send_request("connect", stack, false); // must be upper case
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * 
     * @throws ProtocolException
     * @throws IOException
     */
    public void testConnection() throws ProtocolException, IOException {
        ProtocolLayer layer = (new TcpSessionStarter(null, new IpSessionId(
                "localhost", 9999))).connect().getProtocolLayer();

        try_connect_and_disconnect_ok(layer);

        layer.doClose();
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * This connection should fail.
     * 
     * @throws ProtocolException
     * @throws IOException
     */
    public void testConnectionFail() throws ProtocolException, IOException {
        ProtocolLayer layer = (new TcpSessionStarter(null, new IpSessionId(
                "localhost", 9999))).connect().getProtocolLayer();

        try_connect_and_disconnect_fail(layer);

        layer.doClose();
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * As the previous one: we execute it only to test that the previous
     * connection does not interfere.
     * 
     * @throws ProtocolException
     * @throws IOException
     */
    public void testConnection2() throws ProtocolException, IOException {
        ProtocolLayer layer = (new TcpSessionStarter(null, new IpSessionId(
                "localhost", 9999))).connect().getProtocolLayer();

        try_connect_and_disconnect_ok(layer);

        layer.doClose();
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * All messages will be tunneled through HTTP.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public void testConnectionHTTP() throws ProtocolException, IOException,
            InterruptedException {
        System.out.println("creating the new connection server...");
        ConnectionServerThread connectionServer = new ConnectionServerThread(
                sessionManager, new HTTPEchoProtocolFactory(), new IpSessionId(
                        11000));
        connectionServer.start();
        System.out.println("connection server started");

        HTTPTunnelProtocolLayer httptunnel = new HTTPTunnelProtocolLayer();
        httptunnel.setSenderMode(true);
        ProtocolStack protocolStack = new ProtocolStack(httptunnel);
        protocolStack.connect(new TcpSessionStarter(null, new IpSessionId(
                "localhost", 11000)));

        try_connect_and_disconnect_ok(protocolStack);

        protocolStack.close();
        connectionServer.close();
        connectionServer.join();
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * All messages will be tunneled through HTTP.
     * 
     * This connection should fail.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public void testConnectionHTTPFail() throws ProtocolException, IOException,
            InterruptedException {
        System.out.println("creating the new connection server...");
        ConnectionServerThread connectionServer = new ConnectionServerThread(
                sessionManager, new HTTPEchoProtocolFactory(), new IpSessionId(
                        11000));
        connectionServer.start();
        System.out.println("connection server started");

        HTTPTunnelProtocolLayer httptunnel = new HTTPTunnelProtocolLayer();
        httptunnel.setSenderMode(true);
        ProtocolStack protocolStack = new ProtocolStack(httptunnel);
        protocolStack.connect(new TcpSessionStarter(null, new IpSessionId(
                "localhost", 11000)));

        try_connect_and_disconnect_fail(protocolStack);

        protocolStack.close();
        connectionServer.close();
        connectionServer.join();
    }

    /**
     * Establishes a connection and sends some requests and tests the answers.
     * Uses a thread to send and receive.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public void testConnectionThread() throws ProtocolException, IOException,
            InterruptedException {
        ConnectionThread t = new ConnectionThread();

        t.start();
        t.join();
    }

    /**
     * Spawns many threads that send and receive concurrently.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public void testConnectionThreads() throws ProtocolException, IOException,
            InterruptedException {
        Vector<ConnectionThread> threads = new Vector<ConnectionThread>();

        for (int i = 1; i <= 10; ++i) {
            ConnectionThread t = new ConnectionThread();
            threads.addElement(t);
            t.start();
        }

        Enumeration<ConnectionThread> en = threads.elements();

        while (en.hasMoreElements())
            en.nextElement().join();
    }

    /**
     * Thread used to send and receive.
     * 
     * @author $author$
     * @version $Revision: 1.5 $
     */
    public class ConnectionThread extends Thread {
        /**
         * The main execution method: calls try_connect_and_disconnect.
         */
        public void run() {
            try {
                ProtocolLayer layer = (new TcpSessionStarter(null, new IpSessionId(
                        "localhost", 9999))).connect().getProtocolLayer();

                try_connect_and_disconnect_ok(layer);

                layer.doClose();
            } catch (ProtocolException e) {
                fail(e.getMessage());
            } catch (UnknownHostException ue) {
                fail(ue.getMessage());
            }
        }
    }
}
