/*
 * Created on Dec 16, 2005
 */
package imctests.topology;

import java.net.UnknownHostException;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.topology.RoutingTable;

import junit.framework.TestCase;

/**
 * Tests for the RoutingTable
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class RoutingTableTest extends TestCase {
    RoutingTable routingTable;

    IpSessionId ipSessionId1, ipSessionId2, ipSessionId3, ipSessionId4,
            ipSessionId5, ipSessionId6;
    
    ProtocolStack protocolStack12, protocolStack34; 

    protected void setUp() throws Exception {
        super.setUp();
        routingTable = new RoutingTable();
        
        /* used for direct routes */
        ipSessionId1 = new IpSessionId("localhost", 10001);
        ipSessionId2 = new IpSessionId("localhost", 10002);
        ipSessionId3 = new IpSessionId("localhost", 10003);
        ipSessionId4 = new IpSessionId("localhost", 10004);

        /* used for indirect routes */
        ipSessionId5 = new IpSessionId("localhost", 20005);
        ipSessionId6 = new IpSessionId("localhost", 20006);

        /* all the stacks share the same fake protocol layer */
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();

        protocolStack12 = new ProtocolStack(
                protocolLayerSharedBuffer);
        protocolStack12
                .setSession(new Session(null, ipSessionId1, ipSessionId2));

        protocolStack34 = new ProtocolStack(
                protocolLayerSharedBuffer);
        protocolStack34
                .setSession(new Session(null, ipSessionId3, ipSessionId4));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTable() throws UnknownHostException, ProtocolException {
        ProtocolStack protocolStack = null;

        routingTable.addRoute(ipSessionId2, protocolStack12);
        protocolStack = routingTable.getProtocolStack(ipSessionId2);
        assertTrue(protocolStack != null);
        assertTrue(protocolStack == protocolStack12);

        routingTable.addRoute(ipSessionId4, protocolStack34);
        protocolStack = routingTable.getProtocolStack(ipSessionId4);
        assertTrue(protocolStack != null);
        assertTrue(protocolStack == protocolStack34);

        /* make a snapshot for later use */
        RoutingTable routingTable2 = new RoutingTable(routingTable);

        checkInitialState();

        System.out.println("routing table: " + routingTable);

        /* if we remove 4 we must lose the routes to 5 and 6 as well */
        routingTable.removeRoute(ipSessionId4);

        System.out.println("routing table: " + routingTable);

        assertTrue(routingTable.getProxy(ipSessionId2) == ipSessionId2);
        assertTrue(routingTable.getProxy(ipSessionId4) == null);
        assertTrue(routingTable.getProxy(ipSessionId5) == null);
        assertTrue(routingTable.getProxy(ipSessionId6) == null);
        assertFalse(routingTable.hasRoute(ipSessionId4));
        assertFalse(routingTable.hasRoute(ipSessionId5));
        assertFalse(routingTable.hasRoute(ipSessionId6));

        /* restore the original table */
        routingTable = routingTable2;

        /* and check that everything is like before */
        checkInitialState();

        System.out.println("routing table: " + routingTable);
        
        /* if we remove 5, then 4 must not be a proxy for 5 anymore */
        routingTable.removeRoute(ipSessionId5);
        
        System.out.println("routing table: " + routingTable);
        
        assertFalse(routingTable.hasRoute(ipSessionId5));
        assertTrue(routingTable.getProxy(ipSessionId5) == null);
        
        
    }

    /**
     * @throws ProtocolException
     */
    private void checkInitialState() throws ProtocolException {
        /* we have a direct route to 2 and 4 */
        assertTrue(routingTable.getProxy(ipSessionId2) == ipSessionId2);
        assertTrue(routingTable.getProxy(ipSessionId4) == ipSessionId4);

        /* 4 has a route for 5, thus we have a route for 5 using session 3->4 */
        assertTrue(routingTable.addRoute(ipSessionId5, ipSessionId4,
                protocolStack34));
        assertTrue(routingTable.getProxy(ipSessionId5) == ipSessionId4);

        /* 4 has a route for 6, thus we have a route for 6 using session 3->4 */
        assertTrue(routingTable.addRoute(ipSessionId6, ipSessionId4,
                protocolStack34));
        assertTrue(routingTable.getProxy(ipSessionId6) == ipSessionId4);
    }
}
