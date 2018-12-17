/*
 * Created on Apr 14, 2005
 */
package imctests.protocols;

import java.io.IOException;
import java.net.UnknownHostException;

import org.mikado.imc.protocols.AlreadyBoundSessionStarterException;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IMCSessionStarterTable;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionIdBindException;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.SessionStarterTable;
import org.mikado.imc.protocols.SessionStarters;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.protocols.udp.UdpSessionStarter;
import org.mikado.imc.topology.ProtocolThread;

import junit.framework.TestCase;

/**
 * Tests for SessionStarter. This class uses TCP.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.14 $
 */
public class SessionStarterTest extends TestCase {
    /**
     * Performs an accept on the passed SessionStarter
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.14 $
     */
    public class SessionStarterThread extends Thread {
        SessionStarter sessionStarter;

        boolean success = false;

        /**
         * @param sessionStarter
         */
        public SessionStarterThread(SessionStarter sessionStarter) {
            this.sessionStarter = sessionStarter;
        }

        public void run() {
            try {
                sessionStarter.accept();
                success = true;
            } catch (ProtocolException e) {
            }
        }
    }

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.14 $
     */
    public class EchoClient extends Thread {
        boolean success = false;

        public void run() {
            try {
                exec_client();
                success = true;
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.14 $
     * 
     * As EchoClient but first connects, then waits to be notified, and then
     * starts communicating.
     */
    public class EchoClient2 extends Thread {
        boolean success = false;

        boolean canGo = false;

        public void run() {
            try {
                ProtocolStack protocolStack = connect_client();
                synchronized (this) {
                    while (!canGo)
                        wait();
                }
                send_and_receive(protocolStack);
                success = true;
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used for accepting a session.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.14 $
     */
    public class ServerThread extends Thread {
        SessionStarter sessionStarter;

        ProtocolFactory protocolFactory;

        /**
         * Number of accepts performed. Infinite if < 0.
         */
        int times = -1;

        boolean executed_times = false;

        /**
         * @param sessionStarter
         */
        public ServerThread(SessionStarter sessionStarter,
                ProtocolFactory protocolFactory) {
            this.sessionStarter = sessionStarter;
            this.protocolFactory = protocolFactory;
        }

        public void run() {
            while (true) {
                try {
                    Protocol protocol = protocolFactory.createProtocol();
                    System.out.println(getName() + ": accepting on ..."
                            + sessionStarter.getLocalSessionId());
                    System.out.println(getName() + ": accepted "
                            + protocol.accept(sessionStarter));
                    new ProtocolThread(protocol).start();

                    if (times >= 0) {
                        if (--times == 0) {
                            executed_times = true;
                            sessionStarter.close();
                            return;
                        }
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * For incoming sessions.
     */
    SessionStarter sessionStarterIn;

    /**
     * Used for sessions.
     */
    SessionId serverSessionId;

    /**
     * Used to create a SessionStarter.
     */
    SessionStarterTable sessionStarterTable = new IMCSessionStarterTable();

    /**
     * The identifier for the connection protocol (default "tcp").
     */
    String connectionProtocolId = "tcp";

    public static void main(String[] args) {
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        serverSessionId = createSessionId(9999);
        sessionStarterIn = createSessionStarterForAccept();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            System.out.println("TEARDOWN");
            sessionStarterIn.close();
            System.out.println("TORNDOWN");

        } catch (ProtocolException e) {
            /* it may have been already closed */
        }
    }

    ProtocolFactory echoProtocolFactory() {
        return new ProtocolFactory() {
            public Protocol createProtocol() throws ProtocolException {
                return new Protocol(new EchoProtocolState(Protocol.END));
            }
        };
    }

    SessionStarter createSessionStarterForAccept() throws ProtocolException {
        return sessionStarterTable.createSessionStarter(serverSessionId, null);
    }

    SessionStarter createSessionStarterForConnect() throws ProtocolException {
        return sessionStarterTable.createSessionStarter(null, serverSessionId);
    }

    SessionStarter createSessionStarter(String id) throws ProtocolException {
        return sessionStarterTable.createSessionStarter(id);
    }

    protected SessionId createSessionId(int port) throws UnknownHostException {
        return new IpSessionId("127.0.0.1", port, connectionProtocolId);
    }

    void exec_client() throws ProtocolException {
        try {
            ProtocolStack protocolStack = null;
            protocolStack = connect_client();
            send_and_receive(protocolStack);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    ProtocolStack connect_client() throws ProtocolException {
        SessionStarter sessionStarterOut = createSessionStarterForConnect();
        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.connect(sessionStarterOut);
        System.out.println("connect_client: " + protocolStack.getSession());
        return protocolStack;
    }

    void send_and_receive(ProtocolStack protocolStack)
            throws ProtocolException, IOException {
        String testString = "TEST";
        Marshaler marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine(testString);
        protocolStack.releaseMarshaler(marshaler);
        UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
        String result = unMarshaler.readStringLine();
        System.out.println("received back: " + result);
        assertEquals(result, testString);
        protocolStack.close();
    }

    public void testBindForAccept() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testBindForAccept");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        serverSessionId = createSessionId(9998);
        SessionId boundId = mySessionStarter.bindForAccept(serverSessionId);

        System.out.println("bound session id: " + boundId);
        /*
         * since we use static local ip address, the two ids should be the same
         */
        assertEquals(serverSessionId, boundId);

        ServerThread serverThread = startEchoServerAndClients(mySessionStarter);

        mySessionStarter.close();
        serverThread.join();
    }

    public void testBindForAcceptNullPort() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testBindForAcceptNullPort");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        SessionId boundId = mySessionStarter.bindForAccept(new SessionId(
                connectionProtocolId, "127.0.0.1"));
        serverSessionId = boundId;

        System.out.println("bound session id: " + boundId);

        ServerThread serverThread = startEchoServerAndClients(mySessionStarter);

        mySessionStarter.close();
        serverThread.join();
    }

    /**
     * Makes two bindForAccept with a null port and checks that the two
     * SessionIds are different
     * 
     * @throws UnknownHostException
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testTwoBindForAcceptNullPort() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testTwoBindForAcceptNullPort");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        SessionId boundId = mySessionStarter.bindForAccept(new SessionId(
                connectionProtocolId, "127.0.0.1"));

        System.out.println("bound session id: " + boundId);

        SessionStarter mySessionStarter2 = createSessionStarter(connectionProtocolId);
        SessionId boundId2 = mySessionStarter2.bindForAccept(new SessionId(
                connectionProtocolId, "127.0.0.1"));

        System.out.println("bound session id: " + boundId2);

        assertFalse(boundId.sameId(boundId2));

        mySessionStarter.close();
        mySessionStarter2.close();
    }

    public void testBindForAcceptNull() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testBindForAcceptNull");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        SessionId boundId = mySessionStarter.bindForAccept(null);
        serverSessionId = boundId;

        System.out.println("bound session id: " + boundId);

        ServerThread serverThread = startEchoServerAndClients(mySessionStarter);

        mySessionStarter.close();
        serverThread.join();
    }

    /**
     * Makes two bindForAccept(null) and checks that the two SessionIds are
     * different
     * 
     * @throws UnknownHostException
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testTwoBindForAcceptNull() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testTwoBindForAcceptNull");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        SessionId boundId = mySessionStarter.bindForAccept(null);

        System.out.println("bound session id: " + boundId);

        SessionStarter mySessionStarter2 = createSessionStarter(connectionProtocolId);
        SessionId boundId2 = mySessionStarter2.bindForAccept(null);

        System.out.println("bound session id: " + boundId2);

        assertFalse(boundId.sameId(boundId2));

        mySessionStarter.close();
        mySessionStarter2.close();
    }

    /**
     * Test that tries to use a SessionId that's already in use
     * 
     * @throws ProtocolException
     * @throws UnknownHostException
     */
    public void testSessionIdAlreadyBound() throws ProtocolException,
            UnknownHostException {
        System.out.println("*** testSessionIdAlreadyBound");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        serverSessionId = createSessionId(9998);
        SessionId boundId = mySessionStarter.bindForAccept(serverSessionId);

        System.out.println("bound session id: " + boundId);
        /*
         * since we use static local ip address, the two ids should be the same
         */
        assertEquals(serverSessionId, boundId);

        /* now try to bind the same address with another SessionStarter */
        SessionStarter mySessionStarter2 = createSessionStarter(connectionProtocolId);

        try {
            mySessionStarter2.bindForAccept(serverSessionId);

            /* should not get here */
            fail();
        } catch (SessionIdBindException e) {
            assertEquals("session id: " + serverSessionId, e.getMessage());
        }

        mySessionStarter.close();
    }

    /**
     * Test that tries to bind again a SessionStarter that's already bound.
     * 
     * @throws ProtocolException
     * @throws UnknownHostException
     */
    public void testSessionStarterAlreadyBound() throws ProtocolException,
            UnknownHostException {
        System.out.println("*** testSessionStarterAlreadyBound");
        SessionStarter mySessionStarter = createSessionStarter(connectionProtocolId);
        serverSessionId = createSessionId(9998);
        SessionId boundId = mySessionStarter.bindForAccept(serverSessionId);

        System.out.println("bound session id: " + boundId);
        /*
         * since we use static local ip address, the two ids should be the same
         */
        assertEquals(serverSessionId, boundId);

        try {
            /* now try to bind the same SessionStarter */
            mySessionStarter.bindForAccept(serverSessionId);

            /* should not get here */
            fail();
        } catch (AlreadyBoundSessionStarterException e) {
            assertEquals(serverSessionId.toString(), e.getMessage());
        }

        mySessionStarter.close();
    }

    /**
     * Tests the use of a local session identifier in connection
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     * @throws IOException
     */
    public void testLocalSessionIdConnect() throws ProtocolException,
            InterruptedException, IOException {
        System.out.println("*** testLocalSessionIdConnect");
        ServerThread serverThread = new ServerThread(sessionStarterIn,
                echoProtocolFactory());
        serverThread.start();

        SessionId localId = createSessionId(11000);

        /* specify both the remote and the local address */
        SessionStarter sessionStarterOut = sessionStarterTable
                .createSessionStarter(localId, serverSessionId);

        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.connect(sessionStarterOut);

        System.out.println("session: " + protocolStack.getSession());

        assertEquals(localId, protocolStack.getSession().getLocalEnd());
        assertEquals(serverSessionId, protocolStack.getSession().getRemoteEnd());

        send_and_receive(protocolStack);

        sessionStarterIn.close();
        System.out.println("waiting for server thread to terminate "
                + serverThread.getName());
        serverThread.join();
    }

    private ServerThread startEchoServerAndClients(
            SessionStarter mySessionStarter) {
        ServerThread serverThread = new ServerThread(mySessionStarter,
                echoProtocolFactory());
        serverThread.start();

        EchoClient echoClient1 = new EchoClient();
        EchoClient echoClient2 = new EchoClient();
        EchoClient echoClient3 = new EchoClient();

        echoClient1.start();
        echoClient2.start();
        echoClient3.start();

        try {
            echoClient1.join();
            echoClient2.join();
            echoClient3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertTrue(echoClient1.success);
        assertTrue(echoClient2.success);
        assertTrue(echoClient3.success);
        return serverThread;
    }

    /**
     * tests for SessionStarters collection
     * 
     * @throws UnknownHostException
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testSessionStarters() throws UnknownHostException,
            ProtocolException, InterruptedException {
        System.out.println("*** testSessionStarters");
        SessionStarters sessionStarters = new SessionStarters();

        TcpSessionStarter tcpSessionStarter = new TcpSessionStarter(
                new IpSessionId("localhost", 9999), null);
        TcpSessionStarter tcpSessionStarter2 = new TcpSessionStarter(
                new IpSessionId("localhost", 9999), null);
        TcpSessionStarter tcpSessionStarter3 = new TcpSessionStarter(
                new IpSessionId("localhost", 9999), null);

        sessionStarters.addSessionStarter(tcpSessionStarter);
        sessionStarters.addSessionStarter(tcpSessionStarter2);
        sessionStarters.addSessionStarter(tcpSessionStarter3);

        /* try to remove the second one */
        sessionStarters.removeAndCloseSessionStarter(tcpSessionStarter2);

        /* check that it is not present anymore */
        try {
            sessionStarters.removeAndCloseSessionStarter(tcpSessionStarter2);

            /* must not get here */
            fail();
        } catch (ProtocolException e) {
            assertEquals("SessionStarter not found", e.getMessage());
        }

        /*
         * now start a thread that should perform accept on one of these
         * starters
         */
        SessionStarterThread sessionStarterThread = new SessionStarterThread(
                tcpSessionStarter);

        sessionStarterThread.start();

        /* close the passed session starter... */
        sessionStarters.removeAndCloseSessionStarter(tcpSessionStarter);

        checkSessionStarterThreadFailure(sessionStarterThread);

        /* add another SessionStarter an UDP */
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(
                new IpSessionId("localhost", 9999), null);
        IpSessionId ipSessionId2 = new IpSessionId("localhost", 9998);
        UdpSessionStarter udpSessionStarter2 = new UdpSessionStarter(
                ipSessionId2, null);

        sessionStarters.addSessionStarter(udpSessionStarter);
        sessionStarters.addSessionStarter(udpSessionStarter2);

        /* start threads on the remaining three session starters */
        sessionStarterThread = new SessionStarterThread(tcpSessionStarter3);
        SessionStarterThread sessionStarterThread2 = new SessionStarterThread(
                udpSessionStarter);
        SessionStarterThread sessionStarterThread3 = new SessionStarterThread(
                udpSessionStarter2);

        sessionStarterThread.start();
        sessionStarterThread2.start();
        sessionStarterThread3.start();

        /* now close the SessionStarters */
        sessionStarters.close();

        /* all the threads must have terminated with failures */
        checkSessionStarterThreadFailure(sessionStarterThread);
        checkSessionStarterThreadFailure(sessionStarterThread2);
        checkSessionStarterThreadFailure(sessionStarterThread3);

        /* check that we cannot add a SessionStarter anymore */
        try {
            sessionStarters.addSessionStarter(tcpSessionStarter2);

            /* must not get here */
            fail();
        } catch (ProtocolException e) {
            assertEquals("already closing or closed", e.getMessage());
        }
    }

    /**
     * @param sessionStarterThread
     * @throws InterruptedException
     */
    private void checkSessionStarterThreadFailure(
            SessionStarterThread sessionStarterThread)
            throws InterruptedException {
        sessionStarterThread.join();
        assertFalse(sessionStarterThread.success);
    }

    public void testSimple() throws InterruptedException, ProtocolException {
        System.out.println("*** testSimple");
        ServerThread serverThread = new ServerThread(sessionStarterIn,
                echoProtocolFactory());
        serverThread.start();

        try {
            exec_client();
        } catch (ProtocolException e) {
            fail();
        }

        sessionStarterIn.close();
        serverThread.join();
    }

    public void testThreads() throws InterruptedException, ProtocolException {
        System.err.println("*** testThreads");
        ServerThread serverThread = new ServerThread(sessionStarterIn,
                echoProtocolFactory());
        serverThread.start();

        EchoClient echoClient1 = new EchoClient();
        EchoClient echoClient2 = new EchoClient();
        EchoClient echoClient3 = new EchoClient();

        echoClient1.start();
        echoClient2.start();
        echoClient3.start();

        try {
            echoClient1.join();
            echoClient2.join();
            echoClient3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertTrue(echoClient1.success);
        assertTrue(echoClient2.success);
        assertTrue(echoClient3.success);

        sessionStarterIn.close();
        serverThread.join();
    }

    /**
     * We use a SessionStarter to accept sessions, then we close it.
     * 
     * Further clients must not be able to establish a new session, but client
     * already connected must be able to communicate.
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testThreadsAfterStarterClose() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testThreadsAfterStarterClose");
        ServerThread serverThread = new ServerThread(sessionStarterIn,
                echoProtocolFactory());
        serverThread.times = 3;
        serverThread.start();

        EchoClient2 echoClient1 = new EchoClient2();
        EchoClient2 echoClient2 = new EchoClient2();
        EchoClient2 echoClient3 = new EchoClient2();

        echoClient1.start();
        echoClient2.start();
        echoClient3.start();

        // now wait for the server to stop
        serverThread.join();
        System.out.println("server terminated");

        assertTrue(serverThread.executed_times);

        // another client must not be able to connect now
        EchoClient echoClient = new EchoClient();
        echoClient.start();
        echoClient.join();
        assertFalse(echoClient.success);

        // and let the clients communicate.
        wake_client(echoClient1);
        wake_client(echoClient2);
        wake_client(echoClient3);

        echoClient1.join();
        echoClient2.join();
        echoClient3.join();
        System.out.println("clients terminated");

        assertTrue(echoClient1.success);
        assertTrue(echoClient2.success);
        assertTrue(echoClient3.success);

        sessionStarterIn.close();
    }

    /**
     * We use a SessionStarter for accepting a connection, then we close it,
     * create another one on the same SessionId and we must be able to accept
     * another session (even when existing sessions accepted with the previous
     * SessionStarter are still up).
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testAcceptCloseAccept() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testAcceptCloseAccept");
        ServerThread serverThread = new ServerThread(sessionStarterIn,
                echoProtocolFactory());
        serverThread.times = 3;
        serverThread.start();

        EchoClient2 echoClient1 = new EchoClient2();
        EchoClient2 echoClient2 = new EchoClient2();
        EchoClient2 echoClient3 = new EchoClient2();

        echoClient1.start();
        echoClient2.start();
        echoClient3.start();

        // now wait for the server to stop
        serverThread.join();

        assertTrue(serverThread.executed_times);

        // now we create another SessionStarter using the same SessionId
        sessionStarterIn = createSessionStarterForAccept();
        serverThread = new ServerThread(sessionStarterIn, echoProtocolFactory());
        serverThread.times = 3;
        serverThread.start();

        // another client must be able to connect now
        EchoClient echoClient = new EchoClient();
        echoClient.start();
        echoClient.join();
        assertTrue(echoClient.success);

        // and let the clients communicate.
        wake_client(echoClient1);
        wake_client(echoClient2);
        wake_client(echoClient3);

        echoClient1.join();
        echoClient2.join();
        echoClient3.join();

        assertTrue(echoClient1.success);
        assertTrue(echoClient2.success);
        assertTrue(echoClient3.success);

        sessionStarterIn.close();
    }

    void wake_client(EchoClient2 echoClient2) {
        synchronized (echoClient2) {
            echoClient2.canGo = true;
            echoClient2.notify();
        }
    }
}
