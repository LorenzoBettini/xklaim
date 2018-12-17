package imctests.protocols;

import java.util.Iterator;
import java.util.Vector;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.udp.DatagramDispatcher;
import org.mikado.imc.protocols.udp.UdpSessionStarter;

import junit.framework.TestCase;

/**
 * Specific tests for UDP.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.7 $
 */
public class UdpTests extends TestCase {

    /**
     * Performs accept on a given SessionStarter
     * 
     * @author bettini
     * @version $Revision: 1.7 $
     */
    public class AcceptThread extends Thread {
        UdpSessionStarter udpSessionStarter;

        boolean success = false;

        Session session = null;

        public AcceptThread(UdpSessionStarter udpSessionStarter) {
            this.udpSessionStarter = udpSessionStarter;
        }

        public void run() {
            try {
                session = udpSessionStarter.accept();
                System.out
                        .println(getName() + ": accepted session: " + session);
                success = true;
            } catch (ProtocolException e) {
                e.printStackTrace();
                return;
            }
        }

        public void closeSession() throws ProtocolException {
            if (session != null)
                session.close();
        }
    }

    /**
     * Performs accept on a given SessionStarter in a loop
     * 
     * @author bettini
     * @version $Revision: 1.7 $
     */
    public class AcceptLoopThread extends Thread {
        UdpSessionStarter udpSessionStarter;
        
        Vector<Session> sessions = new Vector<Session>();

        public AcceptLoopThread(UdpSessionStarter udpSessionStarter) {
            this.udpSessionStarter = udpSessionStarter;
        }

        public void run() {
            while (true) {
                try {
                    Session session = udpSessionStarter.accept();
                    System.out.println(getName() + ": accepted session: "
                            + session);
                    sessions.addElement(session);
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        
        public void closeSessions() {
            for (Iterator<Session> iter = sessions.iterator(); iter.hasNext();) {
                Session element = iter.next();
                try {
                    element.close();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Simple accept/connect
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testSingleAccept() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testSingleAccept ***");
        SessionId sessionId = new SessionId("udp", "127.0.0.1:9999");

        /* these are used for accept */
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(sessionId,
                null);
        AcceptThread acceptThread = new AcceptThread(udpSessionStarter);
        acceptThread.start();;

        /* this one's used for connect */
        UdpSessionStarter connectStarter = new UdpSessionStarter(null,
                sessionId);

        Session session = connectStarter.connect();
        System.out.println("connected session: " + session);

        acceptThread.join();

        /* one and only one must have succeeded */
        assertTrue(acceptThread.success);

        connectStarter.close();
        udpSessionStarter.close();

        /*
         * we must also close the already established session otherwise there'll
         * still be an associated DatagramDispatcher
         */
        acceptThread.closeSession();

        /* now there must not be any association for the SessionId */
        DatagramDispatcher datagramDispatcher = udpSessionStarter
                .getDatagramDispatcherTable().getDispatcher(sessionId);
        assertTrue(datagramDispatcher == null);
    }

    /**
     * Closes the server connection and checks that the client senses that
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testClose() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testClose ***");
        SessionId sessionId = new SessionId("udp", "127.0.0.1:9999");

        /* these are used for accept */
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(sessionId,
                null);
        AcceptThread acceptThread = new AcceptThread(udpSessionStarter);
        acceptThread.start();;

        /* this one's used for connect */
        UdpSessionStarter connectStarter = new UdpSessionStarter(null,
                sessionId);

        Session session = connectStarter.connect();
        System.out.println("connected session: " + session);

        acceptThread.join();

        ProtocolStack protocolStack = new ProtocolStack(session.getProtocolLayer());
        
        /* one and only one must have succeeded */
        assertTrue(acceptThread.success);

        /*
         * we must also close the already established session 
         */
        acceptThread.closeSession();
       
        /* check that the protocolStack is not usable */
        try {
            protocolStack.createUnMarshaler();
            fail(); // must not get here
        } catch (ProtocolException e) {
            assertEquals("closed", e.getMessage());
        }

        udpSessionStarter.close();
        /* now there must not be any association for the SessionId */
        DatagramDispatcher datagramDispatcher = udpSessionStarter
                .getDatagramDispatcherTable().getDispatcher(sessionId);
        assertTrue(datagramDispatcher == null);
    }
    
    /**
     * Try to bind two UdpSessionStarter on the same SessionId (one of them must
     * fail)
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testAlreadyAccepting() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testAlreadyAccepting ***");
        SessionId sessionId = new SessionId("udp", "127.0.0.1:9999");

        /* these are used for accept */
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(sessionId,
                null);
        UdpSessionStarter udpSessionStarter2 = new UdpSessionStarter(sessionId,
                null);

        AcceptThread acceptThread = new AcceptThread(udpSessionStarter);
        AcceptThread acceptThread2 = new AcceptThread(udpSessionStarter2);

        /*
         * one of these two threads must fail since the other one is already
         * accepting
         */
        acceptThread.start();
        acceptThread2.start();

        /* this one's used for connect */
        UdpSessionStarter connectStarter = new UdpSessionStarter(null,
                sessionId);

        Session session = connectStarter.connect();
        System.out.println("connected session: " + session);

        acceptThread.join();
        acceptThread2.join();

        /* one and only one must have succeeded */
        assertTrue((acceptThread.success && !acceptThread2.success)
                || (acceptThread2.success && !acceptThread.success));

        connectStarter.close();
        udpSessionStarter.close();
        udpSessionStarter2.close();

        /*
         * we must also close the already established session otherwise there'll
         * still be an associated DatagramDispatcher
         */
        acceptThread.closeSession();
        acceptThread2.closeSession();

        /* now there must not be any association for the SessionId */
        DatagramDispatcher datagramDispatcher = udpSessionStarter
                .getDatagramDispatcherTable().getDispatcher(sessionId);
        assertTrue(datagramDispatcher == null);
    }

    /**
     * A UDPSessionStarter is created for accepting on a SessionId, then it is
     * closed and another one is created on the same SessionId
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testAcceptCloseAccept() throws ProtocolException,
            InterruptedException {
        System.err.println("*** testAcceptCloseAccept ***");
        SessionId sessionId = new SessionId("udp", "127.0.0.1:9999");

        /* used for accept */
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(sessionId,
                null);

        AcceptLoopThread acceptThread = new AcceptLoopThread(udpSessionStarter);

        /*
         * one of these two threads must fail since the other one is already
         * accepting
         */
        acceptThread.start();

        /* this one's used for connect */
        UdpSessionStarter connectStarter = new UdpSessionStarter(null,
                sessionId);

        Session session = connectStarter.connect();
        System.out.println("connected session: " + session);

        udpSessionStarter.close();
        acceptThread.join();

        /*
         * the DatagramDispatcherTable must still have an association for the
         * SessionId. Because a connection is still up
         */
        DatagramDispatcher datagramDispatcher = udpSessionStarter
                .getDatagramDispatcherTable().getDispatcher(sessionId);

        assertTrue(datagramDispatcher != null);

        /*
         * now we create a new UdpSessionStarter on the same SessionId which now
         * should be available again
         */
        /* used for accept */
        udpSessionStarter = new UdpSessionStarter(sessionId, null);

        AcceptLoopThread acceptThread2 = new AcceptLoopThread(udpSessionStarter);
        acceptThread2.start();

        /*
         * create another connection just to be sure that the thread is
         * accepting
         */

        /* this one's used for connect */
        UdpSessionStarter connectStarter2 = new UdpSessionStarter(null,
                sessionId);

        Session session2 = connectStarter2.connect();
        System.out.println("connected session: " + session2);

        /*
         * now the DatagramDispatcher used for accept must be the same as before
         */
        assertSame(datagramDispatcher, udpSessionStarter
                .getDatagramDispatcher());

        connectStarter.close();
        connectStarter2.close();
        udpSessionStarter.close();
        acceptThread2.join();
        
        /*
         * we must also close the already established session otherwise there'll
         * still be an associated DatagramDispatcher
         */
        acceptThread.closeSessions();
        acceptThread2.closeSessions();
        
        /* now there must not be any association for the SessionId */
        datagramDispatcher = udpSessionStarter
                .getDatagramDispatcherTable().getDispatcher(sessionId);
        assertTrue(datagramDispatcher == null);
    }
}
