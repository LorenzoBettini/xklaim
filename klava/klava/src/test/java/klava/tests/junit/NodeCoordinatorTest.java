/*
 * Created on Jan 23, 2006
 */
package klava.tests.junit;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.AcceptNodeCoordinator;
import klava.topology.KlavaNode;
import klava.topology.RegisterNodeCoordinator;

import org.mikado.imc.common.IMCException;


/**
 * Tests for node coordinators
 * 
 * @author Lorenzo Bettini
 */
public class NodeCoordinatorTest extends ClientServerBase {

    public class AcceptCoordinator extends AcceptNodeCoordinator {
        PhysicalLocality remote = new PhysicalLocality();

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public AcceptCoordinator(PhysicalLocality physicalLocality) {
            super(physicalLocality);
        }

        public AcceptCoordinator() {
        }

        /**
         * @see klava.topology.AcceptNodeCoordinator#success(klava.PhysicalLocality)
         */
        @Override
        protected void success(PhysicalLocality remote) throws KlavaException {
            this.remote.setValue(remote);

            /*
             * insert the remote locality as a tuple into the local tuple space
             */
            out(new Tuple(remote));
        }
    }

    public class RegisterCoordinator extends RegisterNodeCoordinator {
        PhysicalLocality remote = new PhysicalLocality();

        LogicalLocality remoteLogical = new LogicalLocality();

        public RegisterCoordinator(PhysicalLocality physicalLocality) {
            super(physicalLocality);
        }

        public RegisterCoordinator() {
            super();
        }

        /**
         * @see klava.topology.RegisterNodeCoordinator#success(klava.PhysicalLocality,
         *      klava.LogicalLocality)
         */
        @Override
        protected void success(PhysicalLocality remote,
                LogicalLocality logicalLocality) throws KlavaException {
            this.remote.setValue(remote);
            remoteLogical.setValue(logicalLocality);

            /*
             * insert the remote locality as a tuple into the local tuple space
             */
            out(new Tuple(remote, logicalLocality));
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleLogin() throws IMCException, InterruptedException,
            KlavaException {
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(serverLoc);
        acceptCoordinator.remote = clientLoc;
        serverNode.addNodeCoordinator(acceptCoordinator);

        assertTrue(clientNode.login(serverLoc));

        acceptCoordinator.join();
        System.out.println("client: " + acceptCoordinator.remote);
        assertFalse(clientLoc.isFormal());

        Tuple template = new Tuple(new PhysicalLocality());
        assertTrue(serverNode.in_nb(template));
        assertEquals(clientLoc, template.getItem(0));
    }

    public void testSimpleLoginMainLocality() throws IMCException,
            InterruptedException, KlavaException {
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator();
        acceptCoordinator.remote = clientLoc;
        serverNode.setMainPhysicalLocality(serverLoc);
        serverNode.addNodeCoordinator(acceptCoordinator);

        assertTrue(clientNode.login(serverLoc));

        acceptCoordinator.join();
        System.out.println("client: " + acceptCoordinator.remote);
        assertFalse(clientLoc.isFormal());

        Tuple template = new Tuple(new PhysicalLocality());
        assertTrue(serverNode.in_nb(template));
        assertEquals(clientLoc, template.getItem(0));
    }

    public void testSimpleSubscribe() throws IMCException,
            InterruptedException, KlavaException {
        RegisterCoordinator coordinator = new RegisterCoordinator(serverLoc);
        coordinator.remote = clientLoc;
        serverNode.addNodeCoordinator(coordinator);

        assertTrue(clientNode.subscribe(serverLoc, clientLogLoc));

        coordinator.join();
        System.out.println("client: " + coordinator.remote);
        System.out.println("client: " + coordinator.remoteLogical);
        assertFalse(coordinator.remoteLogical.isFormal());

        Tuple template = new Tuple(new PhysicalLocality(),
                new LogicalLocality());
        assertTrue(serverNode.in_nb(template));
        assertEquals(clientLoc, template.getItem(0));
        assertEquals(clientLogLoc, template.getItem(1));
    }

    protected void clientLogin(KlavaNode clientNode, PhysicalLocality serverLoc)
            throws KlavaException {
        assertTrue(clientNode.login(serverLoc));

        Tuple template = new Tuple(new PhysicalLocality());
        serverNode.in(template);
        System.out.println("tuple: " + template);
    }

    protected void clientsLogin(int clientNum, PhysicalLocality serverLoc)
            throws KlavaException, IMCException {
        for (int i = 1; i <= clientNum; ++i) {
            KlavaNode clientNode = new KlavaNode();
            clientLogin(clientNode, serverLoc);
            clientNode.close();
        }
    }

    public void testManyLogins() throws IMCException, InterruptedException,
            KlavaException {
        AcceptCoordinator acceptCoordinator = new AcceptCoordinator(serverLoc);
        /* continuously wait for incoming accept requests */
        acceptCoordinator.setLoop(true);
        serverNode.addNodeCoordinator(acceptCoordinator);

        clientsLogin(5, serverLoc);
    }

    protected void clientSubscribe(KlavaNode clientNode,
            LogicalLocality clientLogLoc, PhysicalLocality serverLoc)
            throws KlavaException {
        assertTrue(clientNode.subscribe(serverLoc, clientLogLoc));

        Tuple template = new Tuple(new PhysicalLocality(),
                new LogicalLocality());
        serverNode.in(template);
        System.out.println("tuple: " + template);
        assertEquals(clientLogLoc, template.getItem(1));
    }

    protected void clientsSubscribe(int clientNum, PhysicalLocality serverLoc)
            throws KlavaException, IMCException {
        for (int i = 1; i <= clientNum; ++i) {
            KlavaNode clientNode = new KlavaNode();
            clientSubscribe(clientNode, new LogicalLocality("client" + i),
                    serverLoc);
            clientNode.close();
        }
    }

    public void testManySubscribes() throws IMCException, InterruptedException,
            KlavaException {
        RegisterCoordinator registerCoordinator = new RegisterCoordinator(serverLoc);
        /* continuously wait for incoming accept requests */
        registerCoordinator.setLoop(true);
        serverNode.addNodeCoordinator(registerCoordinator);

        clientsSubscribe(5, serverLoc);
    }

}
