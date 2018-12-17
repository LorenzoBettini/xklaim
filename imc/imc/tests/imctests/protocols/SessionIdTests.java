package imctests.protocols;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.SessionId;

public class SessionIdTests extends TestCase {
    SessionId sid1;

    SessionId sid2;

    SessionId sid3;

    protected void setUp() throws Exception {
        super.setUp();
        sid1 = new IpSessionId("localhost", 9999);
        sid2 = new IpSessionId("localhost", 9999);
        sid3 = new IpSessionId("localhost", 10000);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEquals() {
        assertTrue(sid1.equals(sid2));
        assertFalse(sid1.equals(sid3));
    }

    public void testTreeSet() {
        TreeSet<SessionId> treeSet = new TreeSet<SessionId>();

        assertTrue(treeSet.add(sid1));
        assertFalse(treeSet.add(sid2));
        assertTrue(treeSet.add(sid3));

        System.out.println("TreeSet: " + treeSet);
    }

    public void testParsing() throws UnknownHostException {
        InetSocketAddress inetSocketAddress = null;

        /* specify both */
        inetSocketAddress = IpSessionId.parseAddress("localhost:9999");
        System.out.println("localhost:9999 -> " + inetSocketAddress);
        assertEquals(new InetSocketAddress("localhost", 9999),
                inetSocketAddress);

        /* don't specify the port */
        inetSocketAddress = IpSessionId.parseAddress("localhost");
        System.out.println("localhost -> " + inetSocketAddress);
        assertEquals(new InetSocketAddress("localhost", 0), inetSocketAddress);

        /* don't specify the port, alternative */
        inetSocketAddress = IpSessionId.parseAddress("localhost:");
        System.out.println("localhost: -> " + inetSocketAddress);
        assertEquals(new InetSocketAddress("localhost", 0), inetSocketAddress);

        /* specify only the port */
        inetSocketAddress = IpSessionId.parseAddress(":9999");
        System.out.println(":9999 -> " + inetSocketAddress);
        assertEquals(new InetSocketAddress(9999), inetSocketAddress);

        /*
         * use parseForConnect and check that the wildcard address is replaced
         * with localhost
         */
        inetSocketAddress = IpSessionId.parseAddressForConnect(":9999");
        System.out.println("parseAddressForConnect :9999 -> "
                + inetSocketAddress);
        assertEquals(new InetSocketAddress("127.0.0.1", 9999),
                inetSocketAddress);

        /*
         * use parseForConnect and check that specified address is not replaced
         * with localhost
         */
        inetSocketAddress = IpSessionId
                .parseAddressForConnect("111.111.111.111:9999");
        System.out.println("parseAddressForConnect 111.111.111.111:9999 -> "
                + inetSocketAddress);
        assertEquals(new InetSocketAddress("111.111.111.111", 9999),
                inetSocketAddress);

    }
}
