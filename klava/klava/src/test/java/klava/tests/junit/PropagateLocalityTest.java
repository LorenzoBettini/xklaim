/*
 * Created on Oct 17, 2005
 */
package klava.tests.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.LogEventListener;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.topology.RoutingTable;


import junit.framework.TestCase;
import klava.proto.PropagateLocalityState;

/**
 * Tests the propagation of adding and removing locality, i.e.,
 * PropagateLocalityState
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class PropagateLocalityTest extends TestCase {

    ByteArrayOutputStream out;

    ByteArrayInputStream in;

    ProtocolStack protocolStackOut;

    ProtocolLayerEndPoint protocolLayerOut;

    ProtocolStack protocolStackIn;

    RoutingTable routingTable;

    LogEventListener logEventListener;

    EventManager eventManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        out = new ByteArrayOutputStream();
        protocolLayerOut = new ProtocolLayerEndPoint(null,
                new IMCMarshaler(out));
        protocolStackOut = new ProtocolStack(protocolLayerOut);
        /*
         * we must simulate a real network connection, thus we initialize the
         * stacks with fake sessions
         */
        protocolStackOut.setSession(new Session(protocolLayerOut,
                new IpSessionId("localhost", 21000), new IpSessionId(
                        "localhost", 30000)));

        routingTable = new RoutingTable();

        logEventListener = new LogEventListener();
        eventManager = new EventManager();

        routingTable.setEventManager(eventManager);

        eventManager.addListener(RouteEvent.ROUTE_EVENT, logEventListener);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void initialiazeForRead() throws UnknownHostException, ProtocolException {
        in = new ByteArrayInputStream(out.toByteArray());
        protocolStackIn = new ProtocolStack(new ProtocolLayerEndPoint(
                new IMCUnMarshaler(in), null));
        /*
         * we must simulate a real network connection, thus we initialize the
         * stacks with fake sessions
         */
        protocolStackIn.setSession(new Session(null,
                new IpSessionId("localhost", 31000), new IpSessionId(
                        "localhost", 40000)));
    }

    public void testAddLocalities() throws UnknownHostException {
        Vector<SessionId> vector = new Vector<SessionId>();

        vector.add(new IpSessionId("localhost", 10000));
        vector.add(new IpSessionId("localhost", 10001));
        vector.add(new IpSessionId("localhost", 10002));

        IpSessionId route = new IpSessionId("localhost", 9999);

        try {
            PropagateLocalityState.propagateAddLocalities(protocolStackOut,
                    vector);

            initialiazeForRead();

            PropagateLocalityState propagateLocalityState = new PropagateLocalityState(
                    routingTable);
            propagateLocalityState.setEventManager(eventManager);

            // the added stack is dummy
            routingTable.addRoute(route, protocolStackOut);

            propagateLocalityState.setProtocolStack(protocolStackIn);
            propagateLocalityState.enter(null, new TransmissionChannel(
                    new IMCUnMarshaler(in)));

            assertTrue(routingTable
                    .hasRoute(new IpSessionId("localhost", 10000)));
            assertTrue(routingTable
                    .hasRoute(new IpSessionId("localhost", 10001)));
            assertTrue(routingTable
                    .hasRoute(new IpSessionId("localhost", 10002)));

            assertFalse(routingTable.hasRoute(new IpSessionId("localhost",
                    20000)));

            String events = logEventListener.toString();
            System.out.println("Events: " + events);

            assertTrue(events.indexOf(new IpSessionId("localhost", 10000)
                    .toString()) >= 0);
            assertTrue(events.indexOf(new IpSessionId("localhost", 10001)
                    .toString()) >= 0);
            assertTrue(events.indexOf(new IpSessionId("localhost", 10002)
                    .toString()) >= 0);
            assertFalse(events.indexOf(new IpSessionId("localhost", 20000)
                    .toString()) >= 0);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testRemoveLocalities() throws UnknownHostException, ProtocolException {
        Vector<SessionId> vector = new Vector<SessionId>();

        vector.add(new IpSessionId("localhost", 10000));
        vector.add(new IpSessionId("localhost", 10001));
        vector.add(new IpSessionId("localhost", 10002));

        SessionId route = protocolStackOut.getSession().getRemoteEnd();

        try {
            PropagateLocalityState.propagateRemoveLocalities(protocolStackOut,
                    new IpSessionId("localhost", 10001));

            initialiazeForRead();

            PropagateLocalityState propagateLocalityState = new PropagateLocalityState(
                    routingTable);
            propagateLocalityState.setEventManager(eventManager);

            routingTable.setEventManager(null); // suspend events
            // the added stack is dummy
            routingTable.addRoute(route, protocolStackOut);
            assertTrue(routingTable.addRoute(new IpSessionId("localhost", 10000), route, protocolStackOut));
            assertTrue(routingTable.addRoute(new IpSessionId("localhost", 10001), route, protocolStackOut));
            assertTrue(routingTable.addRoute(new IpSessionId("localhost", 10002), route, protocolStackOut));

            routingTable.setEventManager(eventManager); // enable events

            propagateLocalityState.enter(null, new TransmissionChannel(
                    new IMCUnMarshaler(in)));

            assertTrue(routingTable
                    .hasRoute(new IpSessionId("localhost", 10000)));
            assertTrue(routingTable
                    .hasRoute(new IpSessionId("localhost", 10002)));

            // the following has been removed
            assertFalse(routingTable.hasRoute(new IpSessionId("localhost",
                    10001)));

            // the following has never been inserted
            assertFalse(routingTable.hasRoute(new IpSessionId("localhost",
                    20000)));

            String events = logEventListener.toString();
            System.out.println("Events: " + events);

            assertFalse(events.indexOf(new IpSessionId("localhost", 10000)
                    .toString()) >= 0);
            assertTrue(events.indexOf(new IpSessionId("localhost", 10001)
                    .toString()) >= 0);
            assertFalse(events.indexOf(new IpSessionId("localhost", 10002)
                    .toString()) >= 0);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
