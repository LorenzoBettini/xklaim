/**
 * created: Jan 3, 2006
 */
package klava.tests.junit;

import klava.Environment;
import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;
import klava.topology.KlavaProcessVar;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.ProtocolException;


/**
 * Tests execution of klava processes within a klava node.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class NodeProcessTest extends ClientServerBase {
    /**
     * Waits for a process at the local tuple space and executes it.
     * 
     * @author Lorenzo Bettini
     */
    public class ReceiveProcess extends KlavaProcess {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            KlavaProcessVar klavaProcessVar = new KlavaProcessVar();
            Tuple template = new Tuple(klavaProcessVar);
            in(template, self);
            System.out.println("received a process");
            eval(klavaProcessVar.klavaProcess, self);
        }

    }

    /**
     * @author Lorenzo Bettini
     * 
     */
    public class SimpleTupleProcess extends SimpleProcess {
        Tuple tuple;

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            setDoAutomaticClosure(true);
            System.out.println(getClass().getName() + ": communicating with "
                    + destination);
            out(tuple, destination);
            result = "done";
        }

    }
    
    public void testSimpleProcessExecution() throws InterruptedException,
            IMCException, KlavaException {
        SimpleProcess simpleProcess = new SimpleProcess();
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(clientNode.in_nb(new Tuple(new KString())));
    }

    public void testProcessToServerExecution() throws InterruptedException,
            IMCException, KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();
        /* make the process communicate with the server */
        simpleProcess.destination = serverLoc;
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(serverNode.in_nb(new Tuple(new KString())));
    }

    public void testProcessLocalityResolution() throws InterruptedException,
            IMCException, KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();
        LogicalLocality destination = new LogicalLocality("destination");

        /*
         * adds a mapping in the client environment for the logical locality
         * destination to the server locality
         */
        clientNode.addToEnvironment(destination, serverLoc);

        /* set the process' destination with the logical locality */
        simpleProcess.destination = destination;

        /*
         * the process should now send the tuple to server, since it uses the
         * node environment
         */
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(serverNode.in_nb(new Tuple(new KString())));
        assertFalse(clientNode.in_nb(new Tuple(new KString())));

        /*
         * now we add a mapping to the process' environment for destination to
         * the client locality
         */
        simpleProcess = new SimpleProcess();
        simpleProcess.destination = destination;
        simpleProcess.addToEnvironment(destination, clientLoc);
        /*
         * this second addition should do nothing since a mapping for
         * destination is already present
         */
        simpleProcess.addToEnvironment(destination, serverLoc);

        /*
         * the process should now send the tuple to client, since it uses its
         * own environment first.
         */
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(clientNode.in_nb(new Tuple(new KString())));
        assertFalse(serverNode.in_nb(new Tuple(new KString())));

    }

    public void testCloseProcess() throws InterruptedException, IMCException,
            KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();
        LogicalLocality destination = new LogicalLocality("destination");

        /* no environment for the first test */
        simpleProcess.makeProcessClosure(serverLoc, null);

        /*
         * the process uses self by default which is now closed with the server
         * destination
         */
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(serverNode.in_nb(new Tuple(new KString())));
        assertFalse(clientNode.in_nb(new Tuple(new KString())));

        simpleProcess = new SimpleProcess();
        simpleProcess.makeProcessClosure(serverLoc, null);
        /* check that a subsequent closure will not update the self */
        simpleProcess.makeProcessClosure(clientLoc, null);

        /*
         * the process uses self by default which is now closed with the server
         * destination
         */
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(serverNode.in_nb(new Tuple(new KString())));
        assertFalse(clientNode.in_nb(new Tuple(new KString())));

        /* now use an environment in the closure */
        simpleProcess = new SimpleProcess();
        Environment environment = new Environment();
        /* where destination is bound to server */
        environment.add(destination, serverLoc);
        simpleProcess.makeProcessClosure(serverLoc, environment);
        /* check that a subsequent closure will not update the environment */
        Environment environment1 = new Environment();
        environment1.add(destination, clientLoc);
        simpleProcess.makeProcessClosure(clientLoc, environment1);

        /* set the destination for the process */
        simpleProcess.destination = destination;

        /*
         * the process uses destination which is now closed with the server
         * destination
         */
        clientNode.eval(simpleProcess);
        simpleProcess.join();
        assertEquals(simpleProcess.result, "done");
        /* check that the process actually inserted the tuple */
        assertTrue(serverNode.in_nb(new Tuple(new KString())));
        assertFalse(clientNode.in_nb(new Tuple(new KString())));

    }

    public void testAutomaticProcessClosure() throws InterruptedException,
            IMCException, KlavaException {
        SimpleTupleProcess simpleTupleProcess = new SimpleTupleProcess();
        Tuple tuple = new Tuple(new LogicalLocality("self"));
        Tuple template = new Tuple(new PhysicalLocality());

        /*
         * the first execution should fail since self cannot be resolved
         */
        simpleTupleProcess.tuple = tuple;
        clientNode.eval(simpleTupleProcess);

        simpleTupleProcess.join();

        assertEquals(simpleTupleProcess.result, "");

        /* this must succeed since we use mainPhysicalLocality */
        simpleTupleProcess = new SimpleTupleProcess();

        simpleTupleProcess.tuple = tuple;
        serverNode.setMainPhysicalLocality(serverLoc);
        serverNode.eval(simpleTupleProcess);

        simpleTupleProcess.join();

        /* the inserted tuple must contain now a PhysicalLocality */
        assertEquals(simpleTupleProcess.result, "done");
        assertTrue(serverNode.in_nb(template));
        assertEquals(template.getItem(0), serverLoc);

        /* reset it */
        serverNode.setMainPhysicalLocality(null);

        /* this must succeed since there are connections */
        clientLoginsToServer();

        simpleTupleProcess = new SimpleTupleProcess();
        tuple = new Tuple(new LogicalLocality("self"));
        simpleTupleProcess.tuple = tuple;

        clientNode.eval(simpleTupleProcess);

        simpleTupleProcess.join();

        assertEquals(simpleTupleProcess.result, "done");

        /* the inserted tuple must contain now a PhysicalLocality */
        assertEquals(simpleTupleProcess.result, "done");
        template = new Tuple(new PhysicalLocality());
        assertTrue(clientNode.in_nb(template));
        assertEquals(template.getItem(0), clientLoc);

        simpleTupleProcess = new SimpleTupleProcess();
        tuple = new Tuple(new LogicalLocality("self"));
        simpleTupleProcess.tuple = tuple;

        serverNode.eval(simpleTupleProcess);

        simpleTupleProcess.join();

        assertEquals(simpleTupleProcess.result, "done");

        /* the inserted tuple must contain now a PhysicalLocality */
        assertEquals(simpleTupleProcess.result, "done");
        template = new Tuple(new PhysicalLocality());
        assertTrue(serverNode.in_nb(template));
        assertEquals(template.getItem(0), serverLoc);
    }

    public void testAutomaticProcessClosurePartial()
            throws InterruptedException, IMCException, KlavaException {
        SimpleTupleProcess simpleTupleProcess = new SimpleTupleProcess();

        /*
         * the first logical locality will be resolved by the process itself,
         * while the second one will be resolved through the node
         */
        Tuple tuple = new Tuple(new LogicalLocality("test1"),
                new LogicalLocality("test2"));
        Tuple template = new Tuple(new PhysicalLocality(),
                new PhysicalLocality());

        PhysicalLocality firstLoc = new PhysicalLocality("localhost", 50000);
        PhysicalLocality otherLoc = new PhysicalLocality("localhost", 50001);

        simpleTupleProcess.addToEnvironment(new LogicalLocality("test1"),
                firstLoc);
        clientNode.addToEnvironment(new LogicalLocality("test2"), otherLoc);

        simpleTupleProcess.tuple = tuple;
        clientNode.eval(simpleTupleProcess);

        simpleTupleProcess.join();

        /* the inserted tuple must contain now a PhysicalLocality */
        assertEquals(simpleTupleProcess.result, "done");
        assertTrue(clientNode.in_nb(template));
        assertEquals(template.getItem(0), firstLoc);
        assertEquals(template.getItem(1), otherLoc);

    }

    public void testFailEval() throws ProtocolException, InterruptedException {
        clientLoginsToServer();
        /* we don't want remote processes */
        serverNode.refuseRemoteProcesses(true);
        SimpleProcess simpleProcess = new SimpleProcess();

        /* try to spawn a process to a node that does not accept processes */
        try {
            clientNode.eval(simpleProcess, serverLoc);

            /* must not get here */
            fail();
        } catch (KlavaException e) {
            e.printStackTrace();
            assertEquals(serverLoc + ": we don't accept remote processes", e
                    .getMessage());
        }
    }

    public void testSimpleEval() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();

        /* make sure there was no previous tuple */
        assertFalse(serverNode.in_nb(new Tuple(new KString())));

        /* try to spawn a process to a node that does not accept processes */
        clientNode.eval(simpleProcess, serverLoc);

        /*
         * check that the process has actually inserted the tuple at the remote
         * server (the processes use self by default as the destination)
         */
        serverNode.in(new Tuple(new KString()));
    }

    public void testClosedEval() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();

        /*
         * close the process so that it will refer to the client via self
         */
        simpleProcess.makeProcessClosure(clientLoc, null);

        /* make sure there was no previous tuple */
        assertFalse(clientNode.in_nb(new Tuple(new KString())));

        /* try to spawn a process to a node that does not accept processes */
        clientNode.eval(simpleProcess, serverLoc);

        /*
         * check that the process has actually inserted the tuple at the client
         * (the processes use self by default as the destination which is now
         * bound to the clien
         */
        clientNode.in(new Tuple(new KString()));
    }

    public void testOutProcess() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();
        SimpleProcess simpleProcess = new SimpleProcess();

        /* make sure there was no previous tuple */
        assertFalse(serverNode.in_nb(new Tuple(new KString())));

        ReceiveProcess receiveProcess = new ReceiveProcess();
        serverNode.eval(receiveProcess);

        /* try to spawn a process to a node that does not accept processes */
        clientNode.out(new Tuple(simpleProcess), serverLoc);

        /*
         * check that the process has actually inserted the tuple at the remote
         * server (the processes use self by default as the destination)
         */
        serverNode.in(new Tuple(new KString()));

        receiveProcess.join();
    }

    public void testProcessToServerTransLoc() throws InterruptedException,
            IMCException, KlavaException {
        clientLoginsToServer();
        SimpleProcessWithLocTranslation simpleProcess = new SimpleProcessWithLocTranslation();
        /* make the process communicate with the server */
        simpleProcess.destination = serverLoc;
        clientNode.eval(simpleProcess, serverLoc);
        /* check that the process actually inserted the tuples */
        serverNode.in(new Tuple(new KString()));
        /*
         * the process translates self when executing at the server, so we must
         * look for the server locality
         */
        serverNode.in(new Tuple(serverLoc));
    }

    public void testClosedProcessToServerTransLoc()
            throws InterruptedException, IMCException, KlavaException {
        clientLoginsToServer();
        SimpleProcessWithLocTranslation simpleProcess = new SimpleProcessWithLocTranslation();
        /* make the process communicate with the server */
        simpleProcess.destination = serverLoc;
        simpleProcess.makeProcessClosure(clientLoc, null);
        clientNode.eval(simpleProcess, serverLoc);

        /* check that the process actually inserted the tuples */
        serverNode.in(new Tuple(new KString()));
        /*
         * the process translates self when executing at the server, but it is
         * already closed, so we must look for the client locality
         */
        serverNode.in(new Tuple(clientLoc));
    }
    
    public void testTranslationWithoutSelf() {
        
    }

}
