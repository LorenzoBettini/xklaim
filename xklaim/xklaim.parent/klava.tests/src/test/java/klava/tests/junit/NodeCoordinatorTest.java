/*
 * Created on Jan 23, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.AcceptNodeCoordinator;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;
import klava.topology.KlavaProcessVar;
import klava.topology.RegisterNodeCoordinator;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.ProtocolException;


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
        private static final long serialVersionUID = 1L;

        PhysicalLocality remote = new PhysicalLocality();

        LogicalLocality remoteLogical = new LogicalLocality();

        {
            /*
             * These tests store the subscribed logical locality as tuple data,
             * not its resolved physical locality.
             */
            setDoAutomaticClosure(false);
        }

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

    public class TupleClosureCoordinator extends KlavaNodeCoordinator {
        private static final long serialVersionUID = 1L;

        Tuple tuple;

        Locality destination = self;

        public TupleClosureCoordinator(Tuple tuple, Locality destination) {
            this.tuple = tuple;
            this.destination = destination;
        }

        @Override
        public void executeProcess() throws KlavaException {
            out(tuple, destination);
        }
    }

    public class ReceiveProcess extends KlavaProcess {
        private static final long serialVersionUID = 1L;

        @Override
        public void executeProcess() throws KlavaException {
            KlavaProcessVar klavaProcessVar = new KlavaProcessVar();
            Tuple template = new Tuple(klavaProcessVar);
            in(template, self);
            eval(klavaProcessVar.klavaProcess, self);
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

    public void testCoordinatorOutMakesAutomaticClosure()
            throws IMCException, InterruptedException, KlavaException {
        serverNode.setMainPhysicalLocality(serverLoc);
        LogicalLocality peer = new LogicalLocality("peer");
        PhysicalLocality peerLoc = new PhysicalLocality("127.0.0.1", 21001);
        serverNode.addToEnvironment(peer, peerLoc);
        Tuple tuple = new Tuple(new LogicalLocality("self"), peer);
        TupleClosureCoordinator coordinator =
                new TupleClosureCoordinator(tuple, self);

        serverNode.addNodeCoordinator(coordinator);
        coordinator.join();

        Tuple template = new Tuple(new PhysicalLocality(),
                new PhysicalLocality());
        assertTrue(serverNode.in_nb(template));
        assertEquals(serverLoc, template.getItem(0));
        assertEquals(peerLoc, template.getItem(1));
    }

    public void testCoordinatorOutMakesClosureOfProcessInTuple()
            throws ProtocolException, InterruptedException, KlavaException,
            IMCException {
        clientLoginsToServer();
        ReceiveProcess receiveProcess = new ReceiveProcess();
        serverNode.eval(receiveProcess);
        TupleClosureCoordinator coordinator =
                new TupleClosureCoordinator(new Tuple(new SimpleProcess()),
                        serverLoc);

        clientNode.addNodeCoordinator(coordinator);
        coordinator.join();

        assertTrue(clientNode.in_t(new Tuple(new KString()), 5000));
        assertFalse(serverNode.in_nb(new Tuple(new KString())));
        receiveProcess.join();
    }

}
