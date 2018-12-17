/**
 * created: Jan 4, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClosureMaker;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;

import org.mikado.imc.protocols.ProtocolException;


/**
 * Tests for closures
 * 
 * @author Lorenzo Bettini
 * 
 */
public class ClosureMakerTest extends ClientServerBase {

    /**
     * @author Lorenzo Bettini
     * 
     */
    public class SimpleProcess extends KlavaProcess {
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {

        }

        Locality getSelf() {
            return self;
        }
    }

    /**
     * Test closure involving only local resources
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     * @throws KlavaException
     */
    public void testClosures() throws ProtocolException, InterruptedException,
            KlavaException {
        ClosureMaker closureMaker = clientNode.getClosureMaker();

        PhysicalLocality forSelf = serverLoc;

        SimpleProcess simpleProcess = new SimpleProcess();
        SimpleProcess simpleProcess1 = new SimpleProcess();
        KString string = new KString("foo");
        LogicalLocality logicalLocality = new LogicalLocality(clientLogLoc);
        LogicalLocality logicalLocality1 = new LogicalLocality(serverLogLoc);
        Tuple internal = new Tuple(simpleProcess1, logicalLocality1,
                KlavaNode.self);
        Tuple tuple = new Tuple(string, internal, simpleProcess,
                logicalLocality);

        System.out.println("original tuple: " + tuple);

        try {
            closureMaker.makeClosure(tuple, forSelf);
            /* should not get here since clientLogLoc cannot be resolved */
            fail();
        } catch (KlavaLogicalLocalityException e) {
            assertEquals(clientLogLoc.toString(), e.getMessage());
        }

        clientLoc = new PhysicalLocality("localhost", 10000);
        clientNode.addToEnvironment(clientLogLoc, clientLoc);

        /*
         * this must succeed since clientLogLoc is now mapped into the clientLoc
         * physical locality in the client (and serverLogLoc is already in the
         * client environment).
         */
        closureMaker.makeClosure(tuple, forSelf);

        System.out.println("closed tuple: " + tuple);

        checkTupleFields(simpleProcess, simpleProcess1, string,
                logicalLocality, logicalLocality1, internal, tuple);
    }

    /**
     * Test closure involving only local resources, no exception is thrown if
     * there's a problem during the closure.
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     * @throws KlavaException
     */
    public void testNoStopOnException() throws ProtocolException,
            InterruptedException, KlavaException {
        ClosureMaker closureMaker = clientNode.getClosureMaker();
        closureMaker.setStopOnException(false);

        PhysicalLocality forSelf = serverLoc;

        SimpleProcess simpleProcess = new SimpleProcess();
        SimpleProcess simpleProcess1 = new SimpleProcess();
        KString string = new KString("foo");
        LogicalLocality logicalLocality = new LogicalLocality(clientLogLoc);
        LogicalLocality logicalLocality1 = new LogicalLocality(serverLogLoc);
        Tuple internal = new Tuple(simpleProcess1, logicalLocality1,
                KlavaNode.self);
        Tuple tuple = new Tuple(string, internal, simpleProcess,
                logicalLocality);

        System.out.println("original tuple: " + tuple);

        /* should fail since clientLogLoc cannot be resolved */
        assertFalse(closureMaker.makeClosure(tuple, forSelf));

        System.out.println("closed tuple: " + tuple);

        /* check the tuple fields */
        assertTrue(tuple.getItem(0) == string);
        assertTrue(tuple.getItem(1) == internal);
        assertTrue(tuple.getItem(2) == simpleProcess);

        /* this should still be not closed */
        assertTrue(tuple.getItem(3) == logicalLocality);

        assertEquals(simpleProcess.getSelf(), serverLoc);

        /*
         * notice that since closure keeps on even in case of a failure, the
         * server logical locality is closed (the client has the mapping for
         * serverLogLoc)
         */
        assertTrue(internal.getItem(0) == simpleProcess1);
        assertTrue(internal.getItem(1) != logicalLocality1);
        assertTrue(internal.getItem(2) != KlavaNode.self);

        assertEquals(simpleProcess1.getSelf(), serverLoc);
        assertEquals(internal.getItem(1), serverLoc);
        assertEquals(internal.getItem(2), serverLoc);
    }

    /**
     * @param simpleProcess
     * @param simpleProcess1
     * @param string
     * @param logicalLocality
     * @param logicalLocality1
     * @param internal
     * @param tuple
     */
    private void checkTupleFields(SimpleProcess simpleProcess,
            SimpleProcess simpleProcess1, KString string,
            LogicalLocality logicalLocality, LogicalLocality logicalLocality1,
            Tuple internal, Tuple tuple) {
        /* check the tuple fields */
        assertTrue(tuple.getItem(0) == string);
        assertTrue(tuple.getItem(1) == internal);
        assertTrue(tuple.getItem(2) == simpleProcess);
        assertTrue(tuple.getItem(3) != logicalLocality);

        assertEquals(tuple.getItem(3), clientLoc);
        assertEquals(simpleProcess.getSelf(), serverLoc);

        assertTrue(internal.getItem(0) == simpleProcess1);
        assertTrue(internal.getItem(1) != logicalLocality1);
        assertTrue(internal.getItem(2) != KlavaNode.self);

        assertEquals(simpleProcess1.getSelf(), serverLoc);
        assertEquals(internal.getItem(1), serverLoc);
        assertEquals(internal.getItem(2), serverLoc);
    }

    /**
     * Test closures involving also remote queries
     * 
     * @throws ProtocolException
     * @throws InterruptedException
     * @throws KlavaException
     */
    public void testRemoteClosures() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();
        ClosureMaker closureMaker = clientNode.getClosureMaker();

        PhysicalLocality forSelf = serverLoc;

        SimpleProcess simpleProcess = new SimpleProcess();
        SimpleProcess simpleProcess1 = new SimpleProcess();
        KString string = new KString("foo");
        LogicalLocality logicalLocality = new LogicalLocality(clientLogLoc);
        LogicalLocality logicalLocality1 = new LogicalLocality(serverLogLoc);
        Tuple internal = new Tuple(simpleProcess1, logicalLocality1,
                KlavaNode.self);
        Tuple tuple = new Tuple(string, internal, simpleProcess,
                logicalLocality);

        System.out.println("original tuple: " + tuple);

        try {
            closureMaker.makeClosure(tuple, forSelf);
            /* should not get here since clientLogLoc cannot be resolved */
            fail();
        } catch (KlavaLogicalLocalityException e) {
            assertEquals(clientLogLoc.toString(), e.getMessage());
        }

        serverNode.addToEnvironment(clientLogLoc, clientLoc);

        /*
         * this must succeed since clientLogLoc is now mapped into the clientLoc
         * physical locality in the server, and the client forwards its request
         * to the server (and serverLogLoc is already in the client
         * environment).
         */
        closureMaker.makeClosure(tuple, forSelf);

        System.out.println("closed tuple: " + tuple);

        checkTupleFields(simpleProcess, simpleProcess1, string,
                logicalLocality, logicalLocality1, internal, tuple);
    }
}
