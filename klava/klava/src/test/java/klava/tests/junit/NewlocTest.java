/*
 * Created on Feb 1, 2006
 */
package klava.tests.junit;

import org.mikado.imc.events.EventCollector;
import org.mikado.imc.events.PrintEventListener;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;

/**
 * Tests for newloc
 * 
 * @author Lorenzo Bettini
 */
public class NewlocTest extends TestCase {
    public class NewlocCoordinator extends KlavaNodeCoordinator {

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            out(new Tuple(new KString(getName())), self);
        }

    }

    public class InteractWithNewLocThread extends Thread {
        boolean success = false;

        public void run() {
            try {
                interactWithNewLoc(klavaNode.newloc());
                success = true;
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }
    }

    KlavaNode klavaNode;

    protected void setUp() throws Exception {
        super.setUp();
        klavaNode = new KlavaNode();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        klavaNode.close();
    }

    public void testNewloc() throws KlavaException, InterruptedException {
        PhysicalLocality newloc = klavaNode.newloc();

        interactWithNewLoc(newloc);
    }

    /**
     * Test whether sessions established with newloc are correctly propagated.
     * 
     * @throws KlavaException
     * @throws InterruptedException
     * @throws ProtocolException
     */
    public void testNewlocPropagation() throws KlavaException,
            InterruptedException, ProtocolException {
        System.out.println("*** testNewlocPropagation");
        KlavaNode serverNode = new KlavaNode();
        EventCollector eventCollector = new EventCollector();
        serverNode.addListener(RouteEvent.ROUTE_EVENT, eventCollector);
        serverNode
                .addListener(RouteEvent.ROUTE_EVENT, new PrintEventListener());

        KlavaNode clientNode = new KlavaNode();

        PhysicalLocality clientLoc = serverNode.newloc(clientNode);

        eventCollector.waitForEventNumber(1);
        RouteEvent routeEvent = (RouteEvent) eventCollector
                .getCollectedEvents().elementAt(0);
        assertEquals(routeEvent.destination, clientLoc.getSessionId());

        /*
         * now performs a newloc on the client and check that the session is
         * propagated to the server
         */
        PhysicalLocality newlocLoc = clientNode.newloc();
        eventCollector.waitForEventNumber(2);
        routeEvent = (RouteEvent) eventCollector.getCollectedEvents()
                .elementAt(1);
        assertEquals(routeEvent.destination, newlocLoc.getSessionId());

        /* check that the server can actually reach the newloc of the client */
        ProtocolStack protocolStack = serverNode.getNodeStack(newlocLoc);
        assertTrue(protocolStack != null);
        assertEquals(protocolStack.getSession().getRemoteEnd(), clientLoc
                .getSessionId());
    }

    /**
     * Test whether sessions established with newloc are correctly propagated
     * when loggin to another server.
     * 
     * @throws KlavaException
     * @throws InterruptedException
     * @throws ProtocolException 
     */
    public void testNewlocPropagationLogin() throws KlavaException,
            InterruptedException, ProtocolException {
        System.out.println("*** testNewlocPropagationLogin");
        KlavaNode serverNode = new KlavaNode();
        EventCollector eventCollector = new EventCollector();
        serverNode.addListener(RouteEvent.ROUTE_EVENT, eventCollector);
        serverNode
                .addListener(RouteEvent.ROUTE_EVENT, new PrintEventListener());

        KlavaNode clientNode = new KlavaNode();
        PhysicalLocality newlocLoc = clientNode.newloc();

        /*
         * now log the client to the server and check that the newloc in the
         * client is propagated to the server
         */
        PhysicalLocality clientLoc = serverNode.newloc(clientNode);

        eventCollector.waitForEventNumber(2);
        RouteEvent routeEvent = (RouteEvent) eventCollector
                .getCollectedEvents().elementAt(0);
        assertEquals(routeEvent.destination, clientLoc.getSessionId());

        routeEvent = (RouteEvent) eventCollector.getCollectedEvents()
                .elementAt(1);
        assertEquals(routeEvent.destination, newlocLoc.getSessionId());

        /* check that the server can actually reach the newloc of the client */
        ProtocolStack protocolStack = serverNode.getNodeStack(newlocLoc);
        assertTrue(protocolStack != null);
        assertEquals(protocolStack.getSession().getRemoteEnd(), clientLoc
                .getSessionId());
    }

    private void interactWithNewLoc(PhysicalLocality newloc)
            throws KlavaException {
        System.out.println("newloc: " + newloc);

        /* spawn a process that waits for a tuple in the new node */
        InProcessAtSelf readingProcess = new InProcessAtSelf();

        klavaNode.eval(readingProcess, newloc);

        /*
         * try to put something in the tuple space of the new node
         */
        klavaNode.out(new Tuple(new KString("foo")), newloc);

        /* wait for the reading process to put a tuple back */
        Tuple template = new Tuple(new KString(), new KString());
        klavaNode.in(template, newloc);

        System.out.println("obtained tuple: " + template);

        assertEquals(new KString("foo"), template.getItem(1));
    }

    public void testNewlocWithNodeName() throws KlavaException {
        PhysicalLocality physicalLocality = new PhysicalLocality(
                "tcp-127.0.0.1:9999");
        klavaNode.setNodeName(physicalLocality.toString());
        PhysicalLocality newloc = klavaNode.newloc();

        System.out.println("newloc: " + newloc);

        /*
         * check that the main locality is used in the creation of the brand new
         * session id
         */
        assertTrue(newloc.toString().indexOf(physicalLocality.toString()) >= 0);
    }

    public void testManyNewLocs() throws KlavaException, InterruptedException {
        InteractWithNewLocThread interactWithNewLocThread = new InteractWithNewLocThread();
        InteractWithNewLocThread interactWithNewLocThread2 = new InteractWithNewLocThread();
        InteractWithNewLocThread interactWithNewLocThread3 = new InteractWithNewLocThread();
        InteractWithNewLocThread interactWithNewLocThread4 = new InteractWithNewLocThread();
        InteractWithNewLocThread interactWithNewLocThread5 = new InteractWithNewLocThread();

        interactWithNewLocThread.start();
        interactWithNewLocThread2.start();
        interactWithNewLocThread3.start();
        interactWithNewLocThread4.start();
        interactWithNewLocThread5.start();

        interactWithNewLocThread.join();
        interactWithNewLocThread2.join();
        interactWithNewLocThread3.join();
        interactWithNewLocThread4.join();
        interactWithNewLocThread5.join();

        assertTrue(interactWithNewLocThread.success);
        assertTrue(interactWithNewLocThread2.success);
        assertTrue(interactWithNewLocThread3.success);
        assertTrue(interactWithNewLocThread4.success);
        assertTrue(interactWithNewLocThread5.success);
    }

    public void testNewlocCoordinator() throws KlavaException {
        NewlocCoordinator newlocCoordinator = new NewlocCoordinator();

        PhysicalLocality newloc = klavaNode.newloc(newlocCoordinator);

        Tuple template = new Tuple(new KString());

        klavaNode.in(template, newloc);

        assertEquals(new KString(newlocCoordinator.getName()), template
                .getItem(0));
    }

    public void testNewlocClassName() throws KlavaException {
        String className = NewlocNode.class.getCanonicalName();

        PhysicalLocality newloc = klavaNode.newloc(className, null);

        Tuple template = new Tuple(new KString());

        klavaNode.in(template, newloc);

        System.out.println("node class name: " + className);

        assertEquals(new KString(className), template.getItem(0));
    }

    public void testNewlocNode() throws KlavaException {
        PhysicalLocality newloc = klavaNode.newloc(new NewlocNode());

        Tuple template = new Tuple(new KString());

        klavaNode.in(template, newloc);

        assertEquals(new KString(NewlocNode.class.getCanonicalName()), template
                .getItem(0));
    }
}
