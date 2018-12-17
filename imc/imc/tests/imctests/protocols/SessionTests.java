package imctests.protocols;

import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.pipe.LocalSessionStarter;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.SessionManagers;

public class SessionTests extends TestCase {
    SessionId sid1;

    SessionId sid2;

    SessionId sid3;

    protected void setUp() throws Exception {
        super.setUp();
        sid1 = new IpSessionId("localhost", 9999);
        sid2 = new IpSessionId("localhost", 9998);
        sid3 = new IpSessionId("localhost", 10000);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSessionContainsId() throws UnknownHostException {
        Session session = new Session(null, sid1, sid3);

        assertTrue(session.containsSessionId(sid1));
        assertTrue(session
                .containsSessionId(new IpSessionId("localhost", 9999)));
        assertTrue(session.containsSessionId(sid3));

        SessionId sid4 = new IpSessionId("localhost", 10001);

        assertFalse(session.containsSessionId(sid4));
    }

    public void testCloseSessions() throws ProtocolException,
            UnknownHostException {
        SessionManager sessionManager = new SessionManager();

        Session session = new Session(null, sid1, sid3);
        Session session2 = new Session(null, sid2, sid3);

        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.setSession(session);
        ProtocolStack protocolStack2 = new ProtocolStack();
        protocolStack2.setSession(session2);

        sessionManager.addSession(protocolStack);
        sessionManager.addSession(protocolStack2);

        assertFalse(session.isClosed());
        assertFalse(session2.isClosed());

        /*
         * now we should remove only the second session since it involves sid2
         */
        sessionManager.closeSessions(sid2);

        assertFalse(session.isClosed());
        assertTrue(session2.isClosed());

        /* add it again */
        session2 = new Session(null, sid2, sid3);
        protocolStack2 = new ProtocolStack();
        protocolStack2.setSession(session2);
        assertTrue(sessionManager.addSession(protocolStack2));

        /* this should not remove anything */
        sessionManager.closeSessions(new IpSessionId("localhost", 11000));
        assertFalse(session.isClosed());
        assertFalse(session2.isClosed());

        /*
         * this should remove both since it involves sid3
         */
        sessionManager.closeSessions(sid3);

        assertTrue(session.isClosed());
        assertTrue(session2.isClosed());

        assertEquals(0, sessionManager.sessionSize());
    }

    public void testCloseSessions2() throws ProtocolException,
            UnknownHostException {
        SessionManagers sessionManagers = new SessionManagers();

        LocalSessionStarter localSessionStarter = new LocalSessionStarter();
        localSessionStarter.bindForAccept(sid3);
        
        Session session = new Session(null, sid1, sid3);
        Session session2 = new Session(null, sid2, sid3);

        ProtocolStack protocolStack = new ProtocolStack();
        protocolStack.setSession(session);
        ProtocolStack protocolStack2 = new ProtocolStack();
        protocolStack2.setSession(session2);

        sessionManagers.incomingSessionManager.addSession(protocolStack);
        sessionManagers.outgoingSessionManager.addSession(protocolStack2);

        sessionManagers.incomingSessionManager.addSessionStarter(localSessionStarter);
        
        assertFalse(session.isClosed());
        assertFalse(session2.isClosed());
        assertFalse(localSessionStarter.isClosed());

        /*
         * now we should remove only the second session since it involves sid2
         */
        sessionManagers.closeSessions(sid2);

        assertFalse(session.isClosed());
        assertTrue(session2.isClosed());
        assertFalse(localSessionStarter.isClosed());

        /* add it again */
        session2 = new Session(null, sid2, sid3);
        protocolStack2 = new ProtocolStack();
        protocolStack2.setSession(session2);
        assertTrue(sessionManagers.outgoingSessionManager
                .addSession(protocolStack2));

        /* this should not remove anything */
        sessionManagers.closeSessions(new IpSessionId("localhost", 11000));
        assertFalse(session.isClosed());
        assertFalse(session2.isClosed());
        assertFalse(localSessionStarter.isClosed());

        /*
         * this should remove both since it involves sid3
         */
        sessionManagers.closeSessions(sid3);

        assertTrue(session.isClosed());
        assertTrue(session2.isClosed());
        assertTrue(localSessionStarter.isClosed());

        assertEquals(0, sessionManagers.outgoingSessionManager.sessionSize());
        assertEquals(0, sessionManagers.incomingSessionManager.sessionSize());
        
        
    }
}
