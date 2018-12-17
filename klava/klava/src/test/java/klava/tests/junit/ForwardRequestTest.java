/*
 * Created on Nov 25, 2005
 */
package klava.tests.junit;

import java.io.IOException;

import klava.EnvironmentLogicalLocalityResolver;
import klava.KBoolean;
import klava.KInteger;
import klava.KString;
import klava.KlavaException;
import klava.KlavaPhysicalLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.events.LoginSubscribeEvent;
import klava.proto.Response;
import klava.proto.ResponseState;
import klava.proto.TupleOpState;
import klava.proto.TuplePacket;
import klava.proto.TupleResponse;
import klava.topology.KlavaNode;
import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventCollector;
import org.mikado.imc.events.LogEventListener;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.events.WaitForEventQueueListener;
import org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactoryVerbose;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;

/**
 * Tests for forwarding requests.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ForwardRequestTest extends ClientServerBase {
    /**
     * Verbosity for loaded classes.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class KlavaNodeVerbose extends KlavaNode {

        /**
         * @see klava.topology.KlavaNode#createMigratingCodeFactory()
         */
        @Override
        protected MigratingCodeFactory createMigratingCodeFactory() {
            return new JavaByteCodeMigratingCodeFactoryVerbose();
        }

    }

    KlavaNode serverNode2;

    PhysicalLocality serverLoc2;

    LogicalLocality serverLogLoc2;

    LogEventListener serverListener2;

    LogEventListener serverRouteListener2;

    KlavaNode serverNode3;

    PhysicalLocality serverLoc3;

    LogicalLocality serverLogLoc3;

    LogEventListener serverListener3;

    LogEventListener serverRouteListener3;

    KlavaNode serverNode4;

    PhysicalLocality serverLoc4;

    LogicalLocality serverLogLoc4;

    LogEventListener serverListener4;

    LogEventListener serverRouteListener4;

    WaitForEventQueueListener waitForEventQueueListenerClient;

    WaitForEventQueueListener waitForEventQueueListenerServer;

    WaitForEventQueueListener waitForEventQueueListenerServer2;

    WaitForEventQueueListener waitForEventQueueListenerServer3;

    WaitForEventQueueListener waitForEventQueueListenerServer4;

    protected void setUp() throws Exception {
        super.setUp();
        serverNode2 = createKlavaNode();
        serverLoc2 = new PhysicalLocality("127.0.0.1", 9998);
        serverLogLoc2 = new LogicalLocality("server2");
        serverListener2 = new LogEventListener();
        serverRouteListener2 = new LogEventListener();
        serverNode2.addListener(LoginSubscribeEvent.LOGIN_EVENT,
                serverListener2);
        serverNode2.addListener(LoginSubscribeEvent.SUBSCRIBE_EVENT,
                serverListener2);
        serverNode2.addListener(RouteEvent.ROUTE_EVENT, serverRouteListener2);

        serverNode3 = createKlavaNode();
        serverLoc3 = new PhysicalLocality("127.0.0.1", 9997);
        serverLogLoc3 = new LogicalLocality("server3");
        serverListener3 = new LogEventListener();
        serverRouteListener3 = new LogEventListener();
        serverNode3.addListener(LoginSubscribeEvent.LOGIN_EVENT,
                serverListener3);
        serverNode3.addListener(LoginSubscribeEvent.SUBSCRIBE_EVENT,
                serverListener3);
        serverNode3.addListener(RouteEvent.ROUTE_EVENT, serverRouteListener3);

        serverNode4 = createKlavaNode();
        serverLoc4 = new PhysicalLocality("127.0.0.1", 9996);
        serverLogLoc4 = new LogicalLocality("server4");
        serverListener4 = new LogEventListener();
        serverRouteListener4 = new LogEventListener();
        serverNode4.addListener(LoginSubscribeEvent.LOGIN_EVENT,
                serverListener4);
        serverNode4.addListener(LoginSubscribeEvent.SUBSCRIBE_EVENT,
                serverListener4);
        serverNode4.addListener(RouteEvent.ROUTE_EVENT, serverRouteListener4);

        waitForEventQueueListenerClient = new WaitForEventQueueListener();
        clientNode.getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                waitForEventQueueListenerClient);
        waitForEventQueueListenerServer = new WaitForEventQueueListener();
        serverNode.getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                waitForEventQueueListenerServer);
        waitForEventQueueListenerServer2 = new WaitForEventQueueListener();
        serverNode2.getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                waitForEventQueueListenerServer2);
        waitForEventQueueListenerServer3 = new WaitForEventQueueListener();
        serverNode3.getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                waitForEventQueueListenerServer3);
        waitForEventQueueListenerServer4 = new WaitForEventQueueListener();
        serverNode4.getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                waitForEventQueueListenerServer4);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        serverNode2.close();
        serverNode3.close();
    }

    /**
     * Make everyone connect to server (by using the main physical locality)
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void testAcceptMainLocalityMultiple() throws ProtocolException,
            InterruptedException {
        clientLoginsToServerMainLocality(serverNode, serverLoc, serverListener,
                clientNode, clientListener);
        clientLoginsToServerMainLocality(serverNode, serverLoc, serverListener,
                serverNode2, serverListener2);
        clientLoginsToServerMainLocality(serverNode, serverLoc, serverListener,
                serverNode3, serverListener3);
        clientLoginsToServerMainLocality(serverNode, serverLoc, serverListener,
                serverNode4, serverListener4);
    }

    /**
     * This is the simple net
     * 
     * <pre>
     *                server
     *              /        \
     *             /          \
     *          client1     client3
     *                          \
     *                           \
     *                         client2
     * </pre>
     * 
     * @throws KlavaException
     * @throws IMCException
     * @throws InterruptedException
     */
    public void testSimpleClientServerNet() throws KlavaException,
            IMCException, InterruptedException {
        System.out.println("*** testSimpleClientServerNet");
        KlavaNode server = createKlavaNode();
        EventCollector eventCollector = new EventCollector();
        server.addListener(RouteEvent.ROUTE_EVENT, eventCollector);

        KlavaNode client1 = createKlavaNode();
        KlavaNode client2 = createKlavaNode();
        KlavaNode client3 = createKlavaNode();

        PhysicalLocality client2Loc = client3.newloc(client2);

        PhysicalLocality client1Loc = server.newloc(client1);
        PhysicalLocality client3Loc = server.newloc(client3);

        System.out.println("client1: " + client1Loc);
        System.out.println("client2: " + client2Loc);
        System.out.println("client3: " + client3Loc);

        eventCollector.waitForEventNumber(3);

        System.out.println("server route events: "
                + eventCollector.getCollectedEvents());

        /*
         * client1 is not connected directly to client2, so we pass through the
         * server
         */
        client1.out(new Tuple("foo"), client2Loc);

        assertTrue(client2.read_nb(new Tuple("foo"), KlavaNode.self));
        assertTrue(client1.read_nb(new Tuple("foo"), client2Loc));

        /* spawn a remote process to look for the tuple */
        client1.eval(new InOutProcess(KlavaNode.self, client1Loc, new Tuple(
                String.class)), client2Loc);

        assertTrue(client1.in_t(new Tuple("foo"), 3000));

        // this should fail, since we're searching for an absent tuple, in a location
        // that we can reach
        assertFalse(client1.read_nb(new Tuple("foo222"), client2Loc));

        server.close();
    }

    /**
     * The final network topology is
     * 
     * <pre>
     *                                                      server4 (9996)
     *                                                         |
     *                                                      server3 (9997)
     *                                                         |
     *                                       (9999) server   server2 (9998)
     *                                                  \     /
     *                                                   \   /
     *                                                   client
     * </pre>
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     * @throws KlavaException
     */
    public void testLocalityResolver() throws ProtocolException,
            InterruptedException, KlavaException, IOException {
        clientLoginsToServer();

        /* we map this logical locality only on the server */
        LogicalLocality locality = new LogicalLocality("only on server");
        serverNode.addToEnvironment(locality, serverLoc);

        /* the client must not be able to solve it itself */
        assertTrue(clientNode.getEnvironment().toPhysical(locality) == null);
        assertTrue(serverNode.getEnvironment().toPhysical(locality) != null);

        /*
         * this first version won't be able to resolve it since it knows nothing
         * about server connections
         */
        EnvironmentLogicalLocalityResolver environmentLogicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                clientNode.getEnvironment(), null, null);

        checkFailResolution(locality, environmentLogicalLocalityResolver);

        /*
         * this version should be able to request the resolution to the server
         */

        environmentLogicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                clientNode.getEnvironment(),
                clientNode.getSessionManagers().outgoingSessionManager,
                clientNode.getWaitingForLocality());

        checkOkResolution(locality, serverLoc,
                environmentLogicalLocalityResolver);

        /* we map this logical locality only on the second server */
        locality = new LogicalLocality("only on server 2");
        serverNode2.addToEnvironment(locality, serverLoc2);

        /* the client must not be able to solve it itself */
        assertTrue(clientNode.getEnvironment().toPhysical(locality) == null);
        assertTrue(serverNode2.getEnvironment().toPhysical(locality) != null);

        /*
         * this first version won't be able to resolve it since it knows nothing
         * about server connections
         */
        environmentLogicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                clientNode.getEnvironment(), null, null);

        checkFailResolution(locality, environmentLogicalLocalityResolver);

        /*
         * this version requests the resolution to the server which knows
         * nothing about it
         */
        environmentLogicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                clientNode.getEnvironment(),
                clientNode.getSessionManagers().outgoingSessionManager,
                clientNode.getWaitingForLocality());

        checkFailResolution(locality, environmentLogicalLocalityResolver);

        /*
         * save the client physical locality logged at server, since it will be
         * overwritten when client logs to server2
         */
        PhysicalLocality clientToServerLoc = new PhysicalLocality(clientLoc);

        /* now logs to server 2 */
        clientLoginsToServer(serverNode2, serverLoc2, serverListener2,
                serverRouteListener2);

        /* now should find it at the server 2 */
        checkOkResolution(locality, serverLoc2,
                environmentLogicalLocalityResolver);

        /* we map this logical locality only on the third server */
        locality = new LogicalLocality("only on server 3");
        serverNode3.addToEnvironment(locality, serverLoc3);

        /*
         * this version requests the resolution to the server and server 2 which
         * know nothing about it
         */
        environmentLogicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                clientNode.getEnvironment(),
                clientNode.getSessionManagers().outgoingSessionManager,
                clientNode.getWaitingForLocality());

        checkFailResolution(locality, environmentLogicalLocalityResolver);

        /*
         * save the client physical locality logged at server2, since it will be
         * overwritten when server2 logs to server3
         */
        PhysicalLocality clientToServer2Loc = new PhysicalLocality(clientLoc);

        /* now server 2 logs to server 3 */
        clientLoginsToServer(serverNode3, serverLoc3, serverListener3,
                serverRouteListener3, serverNode2, serverListener2,
                serverRouteListener2);

        /* now should find it at the server 3 */
        checkOkResolution(locality, serverLoc3,
                environmentLogicalLocalityResolver);

        /*
         * the locality with which server2 is logged at server3
         */
        PhysicalLocality server2ToServer3Loc = new PhysicalLocality(clientLoc);

        /* make sure that all locality propagation events are delivered */
        waitForRouteEvents();

        printRoutingTables();

        /*
         * server must have a (direct) route only to client and an indirect
         * route to server2, passing through client. While it cannot know about
         * server3
         */
        ProtocolStack clientStack = serverNode.getRoutingTable()
                .getProtocolStack(clientToServerLoc.getSessionId());
        assertTrue(clientStack != null);
        ProtocolStack server2Stack = serverNode.getRoutingTable()
                .getProtocolStack(serverLoc2.getSessionId());
        assertTrue(server2Stack != null);
        assertTrue(server2Stack == clientStack);
        ProtocolStack server3Stack = serverNode.getRoutingTable()
                .getProtocolStack(serverLoc3.getSessionId());
        assertTrue(server3Stack == null);

        /*
         * client must have a (direct) route to server and server2, but not
         * server3
         */
        ProtocolStack serverStack = clientNode.getRoutingTable()
                .getProtocolStack(serverLoc.getSessionId());
        assertTrue(serverStack != null);
        serverStack = clientNode.getRoutingTable().getProtocolStack(
                serverLoc2.getSessionId());
        assertTrue(serverStack != null);
        serverStack = clientNode.getRoutingTable().getProtocolStack(
                serverLoc3.getSessionId());
        assertTrue(serverStack == null);

        /*
         * server2 must have a (direct) route to client and to server3 and an
         * indirect route to server, passing through client
         */
        clientStack = serverNode2.getRoutingTable().getProtocolStack(
                clientToServer2Loc.getSessionId());
        assertTrue(clientStack != null);
        server3Stack = serverNode2.getRoutingTable().getProtocolStack(
                serverLoc3.getSessionId());
        assertTrue(server3Stack != null);
        server2Stack = serverNode2.getRoutingTable().getProtocolStack(
                serverLoc.getSessionId());
        assertTrue(server2Stack != null);
        assertTrue(server2Stack == clientStack);

        /*
         * server3 must have a (direct) route to server2 and indirect routes to
         * client and server, passing through server2
         */
        server2Stack = serverNode3.getRoutingTable().getProtocolStack(
                server2ToServer3Loc.getSessionId());
        assertTrue(server2Stack != null);
        serverStack = serverNode3.getRoutingTable().getProtocolStack(
                serverLoc.getSessionId());
        assertTrue(serverStack != null);
        assertTrue(serverStack == server2Stack);
        clientStack = serverNode3.getRoutingTable().getProtocolStack(
                clientToServer2Loc.getSessionId());
        assertTrue(clientStack != null);
        assertTrue(clientStack == server2Stack);

        /*
         * now server2 sends to server3 a response for client. server3 should be
         * able to forward it to client (passing back through server2)
         */
        System.out.println("send to server3 a response for client");
        String processName = "foo";
        Response<String> response = new Response<String>();
        WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                processName);
        waitingForResponseProcess.response = response;
        clientNode.getWaitingForOkResponse().put(processName, response);
        waitingForResponseProcess.start();
        ResponseState.sendResponseOkError(server3Stack, TuplePacket.OUT_S,
                ResponseState.OK_S, clientToServer2Loc.getSessionId(),
                processName);
        waitingForResponseProcess.join();

        System.out.println("response: " + response.responseContent);
        assertTrue(response.error == null);
        assertTrue(response.responseContent != null);
        assertEquals("OK", response.responseContent);

        /*
         * this should fail silently (server3 has not route for the specified
         * session id)
         */
        IpSessionId bogus = new IpSessionId("localhost", 50000);
        PhysicalLocality bogusLocality = new PhysicalLocality(bogus);
        ResponseState.sendResponseOkError(server3Stack, TuplePacket.OUT_S,
                ResponseState.OK_S, bogus, processName);

        /*
         * now server2 sends to server3 a tuple op response for client. server3
         * should be able to forward it to client (passing back through server2)
         */
        System.out.println("send to server3 a tuple response for client");

        /* the response shared with the thread */
        TupleResponse tupleResponse = new TupleResponse();

        /* the concurrent thread that'll wait for the response */
        WaitingForResponseProcess<TupleResponse> waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                processName);
        waitingForTupleProcess.response = tupleResponse;
        clientNode.getWaitingForTuple().put(processName, tupleResponse);
        waitingForTupleProcess.start();

        Tuple tuple = new Tuple(new KString("bar"));

        /*
         * we use the locality of the server2 logged at server3 as the source of
         * this response
         */
        ResponseState.sendResponseTuple(server3Stack, TuplePacket.IN_S, tuple,
                server2ToServer3Loc.getSessionId(), clientToServer2Loc
                        .getSessionId(), processName, true);

        waitingForTupleProcess.join();

        assertTrue(tupleResponse.error == null);
        assertTrue(tupleResponse.responseContent != null);

        System.out.println("tuple response: " + tupleResponse.responseContent);

        assertEquals(tupleResponse.responseContent, tuple);

        /*
         * now server2 sends to server3 a tuple op (IN) response for an unknown
         * destination. server3 should put the tuple back to server2.
         */
        Tuple tuple2 = new Tuple(new KString()); // formal
        /* make sure there's no such tuple before in server2 */
        assertFalse(serverNode2.getTupleSpace().read_nb(tuple2));

        /* send the bogus response */
        ResponseState.sendResponseTuple(server3Stack, TuplePacket.IN_S, tuple,
                server2ToServer3Loc.getSessionId(), bogus, processName, true);

        /*
         * now the tuple should be put back in server2 so we should be able to
         * retrieve it
         */
        serverNode2.getTupleSpace().in(tuple2);

        System.out.println("tuple put back: " + tuple2);
        assertTrue(tuple2.match(tuple));

        /*
         * server2 sends a tuple operation to server3, but the real destination
         * is client (as it is logged to server2), and server3 should be able to
         * forward it to client.
         */
        tuple = new Tuple(new KInteger(20));

        /* server2 should receive the response of the OUT */
        response = new Response<String>();
        waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                processName);
        waitingForResponseProcess.response = response;
        serverNode2.getWaitingForOkResponse().put(processName, response);
        waitingForResponseProcess.start();

        TuplePacket tuplePacket = new TuplePacket(clientToServer2Loc,
                serverLoc2, TuplePacket.OUT_S, tuple);
        tuplePacket.processName = processName;
        TupleOpState.writePacket(server3Stack, tuplePacket);

        /* wait for the response in server2 */
        waitingForResponseProcess.join();

        System.out.println("response: " + response.responseContent);
        assertTrue(response.error == null);
        assertTrue(response.responseContent != null);
        assertEquals("OK", response.responseContent);

        /*
         * now the tuple should be put in client so we should be able to
         * retrieve it
         */
        tuple2 = new Tuple(new KInteger()); // formal
        clientNode.getTupleSpace().in(tuple2);

        /*
         * server2 sends a tuple operation to server3, but the real destination
         * is a bogus locality that server3 should not be able to forward to, so
         * it must notify server2 about that.
         */
        tuple = new Tuple(new KBoolean(true));

        /* server2 should receive the (FAIL) response of the OUT */
        response = new Response<String>();
        waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                processName);
        waitingForResponseProcess.response = response;
        serverNode2.getWaitingForOkResponse().put(processName, response);
        waitingForResponseProcess.start();

        tuplePacket = new TuplePacket(new PhysicalLocality(bogus),
                server2ToServer3Loc, TuplePacket.OUT_S, tuple);
        tuplePacket.processName = processName;
        TupleOpState.writePacket(server3Stack, tuplePacket);

        /* wait for the response in server2 */
        waitingForResponseProcess.join();

        System.out.println("response: " + response);
        assertTrue(response.error != null);
        assertEquals(response.error, ResponseState.FAIL_S);

        /*
         * now the tuple should not be put in client so we should not be able to
         * retrieve it
         */
        KBoolean b = new KBoolean(); // formal
        tuple2 = new Tuple(b);
        assertFalse(clientNode.getTupleSpace().in_nb(tuple2));

        /*
         * server2 sends a tuple operation to server3, but the real destination
         * is a bogus locality that server3 should not be able to forward to, so
         * it must notify server2 about that. This time we use the method from
         * the class KlavaNode
         */
        tuple = new Tuple(new KBoolean(true));

        /* server2 should receive the (FAIL) response of the OUT */
        response = new Response<String>();

        /* this will block this very thread */
        serverNode2.tupleOperation(server3Stack, TuplePacket.OUT_S, tuple,
                true, serverNode2.getWaitingForOkResponse(), response, -1,
                new PhysicalLocality(bogus));

        System.out.println("response: " + response);
        assertTrue(response.error != null);
        assertEquals(response.error, ResponseState.FAIL_S);

        /*
         * now the tuple should not be put in client so we should not be able to
         * retrieve it
         */
        b = new KBoolean(); // formal
        tuple2 = new Tuple(b);
        assertFalse(clientNode.getTupleSpace().in_nb(tuple2));

        System.out.println("retrieved from client tuple: " + tuple2);
        assertTrue(b.isFormal());

        /*
         * now client tries to send a tuple to server3, and it knows nothing
         * about it, so it should try to query all the nodes it is connected to.
         */
        tuple = new Tuple(new KString("for "), serverLoc3);
        System.out.println("performing out(" + tuple + ")@" + serverLoc3);
        clientNode.out(tuple, serverLoc3);

        /* check that such tuple is actually at server */
        KString s = new KString();
        PhysicalLocality l = new PhysicalLocality();
        tuple2 = new Tuple(s, l);
        assertTrue(serverNode3.getTupleSpace().in_nb(tuple2));

        System.out.println("tuple at server3: " + tuple2);

        assertFalse(s.isFormal());
        assertFalse(l.isFormal());

        /*
         * now client tries to send a tuple to a bogus node, and it knows
         * nothing about it, so it should try to query all the nodes it is
         * connected to, but eventually it should fail.
         */
        tuple = new Tuple(new KString("for "), bogusLocality);
        System.out.println("performing out(" + tuple + ")@" + bogus);

        try {
            clientNode.out(tuple, bogusLocality);

            /* should not get here */
            fail();
        } catch (KlavaException e) {
            e.printStackTrace();

            assertTrue(e instanceof KlavaPhysicalLocalityException);
            assertEquals(e.getMessage(), "no route to " + bogusLocality);
        }

        /* now server3 logs into server4 */
        clientLoginsToServer(serverNode4, serverLoc4, serverListener4,
                serverRouteListener4, serverNode3, serverListener3,
                serverRouteListener3);

        /*
         * the locality with which server3 is logged at server4
         */
        PhysicalLocality server3ToServer4Loc = new PhysicalLocality(clientLoc);

        System.out.println("server 4:");
        waitForRouteEvents(waitForEventQueueListenerServer4, 4);
        printRoutingTable("server 4", serverNode4);

        /*
         * server4 must have a (direct) route to server3 and indirect routes to
         * client and server, server2, passing through server3
         */
        server3Stack = serverNode4.getRoutingTable().getProtocolStack(
                server3ToServer4Loc.getSessionId());
        assertTrue(server3Stack != null);
        serverStack = serverNode4.getRoutingTable().getProtocolStack(
                serverLoc.getSessionId());
        assertTrue(serverStack != null);
        assertTrue(serverStack == server3Stack);
        server2Stack = serverNode4.getRoutingTable().getProtocolStack(
                server2ToServer3Loc.getSessionId());
        assertTrue(server2Stack != null);
        assertTrue(server2Stack == server3Stack);
        clientStack = serverNode4.getRoutingTable().getProtocolStack(
                clientToServer2Loc.getSessionId());
        assertTrue(clientStack != null);
        assertTrue(clientStack == server3Stack);

        /* server2 must not know server4 */
        ProtocolStack server4Stack = serverNode2.getRoutingTable()
                .getProtocolStack(serverLoc4.getSessionId());
        assertTrue(server4Stack == null);

        /*
         * now client tries to send a tuple to to server4, and it knows nothing
         * about it, so it should try to query all the nodes it is connected to.
         */
        tuple = new Tuple(new KString("for "), serverLoc4);
        System.out.println("performing out(" + tuple + ")@" + serverLoc4);

        clientNode.out(tuple, serverLoc4);

        /* check that such tuple is actually at server */
        s = new KString();
        l = new PhysicalLocality();
        tuple2 = new Tuple(s, l);
        assertTrue(serverNode4.getTupleSpace().in_nb(tuple2));

        System.out.println("tuple at server4: " + tuple2);

        assertFalse(s.isFormal());
        assertFalse(l.isFormal());

        /*
         * now test the disconnection and the update of routing tables in the
         * nodes.
         */

        /* disconnect client from server2 */
        System.out.println("client disconnects from server2...");
        clientNode.logout(serverLoc2);

        System.out.println("server:");
        waitForRouteEvents(waitForEventQueueListenerServer, 1);
        System.out.println("server2:");
        waitForRouteEvents(waitForEventQueueListenerServer2, 1);
        System.out.println("server3:");
        waitForRouteEvents(waitForEventQueueListenerServer3, 2);

        printRoutingTables();
    }

    private void printRoutingTables() {
        printRoutingTable("server", serverNode);
        printRoutingTable("client", clientNode);
        printRoutingTable("server2", serverNode2);
        printRoutingTable("server3", serverNode3);
    }

    void waitForRouteEvents() throws InterruptedException {
        System.out.println("server:");
        waitForRouteEvents(waitForEventQueueListenerServer, 2);
        System.out.println("server 2:");
        waitForRouteEvents(waitForEventQueueListenerServer2, 3);
        System.out.println("server 3:");
        waitForRouteEvents(waitForEventQueueListenerServer3, 3);
    }

    void waitForRouteEvents(
            WaitForEventQueueListener waitForEventQueueListener, int times)
            throws InterruptedException {
        for (int i = 0; i < times; ++i)
            System.out.println("EVENT: "
                    + waitForEventQueueListener.waitForEvent());
    }

    void printRoutingTable(String name, KlavaNode klavaNode) {
        System.out.println(name + ":");
        System.out.println(klavaNode.getRoutingTable().toString());
    }

    void checkOkResolution(
            LogicalLocality locality,
            PhysicalLocality expected,
            EnvironmentLogicalLocalityResolver environmentLogicalLocalityResolver) {
        PhysicalLocality physicalLocality;
        try {
            physicalLocality = environmentLogicalLocalityResolver
                    .resolve(locality);

            System.out.println("obtained locality: " + physicalLocality);

            assertEquals(expected, physicalLocality);
        } catch (KlavaException e) {
            e.printStackTrace();
            fail(); // must not get here
        }
    }

    void checkFailResolution(
            LogicalLocality locality,
            EnvironmentLogicalLocalityResolver environmentLogicalLocalityResolver) {
        try {
            environmentLogicalLocalityResolver.resolve(locality);
            fail(); // must not get here
        } catch (KlavaException e) {
            System.out.println("exception: " + e);
            /* check that it is really the exception we wait for */
            assertEquals(e.getMessage(), locality.toString());
        }
    }

    /**
     * Factory method for node creation.
     * 
     * @return the created KlavaNode
     */
    @Override
    protected KlavaNode createKlavaNode() {
        KlavaNode klavaNode = new KlavaNodeVerbose();

        return klavaNode;
    }
}
