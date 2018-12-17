/*
 * Created on Nov 24, 2005
 */
package klava.tests.junit;

import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.LogEventListener;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.topology.RoutingTable;

import junit.framework.TestCase;
import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.events.LoginSubscribeEvent;
import klava.proto.Response;
import klava.topology.KlavaNode;

/**
 * A base class to be used for tests with one client and one server.
 * 
 * @author Lorenzo Bettini
 */
public abstract class ClientServerBase extends TestCase {

    public class AcceptCoordinator extends Thread {
        boolean success;

        PhysicalLocality server = serverLoc;

        KlavaNode serverN = serverNode;

        public void run() {
            try {
                success = serverN.accept(server, clientLoc);
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }

        /**
         * @param server
         */
        public AcceptCoordinator(PhysicalLocality server, KlavaNode serverN) {
            this.server = server;
            this.serverN = serverN;
        }

        public AcceptCoordinator() {
        }
    }

    /**
     * Performs an accept without specifying the local locality (should use the
     * main physical locality)
     * 
     * @author Lorenzo Bettini
     */
    public class AcceptCoordinatorMainLocality extends Thread {
        boolean success;

        KlavaNode serverN = serverNode;

        KlavaException klavaException = null;

        public void run() {
            try {
                success = serverN.accept(clientLoc);
            } catch (KlavaException e) {
                e.printStackTrace();
                klavaException = e;
            }
        }

        /**
         * @param server
         */
        public AcceptCoordinatorMainLocality(KlavaNode serverN) {
            this.serverN = serverN;
        }
    }

    public class DisconnectedCoordinator extends Thread {
        PhysicalLocality physicalLocality = new PhysicalLocality();

        public void run() {
            try {
                serverNode.disconnected(physicalLocality);
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }
    }

    public class DisconnectedUnsubscribeCoordinator extends
            DisconnectedCoordinator {
        LogicalLocality logicalLocality = new LogicalLocality();

        public void run() {
            try {
                serverNode.disconnected(physicalLocality, logicalLocality);
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }
    }

    public class LoginCoordinator extends Thread {
        boolean success = false;

        KlavaException klavaException = null;

        Locality remote = serverLoc;

        KlavaNode client = clientNode;

        /**
         * @param remote
         */
        public LoginCoordinator(Locality remote) {
            this.remote = remote;
        }

        /**
         * @param client
         * @param remote
         */
        public LoginCoordinator(KlavaNode client, Locality remote) {
            this.client = client;
            this.remote = remote;
        }

        public LoginCoordinator() {

        }

        public void run() {
            try {
                success = client.login(remote);
            } catch (KlavaException e) {
                klavaException = e;
            }
        }
    }

    public class RegisterCoordinator extends Thread {
        boolean success;

        public void run() {
            try {
                success = serverNode
                        .register(serverLoc, clientLoc, clientLogLocVar);
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs an accept without specifying the local locality (should use the
     * main physical locality)
     * 
     * @author Lorenzo Bettini
     */
    public class RegisterCoordinatorMainLocality extends Thread {
        boolean success;

        KlavaException klavaException = null;

        public void run() {
            try {
                success = serverNode.register(clientLoc, clientLogLocVar);
            } catch (KlavaException e) {
                e.printStackTrace();
                klavaException = e;
            }
        }
    }

    public class SubscribeCoordinator extends Thread {
        boolean success = false;

        Locality remote = serverLoc;

        KlavaException klavaException = null;

        /**
         * @param remote
         */
        public SubscribeCoordinator(Locality remote) {
            this.remote = remote;
        }

        public SubscribeCoordinator() {
        }

        public void run() {
            try {
                success = clientNode.subscribe(remote, clientLogLoc);
            } catch (KlavaException e) {
                klavaException = e;
            }
        }
    }

    public class NotifyResponseThread extends Thread {
        Response<String> response;

        /**
         * @param response
         */
        public NotifyResponseThread(Response<String> response) {
            this.response = response;
        }

        public void run() {
            synchronized (response) {
                response.responseContent = "OK";
                response.notifyAll();
            }
        }
    }

    KlavaNode serverNode;

    KlavaNode clientNode;

    PhysicalLocality serverLoc;

    /**
     * This is the variable that will be set, by the AcceptCoordinator, with the
     * PhysicalLocality of the client that has just logged in.
     */
    PhysicalLocality clientLoc;

    LogicalLocality serverLogLoc;

    LogicalLocality clientLogLoc;

    LogicalLocality clientLogLocVar;

    LogEventListener serverListener;

    LogEventListener serverRouteListener;

    LogEventListener clientListener;

    LogEventListener clientRouteListener;

    LogicalLocality self = KlavaNode.self;

    public static void main(String[] args) {
    }

    protected void setUp() throws Exception {
        super.setUp();
        serverNode = createKlavaNode();
        serverNode.addToEnvironment(new LogicalLocality("already present"),
                new PhysicalLocality("127.0.0.1", 21000));
        clientNode = createKlavaNode();
        serverLoc = new PhysicalLocality("127.0.0.1", 9999);
        clientLoc = new PhysicalLocality();
        serverLogLoc = new LogicalLocality("server");
        clientLogLoc = new LogicalLocality("client");
        clientLogLocVar = new LogicalLocality();
        clientNode.addToEnvironment(serverLogLoc, serverLoc);
        serverListener = new LogEventListener();
        clientListener = new LogEventListener();
        serverRouteListener = new LogEventListener();
        clientRouteListener = new LogEventListener();
        clientNode.addListener(LoginSubscribeEvent.LOGIN_EVENT, clientListener);
        clientNode.addListener(LoginSubscribeEvent.SUBSCRIBE_EVENT,
                clientListener);
        clientNode.addListener(RouteEvent.ROUTE_EVENT, clientRouteListener);
        serverNode.addListener(LoginSubscribeEvent.LOGIN_EVENT, serverListener);
        serverNode.addListener(LoginSubscribeEvent.SUBSCRIBE_EVENT,
                serverListener);
        serverNode.addListener(RouteEvent.ROUTE_EVENT, serverRouteListener);
    }

    /**
     * Factory method for node creation.
     * 
     * @return the created KlavaNode
     */
    protected KlavaNode createKlavaNode() {
        return new KlavaNode();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        serverNode.close();
        clientNode.close();
    }

    protected void clientLoginsToServer() throws InterruptedException,
            ProtocolException {
        clientLoginsToServer(serverNode, serverLoc, serverListener,
                serverRouteListener);
    }

    protected void clientLoginsToServer(KlavaNode serverNode,
            PhysicalLocality serverLoc, EventListener serverListener,
            EventListener serverRouteListener) throws InterruptedException,
            ProtocolException {
        clientLoginsToServer(serverNode, serverLoc, serverListener,
                serverRouteListener, clientNode, clientListener,
                clientRouteListener);
    }

    protected void clientLoginsToServer(KlavaNode serverN,
            PhysicalLocality server, EventListener serverListener,
            EventListener serverRouteListener, KlavaNode clientNode,
            EventListener clientListener, EventListener clientRouteListener)
            throws InterruptedException, ProtocolException {
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(server,
                serverN);
        LoginCoordinator loginCoordinator = new LoginCoordinator(clientNode,
                server);

        acceptCoordinator.start();
        loginCoordinator.start();

        acceptCoordinator.join();
        loginCoordinator.join();

        System.out.println("client loc: " + clientLoc);
        System.out.println("server loc: " + server);
        assertTrue(!clientLoc.isFormal());

        System.out.println("client events: " + clientListener);
        System.out.println("client route events: " + clientRouteListener);
        System.out.println("client routing table: "
                + clientNode.getRoutingTable());
        System.out.println("server events: " + serverListener);
        System.out.println("server route events: " + serverRouteListener);
        System.out
                .println("server routing table: " + serverN.getRoutingTable());

        checkRoutingTableConsistency(clientNode.getRoutingTable());
        checkRoutingTableConsistency(serverN.getRoutingTable());

        String clientEvents = clientListener.toString();
        String serverEvents = serverListener.toString();
        String serverRouteEvents = serverRouteListener.toString();
        String clientRouteEvents = clientRouteListener.toString();

        /* check that the events were created correctly */
        ProtocolStack serverStack = clientNode.getNodeStack(server);
        assertTrue(serverStack != null);
        LoginSubscribeEvent loginSubscribeEvent = new LoginSubscribeEvent(this,
                serverStack.getSession());
        assertTrue(clientEvents.indexOf(loginSubscribeEvent.toString()) >= 0);
        assertTrue(clientRouteEvents.indexOf(server.toString()) >= 0);

        ProtocolStack clientStack = serverN.getNodeStack(clientLoc);
        assertTrue(clientStack != null);
        loginSubscribeEvent = new LoginSubscribeEvent(this, clientStack
                .getSession());
        assertTrue(serverEvents.indexOf(loginSubscribeEvent.toString()) >= 0);
        assertTrue(serverRouteEvents.indexOf(clientLoc.toString()) >= 0);
    }

    protected void clientLoginsToServerMainLocality(KlavaNode serverNode,
            PhysicalLocality serverLoc, EventListener serverListener,
            KlavaNode clientNode, EventListener clientListener)
            throws InterruptedException, ProtocolException {
        serverNode.setMainPhysicalLocality(serverLoc);
        AcceptCoordinatorMainLocality acceptCoordinator = new AcceptCoordinatorMainLocality(
                serverNode);
        LoginCoordinator loginCoordinator = new LoginCoordinator(clientNode,
                serverNode.getMainPhysicalLocality());

        acceptCoordinator.start();
        loginCoordinator.start();

        acceptCoordinator.join();
        loginCoordinator.join();

        System.out.println("client loc: " + clientLoc);
        System.out.println("server loc: "
                + serverNode.getMainPhysicalLocality());
        assertTrue(!clientLoc.isFormal());

        System.out.println("client events: " + clientListener);
        System.out.println("client routing table: "
                + clientNode.getRoutingTable());
        System.out.println("server events: " + serverListener);
        System.out.println("server routing table: "
                + serverNode.getRoutingTable());

        checkRoutingTableConsistency(clientNode.getRoutingTable());
        checkRoutingTableConsistency(serverNode.getRoutingTable());

        String clientEvents = clientListener.toString();
        String serverEvents = serverListener.toString();

        /* check that the events were created correctly */
        ProtocolStack serverStack = clientNode.getNodeStack(serverLoc);
        assertTrue(serverStack != null);
        LoginSubscribeEvent loginSubscribeEvent = new LoginSubscribeEvent(this,
                serverStack.getSession());
        assertTrue(clientEvents.indexOf(loginSubscribeEvent.toString()) >= 0);

        ProtocolStack clientStack = serverNode.getNodeStack(clientLoc);
        assertTrue(clientStack != null);
        loginSubscribeEvent = new LoginSubscribeEvent(this, clientStack
                .getSession());
        assertTrue(serverEvents.indexOf(loginSubscribeEvent.toString()) >= 0);
    }

    protected void clientSubscribesToServer() throws InterruptedException,
            ProtocolException {
        RegisterCoordinator registerCoordinator = new RegisterCoordinator();
        SubscribeCoordinator subscribeCoordinator = new SubscribeCoordinator();

        registerCoordinator.start();
        subscribeCoordinator.start();

        registerCoordinator.join();
        subscribeCoordinator.join();

        System.out.println("client loc: " + clientLoc);
        assertTrue(!clientLoc.isFormal());

        System.out.println("client log loc: " + clientLogLocVar);
        assertTrue(!clientLogLocVar.isFormal());
        assertEquals(clientLogLocVar, clientLogLoc);

        PhysicalLocality physicalLocality = serverNode.getEnvironment()
                .toPhysical(clientLogLocVar);
        assertTrue(physicalLocality != null);
        assertEquals(physicalLocality, clientLoc);

        System.out.println("client events: " + clientListener);
        System.out.println("server events: " + serverListener);

        String clientEvents = clientListener.toString();
        String serverEvents = serverListener.toString();

        /* check that the events were created correctly */
        ProtocolStack serverStack = clientNode.getNodeStack(serverLoc);
        assertTrue(serverStack != null);
        LoginSubscribeEvent loginSubscribeEvent = new LoginSubscribeEvent(this,
                serverStack.getSession(), clientLogLoc);
        assertTrue(clientEvents.indexOf(loginSubscribeEvent.toString()) >= 0);

        ProtocolStack clientStack = serverNode.getNodeStack(clientLoc);
        assertTrue(clientStack != null);
        loginSubscribeEvent = new LoginSubscribeEvent(this, clientStack
                .getSession(), clientLogLoc);
        assertTrue(serverEvents.indexOf(loginSubscribeEvent.toString()) >= 0);
    }

    protected void clientSubscribesToServerMainLocality() throws InterruptedException,
            ProtocolException {
        
        serverNode.setMainPhysicalLocality(serverLoc);
        
        RegisterCoordinatorMainLocality registerCoordinator = new RegisterCoordinatorMainLocality();
        SubscribeCoordinator subscribeCoordinator = new SubscribeCoordinator();

        registerCoordinator.start();
        subscribeCoordinator.start();

        registerCoordinator.join();
        subscribeCoordinator.join();

        System.out.println("client loc: " + clientLoc);
        assertTrue(!clientLoc.isFormal());

        System.out.println("client log loc: " + clientLogLocVar);
        assertTrue(!clientLogLocVar.isFormal());
        assertEquals(clientLogLocVar, clientLogLoc);

        PhysicalLocality physicalLocality = serverNode.getEnvironment()
                .toPhysical(clientLogLocVar);
        assertTrue(physicalLocality != null);
        assertEquals(physicalLocality, clientLoc);

        System.out.println("client events: " + clientListener);
        System.out.println("server events: " + serverListener);

        String clientEvents = clientListener.toString();
        String serverEvents = serverListener.toString();

        /* check that the events were created correctly */
        ProtocolStack serverStack = clientNode.getNodeStack(serverLoc);
        assertTrue(serverStack != null);
        LoginSubscribeEvent loginSubscribeEvent = new LoginSubscribeEvent(this,
                serverStack.getSession(), clientLogLoc);
        assertTrue(clientEvents.indexOf(loginSubscribeEvent.toString()) >= 0);

        ProtocolStack clientStack = serverNode.getNodeStack(clientLoc);
        assertTrue(clientStack != null);
        loginSubscribeEvent = new LoginSubscribeEvent(this, clientStack
                .getSession(), clientLogLoc);
        assertTrue(serverEvents.indexOf(loginSubscribeEvent.toString()) >= 0);
    }

    protected void checkRoutingTableConsistency(RoutingTable routingTable)
            throws ProtocolException {
        assertTrue(routingTable.checkForConsistency());
    }
}
