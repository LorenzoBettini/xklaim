/*
 * Created on Jan 18, 2005
 *
 */
package imctests.topology;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.LogEventListener;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.KeepAliveProtocolState;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolComposite;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolState;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.AcceptNodeCoordinator;
import org.mikado.imc.topology.ConnectState;
import org.mikado.imc.topology.ConnectionManagementState;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.NodeCoordinator;
import org.mikado.imc.topology.NodeLocation;
import org.mikado.imc.topology.NodeProcess;
import org.mikado.imc.topology.NotifyProtocolState;
import org.mikado.imc.topology.SendAndWaitForNotification;
import org.mikado.imc.topology.WaitingForNotification;

import junit.framework.TestCase;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.14 $
 */
public class NodeTest extends TestCase {
    /**
     * Simply record the underlying session into a vector
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.14 $
     */
    public class RecordSessionState extends ProtocolState {

        /**
         * 
         */
        public RecordSessionState() {
            super(Protocol.END);
        }

        /**
         * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            sessions.addElement(getSession());
        }

    }

    /**
     * @author bettini
     */
    public class SendNumberAndWaitProcess extends NodeProcess {
        private static final long serialVersionUID = 3977580286110150712L;

        SendNumberAndWait sendNumberAndWait;

        ProtocolException protocolException = null;

        public SendNumberAndWaitProcess(SendNumberAndWait sendNumberAndWait) {
            super();
            this.sendNumberAndWait = sendNumberAndWait;
        }

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            try {
                int retry = 5;
                ProtocolStack protocolStack = null;
                NodeLocation nodeLocation = new NodeLocation(createSessionId(
                        9999).toString());

                while (retry-- > 0) {
                    protocolStack = getNodeStack(nodeLocation);
                    if (protocolStack == null) {
                        System.err.println("no stack, retrying...");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            return;
                        }
                    } else {
                        break;
                    }
                }

                if (protocolStack == null) {
                    throw new IMCException("cannot find protocol stack");
                }

                assertFalse(isLocal(nodeLocation));

                sendNumberAndWait.protocolStack = protocolStack;
                sendNumberAndWait.send();
            } catch (ProtocolException pe) {
                protocolException = pe;
                throw pe;
            }
        }

    }

    /**
     * @author bettini
     */
    public class SendNumberAndWait extends SendAndWaitForNotification {
        boolean success = false;

        ProtocolStack protocolStack;

        String notificationString = new String();

        /**
         * @param id
         * @param waiting
         */
        public SendNumberAndWait(String id, WaitingForNotification waiting) {
            super(id, waiting);
        }

        /**
         * @see org.mikado.imc.newtopology.SendAndWaitForNotification#doSend()
         */
        protected void doSend() throws ProtocolException {
            System.out.println(getId()
                    + ": sending 10 numbers to localhost:9999");
            Marshaler marshaler = protocolStack.createMarshaler();
            try {
                for (int i = 1; i <= 10; ++i) {
                    marshaler.writeInt(i);
                }
                // now send the identifier
                marshaler.writeStringLine(getId());
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
            protocolStack.releaseMarshaler(marshaler);
            System.out.println(getId() + ": sent 10 numbers");
        }

        /**
         * @see org.mikado.imc.newtopology.SendAndWaitForNotification#doReceiveNotification()
         */
        protected void doReceiveNotification(UnMarshaler unMarshaler)
                throws ProtocolException {
            try {
                System.out.println(getId() + ": receiving notification");
                notificationString = unMarshaler.readStringLine();
                System.out.println(getId() + ": received notification = "
                        + notificationString);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
            success = true;
        }
    }

    /**
     * @author bettini
     * 
     */
    public class NumberReceiverState extends ProtocolStateSimple {
        Vector<Integer> numbers = new Vector<Integer>();

        int toreceive;

        int times = 1;

        /**
         * @param toreceive
         */
        public NumberReceiverState(int toreceive) {
            this.toreceive = toreceive;
        }

        /**
         * @param toreceive
         */
        public NumberReceiverState(int toreceive, int times) {
            this.toreceive = toreceive;
            this.times = times;
        }

        /**
         * @see org.mikado.imc.newprotocols.ProtocolState#enter(java.lang.Object)
         */
        public void enter(Object param, TransmissionChannel ignore)
                throws ProtocolException {
            int received_numbers = 0;
            int t = 0;
            while (t < times) {
                UnMarshaler unMarshaler = createUnMarshaler();
                received_numbers = 0;
                while (received_numbers < toreceive) {
                    try {
                        numbers.addElement(new Integer(unMarshaler.readInt()));
                        ++received_numbers;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                ++t;
            }

            setNextState(Protocol.END);
        }
    }

    /**
     * @author bettini
     * 
     */
    public class NumberReceiverStateAndNotify extends ProtocolStateSimple {
        Vector<Integer> numbers = new Vector<Integer>();

        WaitingForNotification waitingForNotification;

        int toreceive;

        int times;

        /**
         * @param toreceive
         */
        public NumberReceiverStateAndNotify(int toreceive, int times,
                WaitingForNotification waitingForNotification) {
            this.toreceive = toreceive;
            this.times = times;
            this.waitingForNotification = waitingForNotification;
        }

        /**
         * 
         * @see org.mikado.imc.newprotocols.ProtocolState#enter(java.lang.Object)
         */
        public void enter(Object param, TransmissionChannel ignore)
                throws ProtocolException {
            int received_numbers = 0;
            int current_times = 0;

            try {
                while (current_times < times) {
                    received_numbers = 0;
                    UnMarshaler unMarshaler = createUnMarshaler();
                    while (received_numbers < toreceive) {
                        numbers.addElement(new Integer(unMarshaler.readInt()));
                        ++received_numbers;
                    }
                    String id = unMarshaler.readStringLine();
                    sendnotify(id, unMarshaler);
                    ++current_times;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            setNextState(Protocol.END);
        }

        protected void sendnotify(String id, UnMarshaler unMarshaler)
                throws IOException, ProtocolException {
            Marshaler marshaler = createMarshaler();
            marshaler.writeStringLine(id); // send it back as notification
            releaseMarshaler(marshaler);
            waitingForNotification.wakeUp(id, unMarshaler); // manually notify
            // it
        }
    }

    /**
     * @author bettini
     */
    public class NumberReceiverStateAndNotify2 extends
            NumberReceiverStateAndNotify {

        /**
         * @param toreceive
         * @param times
         * @param waitingForNotification
         */
        public NumberReceiverStateAndNotify2(int toreceive, int times,
                WaitingForNotification waitingForNotification) {
            super(toreceive, times, waitingForNotification);
        }

        protected void sendnotify(String id, UnMarshaler unMarshaler)
                throws IOException, ProtocolException {
            Marshaler marshaler = createMarshaler();
            marshaler.writeStringLine("NOTIFY");
            marshaler.writeStringLine(id); // send it back as notification
            marshaler.writeStringLine("OK " + id);
            releaseMarshaler(marshaler);
        }
    }

    /**
     * @author bettini
     */
    public class NumberReceiverStateAndNotifyInterrupt extends
            NumberReceiverStateAndNotify {

        /**
         * @param toreceive
         * @param times
         * @param waitingForNotification
         */
        public NumberReceiverStateAndNotifyInterrupt(int toreceive, int times,
                WaitingForNotification waitingForNotification) {
            super(toreceive, times, waitingForNotification);
        }

        protected void sendnotify(String id, UnMarshaler unMarshaler)
                throws IOException, ProtocolException {
            Marshaler marshaler = createMarshaler();
            marshaler.writeStringLine("NOTIFY");
            // do not send the id so that the notify state is still waiting
            releaseMarshaler(marshaler);
            // now closes the connection to test whether the waiting
            // receiver is notified about the exception
            close();
        }
    }

    /**
     * @author bettini
     * 
     */
    public class SenderProcess extends NodeProcess {
        private static final long serialVersionUID = 4049635698405486641L;

        boolean success = false;

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            ProtocolStack protocolStack = null;
            int retry = 5;
            while (retry-- > 0) {
                protocolStack = getNodeStack(new NodeLocation(createSessionId(
                        9999).toString()));
                if (protocolStack == null) {
                    System.err.println("no stack, retrying...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        return;
                    }
                } else {
                    break;
                }
            }

            if (protocolStack == null)
                return;

            try {
                System.out.println("obtained stack for localhost:9999");
                Marshaler marshaler = protocolStack.createMarshaler();
                marshaler.writeStringLine("hello");
                protocolStack.releaseMarshaler(marshaler);
                UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
                String received = unMarshaler.readStringLine();
                assertEquals(received, "hello");
                if (received.equals("hello"))
                    success = true;
            } catch (IOException e) {
                assertTrue(false);
            }
        }
    }

    /**
     * @author bettini
     * 
     */
    public class NumberSenderProcess extends NodeCoordinator {
        boolean success = false;

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            ProtocolStack protocolStack = null;
            int retry = 5;
            while (retry-- > 0) {
                protocolStack = getNodeStack(new NodeLocation(createSessionId(
                        9999).toString()));
                if (protocolStack == null) {
                    System.err.println("no stack, retrying...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        return;
                    }
                } else {
                    break;
                }
            }

            if (protocolStack == null)
                return;

            try {
                System.out.println("obtained stack for localhost:9999");
                Marshaler marshaler = protocolStack.createMarshaler();
                for (int i = 1; i <= 10; ++i) {
                    marshaler.writeInt(i);
                }
                protocolStack.releaseMarshaler(marshaler);
                System.out.println("sent 10 numbers");
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author bettini
     * 
     */
    public class AcceptCoordinator extends NodeCoordinator {
        Protocol protocol;

        public AcceptCoordinator(ProtocolState protocolState) {
            protocol = new Protocol(protocolState);
        }

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            acceptAndStart(createSessionId(9999), protocol);
        }

    }

    /**
     * Does not start a protocol
     * 
     * @author bettini
     * 
     */
    public class SimpleAcceptCoordinator extends NodeCoordinator {
        SessionId sessionId;

        boolean success = false;

        /**
         * @param sessionId
         */
        public SimpleAcceptCoordinator(SessionId sessionId) {
            this.sessionId = sessionId;
        }

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            accept(sessionId);
            success = true;
        }

    }

    /**
     * @author bettini
     * 
     */
    public class ConnectCoordinator extends NodeCoordinator {
        Protocol protocol;

        SessionId sessionId;

        public ConnectCoordinator(ProtocolState protocolState)
                throws UnknownHostException, IMCException {
            protocol = new Protocol(protocolState);
            sessionId = createSessionId(9999);
        }

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            connect(sessionId, protocol);
        }

    }

    /**
     * @author bettini
     * 
     */
    public class AcceptLayerCoordinator extends NodeCoordinator {
        Protocol protocol;

        public AcceptLayerCoordinator(ProtocolState protocolState) {
            protocol = new Protocol(protocolState);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            ProtocolStack protocolStack = accept(createSessionId(9999));
            ConnectionManagementState connect = new ConnectionManagementState();
            connect.setEventManager(getEventManager());
            ConnectionManagementState disconnect = new ConnectionManagementState(
                    Protocol.END);
            disconnect.setEventManager(getEventManager());
            protocol.setProtocolStack(protocolStack);

            ProtocolComposite protocolComposite = new ProtocolComposite(
                    connect, disconnect, protocol);
            protocolComposite.start();
        }

    }

    /**
     * @author bettini
     * 
     */
    public class ConnectLayerCoordinator extends NodeCoordinator {
        Protocol protocol;

        public ConnectLayerCoordinator(ProtocolState protocolState) {
            protocol = new Protocol(protocolState);
        }

        /**
         * @see org.mikado.imc.newtopology.NodeCoordinator#execute()
         */
        public void execute() throws IMCException {
            ProtocolStack protocolStack = connect(createSessionId(9999));
            ConnectState connectState = new ConnectState();
            ConnectState disconnectState = new ConnectState();
            disconnectState.setDoConnect(false);
            connectState.setEventManager(getEventManager());
            disconnectState.setEventManager(getEventManager());
            protocol.setProtocolStack(protocolStack);

            ProtocolComposite protocolComposite = new ProtocolComposite(
                    connectState, disconnectState, protocol);
            protocolComposite.start();
        }

    }

    /**
     * @author bettini
     * 
     */
    public class ProtocolStateSuccess extends ProtocolStateSimple {
        boolean success = false;

        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            success = true; // it means we're connected
            setNextState(Protocol.END);
        }
    }

    /**
     * @author bettini
     * 
     */
    public class ServerThread extends Thread {
        Protocol protocol;

        public ServerThread(ProtocolState protocolState) {
            protocol = new Protocol(protocolState);
        }

        public void run() {
            try {
                server.acceptAndStart(createSessionId(9999), protocol);
            } catch (ProtocolException e) {
                e.printStackTrace();
                fail(e.getMessage());
            } catch (IMCException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

    Node client;

    EventManager clientEventManager;

    Node server;

    EventManager serverEventManager;

    Vector<Session> sessions;

    public static void main(String[] args) {
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        client = new Node();
        clientEventManager = new EventManager();
        client.setEventManager(clientEventManager);
        server = new Node();
        serverEventManager = new EventManager();
        server.setEventManager(serverEventManager);
        sessions = new Vector<Session>();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        client.close();
        server.close();
    }

    /**
     * It creates a SessionId on 127.0.0.1:port with a specific connection
     * protocol (default is tcp)
     * 
     * @param port
     * @return
     * @throws IMCException
     */
    protected SessionId createSessionId(int port) throws IMCException {
        try {
            return new IpSessionId("127.0.0.1", port, getConnectionProtocolId());
        } catch (UnknownHostException e1) {
            throw new IMCException("creating session id", e1);
        }
    }

    /**
     * It creates a localhost SessionId (reverting to 127.0.0.1 if localhost
     * cannot be resolved).
     * 
     * @param port
     * @return
     * @throws IMCException
     */
    protected SessionId createLocalSessionId(int port) throws IMCException {
        try {
            /* check whether localhost can be resolved */
            new IpSessionId("localhost", port, getConnectionProtocolId());

            /*
             * if it can be resolved, then create a generic SessionId, where
             * localhost is not converted into 127.0.0.1
             */
            return new SessionId(getConnectionProtocolId(), "localhost:" + port);
        } catch (UnknownHostException e) {
            try {
                return new IpSessionId("127.0.0.1", port,
                        getConnectionProtocolId());
            } catch (UnknownHostException e1) {
                throw new IMCException("creating session id", e1);
            }
        }
    }

    /**
     * @return The connection protocol id (default is "tcp")
     */
    protected String getConnectionProtocolId() {
        return "tcp";
    }

    /**
     * This performs several accepts without using a SessionStarter.
     * 
     * The connections established are not closed.
     * 
     * @throws UnknownHostException
     * @throws IMCException
     */
    public void testSeveralAccepts() throws UnknownHostException, IMCException,
            InterruptedException {
        System.err.println("*** testSeveralAccepts ***");
        SessionId sessionId = createLocalSessionId(9998);

        System.out.println("using session id: " + sessionId);

        SimpleAcceptCoordinator acceptOnceNodeCoordinator = new SimpleAcceptCoordinator(
                sessionId);

        server.addNodeCoordinator(acceptOnceNodeCoordinator);

        /*
         * we must make sure the connection stays up so that we have a session
         * created that is not removed when the SessionStarter was closed (it
         * was used for only one accept by AcceptOnceNodeCoordinator)
         */
        ProtocolStack protocolStack = client.connect(sessionId);
        System.out.println("client connected: " + protocolStack.getSession());
        acceptOnceNodeCoordinator.join();
        assertTrue(acceptOnceNodeCoordinator.success);

        /*
         * now we span another AcceptOnceNodeCoordinator that will use the same
         * SessionId for accepting sessions (and a session had already been
         * established with the previous SessionId and it is still up)
         */
        acceptOnceNodeCoordinator = new SimpleAcceptCoordinator(sessionId);

        server.addNodeCoordinator(acceptOnceNodeCoordinator);

        protocolStack = client.connect(sessionId);
        System.out.println("client connected: " + protocolStack.getSession());
        acceptOnceNodeCoordinator.join();
        assertTrue(acceptOnceNodeCoordinator.success);
    }

    public void testSimpleConnection() throws IMCException,
            InterruptedException {
        System.err.println("*** testSimpleConnection ***");
        ProtocolStateSuccess protocolStateSuccessServer = new ProtocolStateSuccess();
        ServerThread serverThread = new ServerThread(protocolStateSuccessServer);
        serverThread.start();

        LogEventListener logEventListenerClient = new LogEventListener();
        LogEventListener logEventListenerServer = new LogEventListener();

        clientEventManager.addListener(SessionManager.EventClass,
                logEventListenerClient);
        serverEventManager.addListener(SessionManager.EventClass,
                logEventListenerServer);

        ProtocolStateSuccess protocolStateSuccessClient = new ProtocolStateSuccess();

        client.connect(createSessionId(9999), new Protocol(
                protocolStateSuccessClient));

        System.out.println("client listener: ");
        System.out.println(logEventListenerClient.toString());
        System.out.println("server listener: ");
        System.out.println(logEventListenerServer.toString());

        assertEquals(logEventListenerClient.toString().length(),
                logEventListenerServer.toString().length());
        assertTrue(protocolStateSuccessServer.success);
        assertTrue(protocolStateSuccessClient.success);

        serverThread.join();
    }

    public void testSimpleConnectionCoordinator() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testSimpleConnectionCoordinator ***");
        ProtocolStateSuccess protocolStateSuccessServer = new ProtocolStateSuccess();
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                protocolStateSuccessServer);

        LogEventListener logEventListenerClient = new LogEventListener();
        LogEventListener logEventListenerServer = new LogEventListener();

        clientEventManager.addListener(SessionManager.EventClass,
                logEventListenerClient);
        serverEventManager.addListener(SessionManager.EventClass,
                logEventListenerServer);

        ProtocolStateSuccess protocolStateSuccessClient = new ProtocolStateSuccess();
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                protocolStateSuccessClient);

        server.addNodeCoordinator(acceptCoordinator);

        client.executeNodeCoordinator(connectCoordinator);

        // the log event listener registered nothing, since both the
        // acceptCoordinator and the connectCoordinator don't have a
        // connection manager.
        System.out.println("client listener: ");
        System.out.println(logEventListenerClient.toString());
        System.out.println("server listener: ");
        System.out.println(logEventListenerServer.toString());

        assertEquals(logEventListenerClient.toString().length(),
                logEventListenerServer.toString().length());
        assertTrue(protocolStateSuccessServer.success);
        assertTrue(protocolStateSuccessClient.success);
    }

    /**
     * starts an accept node coordinator, then closes the node, and starts
     * another accept node coordinator listening on the same session id. Closing
     * the node should close also the SessionStarter associated with previous
     * accept node coordinator
     * 
     * @throws InterruptedException
     * @throws IMCException
     * @throws UnknownHostException
     */
    public void testCloseAcceptCoordinator() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testCloseAcceptCoordinator ***");

        SessionId sessionId = createSessionId(9999);
        SessionStarter sessionStarter = server.createSessionStarter(sessionId,
                null);

        AcceptNodeCoordinator acceptCoordinator = new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() throws ProtocolException {
                        return new Protocol(new RecordSessionState());
                    }
                }, sessionStarter);

        /* this will continuously listen for incoming connections */
        server.addNodeCoordinator(acceptCoordinator);

        Thread.sleep(1000);

        server.close();

        server = new Node();

        /* we should be able to reuse the previous session id */
        sessionStarter = server.createSessionStarter(sessionId, null);
        acceptCoordinator = new AcceptNodeCoordinator(new ProtocolFactory() {
            public Protocol createProtocol() throws ProtocolException {
                return new Protocol(new RecordSessionState());
            }
        }, sessionStarter);

        /* this will continuously listen for incoming connections */
        server.addNodeCoordinator(acceptCoordinator);

        Thread.sleep(1000);
    }

    public void testAcceptCoordinator() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testAcceptCoordinator ***");
        AcceptNodeCoordinator acceptCoordinator = new AcceptNodeCoordinator(
                new ProtocolFactory() {
                    public Protocol createProtocol() throws ProtocolException {
                        return new Protocol(new RecordSessionState());
                    }
                }, createSessionId(9999));

        /* this will continuously listen for incoming connections */
        server.addNodeCoordinator(acceptCoordinator);

        int num_of_connections = 5;
        Vector<ConnectCoordinator> clientCoordinators = new Vector<ConnectCoordinator>();
        Vector<ProtocolStateSuccess> states = new Vector<ProtocolStateSuccess>();

        /*
         * adds some node coordinators that connect to the server, executing on
         * the same client
         */
        for (int i = 0; i < num_of_connections; ++i) {
            ProtocolStateSuccess protocolStateSuccessClient = new ProtocolStateSuccess();
            ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                    protocolStateSuccessClient);
            clientCoordinators.addElement(connectCoordinator);
            states.addElement(protocolStateSuccessClient);
            client.executeNodeCoordinator(connectCoordinator);
        }

        for (int i = 0; i < num_of_connections; ++i) {
            clientCoordinators.elementAt(i).join();
            assertTrue(states.elementAt(i).success);
        }

        System.out.println("recorded sessions: " + sessions);
        assertEquals(sessions.size(), num_of_connections);

        /* stop listening for incoming connections */
        acceptCoordinator.close();
    }

    public void testConnectionCoordinator() throws InterruptedException,
            IMCException {
        System.err.println("*** testConnectionCoordinator ***");
        ProtocolStateSuccess protocolStateSuccessServer = new ProtocolStateSuccess();
        AcceptLayerCoordinator acceptCoordinator = new AcceptLayerCoordinator(
                protocolStateSuccessServer);

        LogEventListener logEventListenerClient = new LogEventListener();
        LogEventListener logEventListenerServer = new LogEventListener();

        clientEventManager.addListener(SessionManager.EventClass,
                logEventListenerClient);
        serverEventManager.addListener(SessionManager.EventClass,
                logEventListenerServer);

        ProtocolStateSuccess protocolStateSuccessClient = new ProtocolStateSuccess();
        ConnectLayerCoordinator connectCoordinator = new ConnectLayerCoordinator(
                protocolStateSuccessClient);

        server.addNodeCoordinator(acceptCoordinator);

        client.executeNodeCoordinator(connectCoordinator);

        System.out.println("client listener: ");
        System.out.println(logEventListenerClient.toString());
        System.out.println("server listener: ");
        System.out.println(logEventListenerServer.toString());

        assertEquals(logEventListenerClient.toString().length(),
                logEventListenerServer.toString().length());
        assertTrue(protocolStateSuccessServer.success);
        assertTrue(protocolStateSuccessClient.success);
    }

    public void testGetNodeStack() throws InterruptedException, IMCException,
            UnknownHostException {
        System.err.println("*** testGetNodeStack ***");
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                new EchoProtocolState());

        KeepAliveProtocolState keepAliveProtocolState = new KeepAliveProtocolState();
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                keepAliveProtocolState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        SenderProcess senderProcess = new SenderProcess();
        client.addNodeProcess(senderProcess);
        senderProcess.join();
        keepAliveProtocolState.die();
        assertTrue(senderProcess.success);
    }

    public void testSendNumbers() throws InterruptedException, IMCException,
            UnknownHostException {
        System.err.println("*** testSendNumbers ***");
        NumberReceiverState numberReceiverState = new NumberReceiverState(10);
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                numberReceiverState);

        KeepAliveProtocolState keepAliveProtocolState = new KeepAliveProtocolState();
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                keepAliveProtocolState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        NumberSenderProcess senderProcess = new NumberSenderProcess();
        client.addNodeCoordinator(senderProcess);
        senderProcess.join();
        keepAliveProtocolState.die();
        acceptCoordinator.join();
        assertTrue(senderProcess.success);
        assertEquals(10, numberReceiverState.numbers.size());
    }

    public void testSendNumbersMany() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testSendNumbersMany ***");
        int num_of_senders = 10;
        int num_of_numbers = 10;

        NumberReceiverState numberReceiverState = new NumberReceiverState(
                num_of_numbers, num_of_senders);
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                numberReceiverState);

        KeepAliveProtocolState keepAliveProtocolState = new KeepAliveProtocolState();
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                keepAliveProtocolState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        Vector<NumberSenderProcess> senders = new Vector<NumberSenderProcess>();
        NumberSenderProcess numberSenderProcess;

        for (int i = 1; i <= num_of_senders; ++i) {
            numberSenderProcess = new NumberSenderProcess();
            client.addNodeCoordinator(numberSenderProcess);
            senders.addElement(numberSenderProcess);
        }

        for (int i = 0; i < num_of_senders; ++i) {
            numberSenderProcess = senders.elementAt(i);
            numberSenderProcess.join();
            assertTrue(numberSenderProcess.success);
        }

        keepAliveProtocolState.die();
        acceptCoordinator.join();

        assertEquals(num_of_numbers * num_of_senders,
                numberReceiverState.numbers.size());

        // check that the numbers arrived in order
        for (int i = 0; i < num_of_senders; ++i) {
            for (int j = 1; j <= num_of_numbers; ++j) {
                assertEquals(j, ((Integer) numberReceiverState.numbers
                        .elementAt((i * num_of_numbers) + (j - 1))).intValue());
            }
        }
    }

    public void testNotifyProtocolState() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testNotifyProtocolState ***");
        int num_of_senders = 10;
        int num_of_numbers = 10;
        WaitingForNotification waitingForNotification = new WaitingForNotification();

        NumberReceiverStateAndNotify numberReceiverState = new NumberReceiverStateAndNotify2(
                num_of_numbers, num_of_senders, waitingForNotification);
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                numberReceiverState);

        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState();
        protocolSwitchState.addRequestState("NOTIFY", new NotifyProtocolState(
                Protocol.START, waitingForNotification));
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                protocolSwitchState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        Vector<SendNumberAndWaitProcess> senders = new Vector<SendNumberAndWaitProcess>();
        SendNumberAndWaitProcess sendNumberAndWait;

        for (int i = 1; i <= num_of_senders; ++i) {
            sendNumberAndWait = new SendNumberAndWaitProcess(
                    new SendNumberAndWait("Sender" + i, waitingForNotification));
            client.addNodeProcess(sendNumberAndWait);
            senders.addElement(sendNumberAndWait);
        }

        for (int i = 0; i < num_of_senders; ++i) {
            sendNumberAndWait = senders.elementAt(i);
            sendNumberAndWait.join();
            assertTrue(sendNumberAndWait.sendNumberAndWait.success);
            assertEquals("OK " + sendNumberAndWait.sendNumberAndWait.getId(),
                    sendNumberAndWait.sendNumberAndWait.notificationString);
        }

        protocolSwitchState.close();
        // acceptCoordinator.join();

        assertEquals(num_of_numbers * num_of_senders,
                numberReceiverState.numbers.size());

        // check that the numbers arrived in order
        for (int i = 0; i < num_of_senders; ++i) {
            for (int j = 1; j <= num_of_numbers; ++j) {
                assertEquals(j, ((Integer) numberReceiverState.numbers
                        .elementAt((i * num_of_numbers) + (j - 1))).intValue());
            }
        }
    }

    /**
     * Test for the notifyException in WaitingForNotification.
     * 
     * We use a NumberReceiverStateAndNotifyInterrupt that closes the stack
     * before notifying to the NotifyProtocolState, which will then receive an
     * exception and notify it to the WaitingForNotification
     * 
     * @throws InterruptedException
     * @throws IMCException
     * @throws UnknownHostException
     */
    public void testNotifyProtocolStateException() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testNotifyProtocolStateException ***");
        WaitingForNotification waitingForNotification = new WaitingForNotification();

        NumberReceiverStateAndNotify numberReceiverState = new NumberReceiverStateAndNotifyInterrupt(
                10, 1, waitingForNotification);
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                numberReceiverState);

        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState();
        protocolSwitchState.addRequestState("NOTIFY", new NotifyProtocolState(
                Protocol.START, waitingForNotification));
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                protocolSwitchState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        SendNumberAndWaitProcess sendNumberAndWait = new SendNumberAndWaitProcess(
                new SendNumberAndWait("Sender", waitingForNotification));
        client.addNodeProcess(sendNumberAndWait);

        sendNumberAndWait.join();

        System.out.println("received exception: "
                + sendNumberAndWait.protocolException);

        assertFalse(sendNumberAndWait.sendNumberAndWait.success);
        assertTrue(sendNumberAndWait.protocolException != null);

        protocolSwitchState.close();
        // acceptCoordinator.join();
    }

    /**
     * Test the close method in WaitingForNotification.
     * 
     * @throws InterruptedException
     * @throws IMCException
     * @throws UnknownHostException
     */
    public void testWaitingForNotificationClose() throws InterruptedException,
            IMCException, UnknownHostException {
        System.err.println("*** testWaitingForNotificationClose ***");
        WaitingForNotification waitingForNotification = new WaitingForNotification();

        // this is just to keep the server alive; we don't need any
        // further interaction from the server
        KeepAliveProtocolState keepAliveProtocolState = new KeepAliveProtocolState();
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(
                keepAliveProtocolState);

        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState();
        protocolSwitchState.addRequestState("NOTIFY", new NotifyProtocolState(
                Protocol.START, waitingForNotification));
        ConnectCoordinator connectCoordinator = new ConnectCoordinator(
                protocolSwitchState);

        server.addNodeCoordinator(acceptCoordinator);

        client.addNodeCoordinator(connectCoordinator);

        SendNumberAndWaitProcess sendNumberAndWait = new SendNumberAndWaitProcess(
                new SendNumberAndWait("Sender", waitingForNotification));
        client.addNodeProcess(sendNumberAndWait);

        // before closing we must make sure that the thread is waiting
        while (sendNumberAndWait.getState() != Thread.State.WAITING)
            Thread.sleep(500);
        
        // now close the WaitingForNotification
        waitingForNotification.close();
        
        sendNumberAndWait.join();

        System.out.println("received exception: "
                + sendNumberAndWait.protocolException);

        assertFalse(sendNumberAndWait.sendNumberAndWait.success);
        assertTrue(sendNumberAndWait.protocolException != null);
        
        // make sure to close the state of the server
        keepAliveProtocolState.close();

        protocolSwitchState.close();
    }
}
