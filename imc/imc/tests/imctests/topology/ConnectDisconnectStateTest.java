/*
 * Created on Jan 13, 2005
 *
 */
package imctests.topology;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import org.mikado.imc.events.SessionEvent;
import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.SessionEvent.SessionEventType;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.ConnectState;
import org.mikado.imc.topology.ConnectionServer;
import org.mikado.imc.topology.NodeLocation;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.ConnectionServerThread;
import org.mikado.imc.topology.ConnectionStarter;

import junit.framework.TestCase;

/**
 * @author bettini
 * 
 */
public class ConnectDisconnectStateTest extends TestCase {
    /**
     * This is used to perform an accept which simply returns a ProtocolStack
     * 
     * @author Lorenzo Bettini
     * 
     */
    public class AcceptStackThread extends Thread {
        ConnectionServer connectionServer;

        ProtocolStack protocolStack;

        public AcceptStackThread(ConnectionServer connectionServer) {
            this.connectionServer = connectionServer;
        }

        public void run() {
            try {
                protocolStack = connectionServer.accept();
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.6 $
     */
    public class ClosingState extends ProtocolStateSimple {

        /**
         * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            System.out.println("ClosingState: closing...");
            close();
        }

    }

    /**
     * @author bettini
     * 
     */
    public class ConnectionListener implements EventListener {
        Vector<String> connections = new Vector<String>();

        Vector<String> disconnections = new Vector<String>();

        /**
         * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
         */
        public synchronized void notify(Event event) {
            if (!(event instanceof SessionEvent))
                assertTrue(false);

            SessionEvent connectionEvent = (SessionEvent) event;

            Session sessionId = connectionEvent.getSession();
            if (connectionEvent.type == SessionEventType.CONNECTION)
                connections.addElement(sessionId.toString());
            else if (connectionEvent.type == SessionEventType.DISCONNECTION)
                disconnections.addElement(sessionId.toString());
            else
                assertTrue(false);
        }

    }

    /**
     * @author bettini
     * 
     */
    public class SimpleProtocolFactory implements ProtocolFactory {

        /**
         * @see org.mikado.imc.newprotocols.ProtocolFactory#createProtocol()
         */
        public Protocol createProtocol() throws ProtocolException {
            Protocol protocol = new Protocol();
            protocol.setState(Protocol.START, new EchoProtocolState());
            return protocol;
        }

    }

    /**
     * @author bettini
     * 
     */
    public class ConnectDisconnectThread extends Thread {
        String buffer;

        public void run() {
            buffer = connect_send_and_disconnect();
        }
    }

    /**
     * @author bettini
     * 
     */
    public class ConnectDisconnectWithStarterThread extends Thread {
        String buffer;

        public void run() {
            buffer = connect_send_and_disconnect_with_starter();
        }
    }

    /** The connection server. */
    ConnectionServerThread connectionServerThread;

    /** The connection manager used by the connection server. */
    SessionManager sessionManager;

    /** The event manager used by the connection server. */
    EventManager eventManager;

    /** The connection manager used by the connection starter. */
    SessionManager connectionStarterManager;

    /** The connection starter used to start a connection. */
    ConnectionStarter connectionStarter;

    /** The event manager used by the connection starter. */
    EventManager eventManagerConn;

    /**
     * The identifier for the connection protocol (default "tcp").
     */
    String connectionProtocolId = "tcp";

    /**
     * to create SessionStarter instances.
     */
    protected SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        sessionManager = new SessionManager();
        eventManager = new EventManager();
        sessionManager.setEventManager(eventManager);
        connectionServerThread = new ConnectionServerThread(sessionManager,
                new SimpleProtocolFactory(), createSessionId(9999));
        connectionServerThread.start();
        System.out.println("connection server started");
        connectionStarterManager = new SessionManager();
        eventManagerConn = new EventManager();
        connectionStarterManager.setEventManager(eventManagerConn);
        connectionStarter = new ConnectionStarter(connectionStarterManager);
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("connection server closing");
        connectionServerThread.close();
        System.out.println("connection server joining");
        connectionServerThread.join();
        System.out.println("connection server stopped");
        Thread.sleep(1000);
    }

    protected SessionId createSessionId(int port) throws UnknownHostException {
        return new IpSessionId("127.0.0.1", port, connectionProtocolId);
    }

    String connect_send_and_disconnect() {
        ConnectState connectState = new ConnectState("ECHO");
        ConnectState disconnectState = new ConnectState("");
        disconnectState.setDoConnect(false);
        Protocol protocol = new Protocol(connectState, disconnectState);
        SimpleEchoState simpleEchoState = new SimpleEchoState(Protocol.END);
        try {
            protocol.setState("ECHO", simpleEchoState);
            SessionId sessionId = createSessionId(9999);
            SessionStarter sessionStarter = sessionStarterTable
                    .createSessionStarter(null, sessionId);
            protocol.connect(sessionStarter);
            protocol.start();
        } catch (ProtocolException e) {

        } catch (UnknownHostException ue) {

        }
        return simpleEchoState.buffer.toString();
    }

    void connect_and_disconnect() {
        ConnectState connectState = new ConnectState("CLOSING");
        ConnectState disconnectState = new ConnectState("");
        disconnectState.setDoConnect(false);
        Protocol protocol = new Protocol(connectState, disconnectState);
        try {
            protocol.setState("CLOSING", new ClosingState());
            SessionId sessionId = createSessionId(9999);
            SessionStarter sessionStarter = sessionStarterTable
                    .createSessionStarter(null, sessionId);
            protocol.connect(sessionStarter);
            protocol.start();
        } catch (ProtocolException e) {

        } catch (UnknownHostException ue) {

        }
    }

    String connect_send_and_disconnect_with_starter() {
        Protocol protocol = new Protocol();
        SimpleEchoState simpleEchoState = new SimpleEchoState(Protocol.END);
        try {
            protocol.setState(Protocol.START, simpleEchoState);
            SessionId sessionId = createSessionId(9999);
            connectionStarter.connect(sessionId, protocol).start();
        } catch (ProtocolException e) {

        } catch (UnknownHostException ue) {

        }
        return simpleEchoState.buffer.toString();
    }

    String connect_send_and_disconnect_with_stack() throws ProtocolException {
        ConnectState connectState = new ConnectState("SIMPLE");
        ConnectState disconnectState = new ConnectState("");
        disconnectState.setDoConnect(false);
        Protocol protocol = new Protocol(connectState, disconnectState);
        SimpleEchoState simpleEchoState = new SimpleEchoState(Protocol.END);
        protocol.setState("SIMPLE", simpleEchoState);
        try {
            SessionId sessionId = createSessionId(9999);
            ProtocolStack protocolStack = connectionStarter.connect(sessionId);

            /* check remote and local stacks */
            NodeLocation server = new NodeLocation(sessionId.toString());
            assertTrue(connectionStarterManager.getNodeStack(server) != null);
            assertTrue(connectionStarterManager.getNodeStack(server) == protocolStack);
            assertFalse(connectionStarterManager.isLocal(server));
            assertTrue(connectionStarterManager.isLocal(new NodeLocation(
                    protocolStack.getSession().getLocalEnd().toString())));

            protocol.setProtocolStack(protocolStack);
            protocol.start();
        } catch (ProtocolException e) {

        } catch (UnknownHostException ue) {

        }
        return simpleEchoState.buffer.toString();
    }

    /**
     * use connect and accept that return stack and check the isLocal method in
     * SessionManager.
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testOnlyStack() throws ProtocolException, InterruptedException {
        System.err.println("*** testOnlyStack ***");
        try {
            SessionId sessionId = createSessionId(9998);
            SessionManager serverSessionManager = new SessionManager();
            ConnectionServer connectionServer = new ConnectionServer(
                    serverSessionManager, sessionId);
            AcceptStackThread acceptStackThread = new AcceptStackThread(
                    connectionServer);
            acceptStackThread.start();

            ProtocolStack protocolStack = connectionStarter.connect(sessionId);
            acceptStackThread.join();
            assertTrue(acceptStackThread.protocolStack != null);

            NodeLocation server = new NodeLocation(sessionId.toString());
            NodeLocation client = new NodeLocation(protocolStack.getSession()
                    .getLocalEnd().toString());

            /* check remote and local stacks */
            assertTrue(connectionStarterManager.getNodeStack(server) != null);
            assertTrue(connectionStarterManager.getNodeStack(server) == protocolStack);
            assertFalse(connectionStarterManager.isLocal(server));
            assertTrue(connectionStarterManager.isLocal(client));

            /* check the server side */
            assertTrue(serverSessionManager.isLocal(server));
            assertTrue(serverSessionManager.getNodeStack(client) != null);
            assertTrue(serverSessionManager.getNodeStack(client) == acceptStackThread.protocolStack);
            assertFalse(serverSessionManager.isLocal(client));

            /* this should stop also the ConnectionServer */
            serverSessionManager.close();

            /* now the AcceptStackThread must have terminated */
            acceptStackThread.join();
        } catch (ProtocolException e) {

        } catch (UnknownHostException ue) {

        }
    }

    public void testSimpleStack() throws ProtocolException {
        System.err.println("*** testSimpleStack ***");
        connect_send_and_disconnect_with_stack();
    }

    public void testSimple() throws ProtocolException {
        System.err.println("*** testSimple ***");
        connect_send_and_disconnect();
    }

    public void testSimpleWStarter() throws ProtocolException {
        System.err.println("*** testSimpleWStarter ***");
        connect_send_and_disconnect_with_starter();
    }

    public void testThread() throws ProtocolException, InterruptedException {
        System.err.println("*** testThread ***");
        ConnectDisconnectThread t = new ConnectDisconnectThread();
        t.start();
        t.join();
        assertTrue(t.buffer != null);
        assertEquals(t.buffer, "Hello");
    }

    public void testThreadWStarter() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testThreadWStarter ***");
        ConnectDisconnectWithStarterThread t = new ConnectDisconnectWithStarterThread();
        t.start();
        t.join();
        assertTrue(t.buffer != null);
        assertEquals(t.buffer, "Hello");
    }

    public void testThreads() throws ProtocolException, InterruptedException {
        System.err.println("*** testThreads ***");
        ConnectionListener listener = new ConnectionListener();
        eventManager.addListener(SessionManager.EventClass, listener);

        Vector<ConnectDisconnectThread> threads = new Vector<ConnectDisconnectThread>();

        int thread_num = 4;
        for (int i = 1; i <= thread_num; ++i) {
            ConnectDisconnectThread t = new ConnectDisconnectThread();
            threads.addElement(t);
            t.start();
        }

        Enumeration<ConnectDisconnectThread> en = threads.elements();

        while (en.hasMoreElements()) {
            ConnectDisconnectThread t = en.nextElement();
            t.join();
            System.out.println("checking thread " + t.getName() + ": "
                    + (t.buffer != null && t.buffer.equals("Hello")));
            assertTrue(t.buffer != null);
            assertEquals(t.buffer, "Hello");
        }

        System.out.println("connections: " + listener.connections);
        System.out.println("disconnections: " + listener.disconnections);

        assertEquals((thread_num * 2), listener.connections.size());
        assertEquals((thread_num * 2), listener.disconnections.size());
    }

    public void testThreadsWStarter() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testThreadsWStarter ***");
        ConnectionListener listener = new ConnectionListener();
        eventManager.addListener(SessionManager.EventClass, listener);

        ConnectionListener listenerConn = new ConnectionListener();
        eventManagerConn.addListener(SessionManager.EventClass, listenerConn);

        Vector<ConnectDisconnectWithStarterThread> threads = new Vector<ConnectDisconnectWithStarterThread>();

        int thread_num = 2;
        for (int i = 1; i <= thread_num; ++i) {
            ConnectDisconnectWithStarterThread t = new ConnectDisconnectWithStarterThread();
            threads.addElement(t);
            t.start();
        }

        Enumeration<ConnectDisconnectWithStarterThread> en = threads.elements();
        int n = 0;

        while (en.hasMoreElements()) {
            ConnectDisconnectWithStarterThread t = en.nextElement();
            t.join();
            assertTrue(t.buffer != null);
            assertEquals("Hello", t.buffer);
            System.out.println("terminated thread " + ++n);
        }

        System.out.println("incoming connections: " + listener.connections);
        System.out.println("incoming disconnections: "
                + listener.disconnections);

        // both the connection manager and the connection management states
        // generate events, that's why events are double.
        assertEquals((thread_num * 2), listener.connections.size());
        assertEquals((thread_num * 2), listener.disconnections.size());

        System.out.println("outgoing connections: " + listenerConn.connections);
        System.out.println("outgoing disconnections: "
                + listenerConn.disconnections);

        assertEquals((thread_num * 2), listenerConn.connections.size());
        assertEquals((thread_num * 2), listenerConn.disconnections.size());
    }

    public void testConnectAndDisconnect() throws ProtocolException,
            InterruptedException {
        if (!connectionProtocolId.equals("tcp"))
            return;
        // for udp disconnections are not detected if they fail

        System.err.println("*** testConnectAndDisconnect ***");
        ConnectionListener listener = new ConnectionListener();
        eventManager.addListener(SessionManager.EventClass, listener);

        connect_and_disconnect();

        // this should give time to the server to detect the
        // disconnection.
        Thread.sleep(1000);

        System.out.println("connections: " + listener.connections);
        System.out.println("disconnections: " + listener.disconnections);

        assertEquals(2, listener.connections.size());
        assertEquals(2, listener.disconnections.size());
    }

    /**
     * @author bettini
     * 
     */
    public class SimpleEchoState extends ProtocolStateSimple {
        String toSend = "Hello";

        public StringBuffer buffer = new StringBuffer();

        /**
         * @param next_state
         */
        public SimpleEchoState(String next_state) {
            super(next_state);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.newprotocols.ProtocolState#enter()
         */
        public void enter(Object param, TransmissionChannel ignore)
                throws ProtocolException {
            Marshaler marshaler = createMarshaler();
            try {
                marshaler.writeStringLine(toSend);
            } catch (IOException e) {
                fail(e.getMessage());
            }
            releaseMarshaler(marshaler);

            System.out.println("Sent string: " + toSend);

            try {
                UnMarshaler unMarshaler = createUnMarshaler();
                String received = unMarshaler.readStringLine();
                System.out.println("Received string: " + received);
                buffer.append(received);
                assertEquals(toSend, received);
            } catch (IOException e1) {
                fail(e1.getMessage());
            }
        }
    }

}
