/*
 * Created on Nov 17, 2005
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.KlavaNoDirectCommunicationsException;
import klava.KlavaPhysicalLocalityException;
import klava.KlavaTimeOutException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.proto.Response;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;

/**
 * Tests some accept/register/login/subscribe with Node and coordinators
 * 
 * @author Lorenzo Bettini
 */
public class AcceptRegisterNodeTest extends ClientServerBase {

    /**
     * The node logs into itself
     * 
     * @throws KlavaException
     * @throws InterruptedException
     * @throws ProtocolException
     */
    public void testNodeLoginsToItself() throws KlavaException,
            InterruptedException, ProtocolException {
        System.out.println("*** testNodeLoginsToItself");

        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(serverLoc,
                serverNode);
        acceptCoordinator.start();

        /* the server node logs into itself */
        serverNode.login(serverLoc);

        acceptCoordinator.join();

        assertTrue(acceptCoordinator.success);

        ProtocolStack protocolStack = serverNode.getNodeStack(serverLoc);
        System.out.println("node stack for " + serverLoc + ": "
                + protocolStack.getSession());
        
        /* now try to use this connection */
        serverNode.out(new Tuple(new KString("foo")), serverLoc);
        assertTrue(serverNode.in_nb(new Tuple(new KString("foo")), serverLoc));
    }

    public void testAcceptMainLocality() throws ProtocolException,
            InterruptedException {
        clientLoginsToServerMainLocality(serverNode, serverLoc, serverListener,
                clientNode, clientListener);
    }

    public void testRegisterMainLocality() throws InterruptedException,
            ProtocolException {
        clientSubscribesToServerMainLocality();
    }

    public void testAccept() throws InterruptedException, KlavaException,
            ProtocolException {
        clientLoginsToServer();
    }

    /**
     * Instead of performing logout, simply close the connection. The LOGOUT
     * event should be generated anyway.
     * 
     * @throws InterruptedException
     * @throws KlavaException
     * @throws IMCException
     */
    public void testBrutalLogout() throws InterruptedException, KlavaException,
            IMCException {
        System.err.println("*** testBrutalLogout");
        DisconnectedCoordinator disconnectedCoordinator = new DisconnectedCoordinator();
        disconnectedCoordinator.start();

        clientLoginsToServer();

        /* now let's close the session */
        System.out.println("now closing connection...");
        clientNode.close();

        /*
         * we cannot be sure that the thread waiting for disconnection did not
         * miss the event, but it's unlikely
         */
        System.out.println("waiting for disconnection...");
        disconnectedCoordinator.join();
        System.out.println("disconnected locality: "
                + disconnectedCoordinator.physicalLocality);

        assertFalse(disconnectedCoordinator.physicalLocality.isFormal());
        assertEquals(clientLoc, disconnectedCoordinator.physicalLocality);
    }

    public void testLogout() throws InterruptedException, KlavaException,
            ProtocolException {
        System.err.println("*** testLogout");
        DisconnectedCoordinator disconnectedCoordinator = new DisconnectedCoordinator();
        disconnectedCoordinator.start();

        clientLoginsToServer();

        /* now let's logout */
        System.out.println("now logging out...");
        boolean logoutResult = clientNode.logout(serverLoc);
        assertTrue(logoutResult);

        /*
         * we cannot be sure that the thread waiting for disconnection did not
         * miss the event, but it's unlikely
         */
        System.out.println("waiting for disconnection...");
        disconnectedCoordinator.join();
        System.out.println("disconnected locality: "
                + disconnectedCoordinator.physicalLocality);

        assertFalse(disconnectedCoordinator.physicalLocality.isFormal());
        assertEquals(clientLoc, disconnectedCoordinator.physicalLocality);
    }

    public void testRegister() throws InterruptedException, ProtocolException {
        System.err.println("*** testRegister");
        clientSubscribesToServer();
    }

    public void testUnsubscribe() throws InterruptedException, KlavaException,
            ProtocolException {
        DisconnectedUnsubscribeCoordinator disconnectedCoordinator = new DisconnectedUnsubscribeCoordinator();
        disconnectedCoordinator.start();

        clientSubscribesToServer();

        /* now let's logout */
        System.out.println("now unsubscribing...");
        boolean logoutResult = clientNode.unsubscribe(serverLoc, clientLogLoc);
        assertTrue(logoutResult);

        /*
         * we cannot be sure that the thread waiting for disconnection did not
         * miss the event, but it's unlikely
         */
        System.out.println("waiting for disconnection...");
        disconnectedCoordinator.join();
        System.out.println("disconnected locality: "
                + disconnectedCoordinator.physicalLocality);
        System.out.println("disconnected logical locality: "
                + disconnectedCoordinator.logicalLocality);

        assertFalse(disconnectedCoordinator.physicalLocality.isFormal());
        assertFalse(disconnectedCoordinator.logicalLocality.isFormal());
        assertEquals(clientLoc, disconnectedCoordinator.physicalLocality);
        assertEquals(clientLogLoc, disconnectedCoordinator.logicalLocality);
    }

    /**
     * Instead of performing unsubscribe, simply close the connection. The
     * UNSUBSCRIBE event should be generated anyway.
     * 
     * @throws InterruptedException
     * @throws KlavaException
     * @throws IMCException
     */
    public void testBrutalUnsubscribe() throws InterruptedException,
            KlavaException, IMCException {
        DisconnectedUnsubscribeCoordinator disconnectedCoordinator = new DisconnectedUnsubscribeCoordinator();
        disconnectedCoordinator.start();

        clientSubscribesToServer();

        /* now let's logout */
        System.out.println("now unsubscribing by closing...");
        clientNode.close();

        /*
         * we cannot be sure that the thread waiting for disconnection did not
         * miss the event, but it's unlikely
         */
        disconnectedCoordinator.join();
        System.out.println("lost locality: "
                + disconnectedCoordinator.physicalLocality);
        System.out.println("lost logical locality: "
                + disconnectedCoordinator.logicalLocality);

        assertFalse(disconnectedCoordinator.physicalLocality.isFormal());
        assertFalse(disconnectedCoordinator.logicalLocality.isFormal());
        assertEquals(clientLoc, disconnectedCoordinator.physicalLocality);
        assertEquals(clientLogLoc, disconnectedCoordinator.logicalLocality);
    }

    /**
     * Must fail due to physical locality exception
     * 
     * @throws InterruptedException
     */
    public void testFailLogin() throws InterruptedException {
        LoginCoordinator loginCoordinator = new LoginCoordinator();

        loginCoordinator.start();

        loginCoordinator.join();

        assertFalse(loginCoordinator.success);
        assertTrue(loginCoordinator.klavaException != null);

        System.out.println("expected exception: "
                + loginCoordinator.klavaException);
        assertEquals(loginCoordinator.klavaException.getMessage(),
                "org.mikado.imc.protocols.ProtocolException: cannot connect to "
                        + serverLoc.toString());
    }

    /**
     * Must fail due to logical locality exception
     * 
     * @throws InterruptedException
     */
    public void testFailLogin2() throws InterruptedException {
        LogicalLocality logicalLocality = new LogicalLocality("foo");
        LoginCoordinator loginCoordinator = new LoginCoordinator(
                logicalLocality);

        loginCoordinator.start();

        loginCoordinator.join();

        assertFalse(loginCoordinator.success);
        assertTrue(loginCoordinator.klavaException != null);

        System.out.println("expected exception: "
                + loginCoordinator.klavaException);
        assertEquals(loginCoordinator.klavaException.getMessage(),
                logicalLocality.toString());
    }

    /**
     * Must fail due to physical locality exception
     * 
     * @throws InterruptedException
     */
    public void testFailSubscribe() throws InterruptedException {
        SubscribeCoordinator subscribeCoordinator = new SubscribeCoordinator();

        subscribeCoordinator.start();

        subscribeCoordinator.join();

        assertFalse(subscribeCoordinator.success);
        assertTrue(subscribeCoordinator.klavaException != null);

        System.out.println("expected exception: "
                + subscribeCoordinator.klavaException);
        assertEquals(subscribeCoordinator.klavaException.getMessage(),
                "org.mikado.imc.protocols.ProtocolException: cannot connect to "
                        + serverLoc.toString());
    }

    /**
     * Must fail due to logical locality exception
     * 
     * @throws InterruptedException
     */
    public void testFailSubscribe2() throws InterruptedException {
        LogicalLocality logicalLocality = new LogicalLocality("foo");
        SubscribeCoordinator subscribeCoordinator = new SubscribeCoordinator(
                logicalLocality);

        subscribeCoordinator.start();

        subscribeCoordinator.join();

        assertFalse(subscribeCoordinator.success);
        assertTrue(subscribeCoordinator.klavaException != null);

        System.out.println("expected exception: "
                + subscribeCoordinator.klavaException);
        assertEquals(subscribeCoordinator.klavaException.getMessage(),
                logicalLocality.toString());
    }

    /**
     * Must fail due to an already existing logical locality
     * 
     * @throws InterruptedException
     */
    public void testFailSubscribe3() throws InterruptedException {
        /* tries to subscribe with an already existing logical locality */
        clientLogLoc = new LogicalLocality("already present");
        RegisterCoordinator registerCoordinator = new RegisterCoordinator();
        SubscribeCoordinator subscribeCoordinator = new SubscribeCoordinator();

        registerCoordinator.start();
        subscribeCoordinator.start();

        registerCoordinator.join();
        subscribeCoordinator.join();

        assertFalse(registerCoordinator.success);
        assertFalse(subscribeCoordinator.success);
        /* it must not have failed due to an exception */
        assertTrue(subscribeCoordinator.klavaException == null);
    }
    
    /**
     * Disconnectes from a server it is not connected to
     * @throws KlavaException 
     */
    public void testFailLogout() throws KlavaException {
        try {
            clientNode.logout(serverLoc);
            
            fail(); // must not get here
        } catch (KlavaNoDirectCommunicationsException e) {
            assertEquals(serverLoc.toString(), e.getMessage());
        }
    }

    public void testFailLocality() throws ProtocolException, KlavaException {
        Tuple tuple = new Tuple(new KString("foo"));

        /* a non existent LogicalLocality */
        LogicalLocality logicalLocality = new LogicalLocality("foo");
        try {
            clientNode.out(tuple, logicalLocality);
        } catch (KlavaLogicalLocalityException e) {
            System.out.println("expected exception: " + e);
            assertEquals(e.getMessage(), logicalLocality.toString());
        }

        /* a non existent PhysicalLocality */
        PhysicalLocality physicalLocality = new PhysicalLocality("127.0.0.1",
                20000);
        try {
            clientNode.out(tuple, physicalLocality);
        } catch (KlavaPhysicalLocalityException e) {
            System.out.println("expected exception: " + e);
            assertEquals(e.getMessage(), "no route to "
                    + physicalLocality.toString());
        }
    }

    public void testTupleOperations() throws InterruptedException,
            ProtocolException, KlavaException {
        clientLoginsToServer();
        performTupleOperations(serverLoc);
    }

    public void testTupleOperations2() throws InterruptedException,
            ProtocolException, KlavaException {
        clientSubscribesToServer();

        performTupleOperations(serverLogLoc);
    }

    /**
     * @param destination
     *            TODO
     * @throws KlavaException
     * @throws ProtocolException
     */
    private void performTupleOperations(Locality destination)
            throws KlavaException, ProtocolException {
        Tuple tuple = new Tuple(new KString("foo"));

        clientNode.out(tuple, destination);

        System.out.println("server tuple space: " + serverNode.getTupleSpace());

        assertTrue(serverNode.getTupleSpace().length() == 1);
        /* check that the tuple inserted is equal to the one sent */
        assertEquals(serverNode.getTupleSpace().getTupleEnumeration()
                .nextElement(), tuple);

        /* now let's try to retrieve the tuple back */
        Tuple template = new Tuple(new KString());
        clientNode.read(template, destination);

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString("foo"));

        template = new Tuple(new KString());
        clientNode.in(template, destination);

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString("foo"));

        /* the following should fail, but it's a non-blockin operation */
        template = new Tuple(new KString());
        assertFalse(clientNode.in_nb(template, destination));

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString());

        /* the following should fail, but it's a non-blockin operation */
        template = new Tuple(new KString());
        assertFalse(clientNode.read_nb(template, destination));

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString());

        /* the following should fail, but it's a timeout operation */
        template = new Tuple(new KString());
        assertFalse(clientNode.read_t(template, destination, 1000));

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString());

        /* the following should fail, but it's a timeout operation */
        template = new Tuple(new KString());
        assertFalse(clientNode.in_t(template, destination, 1000));

        System.out.println("matched tuple: " + template);
        assertEquals(template.getItem(0), new KString());
    }

    public void testResponseTimeout() {
        Response<String> response = new Response<String>();

        try {
            response.waitForResponse(1000);
        } catch (KlavaTimeOutException e) {
            assertTrue(true);
            assertTrue(response.error == null);
            assertTrue(response.responseContent == null);
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testResponseTimeout2() {
        Response<String> response = new Response<String>();
        NotifyResponseThread notifyResponseThread = new NotifyResponseThread(
                response);
        notifyResponseThread.start();

        try {
            response.waitForResponse(10000); // the thread should be able to
            // notify before this time
            // elapses
        } catch (KlavaTimeOutException e) {
            fail();
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(response.error == null);
        assertTrue(response.responseContent != null);
    }
}
