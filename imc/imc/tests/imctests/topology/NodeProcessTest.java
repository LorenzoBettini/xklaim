package imctests.topology;

import java.util.Vector;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventCollector;
import org.mikado.imc.events.ProcessEvent;
import org.mikado.imc.events.AddRemoveEvent.EventType;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.NodeProcess;

import junit.framework.TestCase;

/**
 * Tests for processes running on a node
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class NodeProcessTest extends TestCase {

    /**
     * A process that loops and never returns
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class NonEndingProcess extends NodeProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void execute() throws IMCException {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    
                }                
            }
        }

    }

    /**
     * Continuously sleeps
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class SleepingProcess extends NodeProcess {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void execute() throws IMCException {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                throw new IMCException("while sleeping", e);
            }

        }

    }

    /**
     * Does nothing
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class EmptyProcess extends NodeProcess {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void execute() throws IMCException {

        }

    }

    /**
     * A process that replicates itself
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class ReplicatingProcess extends NodeProcess {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void execute() throws IMCException {
            try {
                addNodeProcess(new ReplicatingProcess());
            } catch (IMCException e) {
                error = e.getMessage();
            }

        }

    }

    String error;

    protected void setUp() throws Exception {
        super.setUp();
        error = "";
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testProcessEvents() throws InterruptedException, IMCException {
        Node node = new Node();
        EmptyProcess emptyProcess = new EmptyProcess();
        EmptyProcess emptyProcess2 = new EmptyProcess();

        /* used to intercept events concerning processes */
        EventCollector eventCollector = new EventCollector();
        node.addListener(ProcessEvent.ProcessEventId, eventCollector);

        node.addNodeProcess(emptyProcess);
        emptyProcess.join();

        /*
         * we must have collected two events, one for addition and one for
         * removal
         */
        eventCollector.waitForEventNumber(2);
        Vector<Event> events = eventCollector.getCollectedEvents();
        assertTrue(((ProcessEvent) events.elementAt(0)).eventType == EventType.ADDED);
        assertEquals(((ProcessEvent) events.elementAt(0)).element.getName(),
                emptyProcess.getName());
        assertTrue(((ProcessEvent) events.elementAt(1)).eventType == EventType.REMOVED);
        assertEquals(((ProcessEvent) events.elementAt(1)).element.getName(),
                emptyProcess.getName());

        node.addNodeProcess(emptyProcess2);
        emptyProcess.join();

        /*
         * we must have collected other two events, one for addition and one for
         * removal
         */
        eventCollector.waitForEventNumber(4);
        events = eventCollector.getCollectedEvents();
        assertTrue(((ProcessEvent) events.elementAt(2)).eventType == EventType.ADDED);
        assertEquals(((ProcessEvent) events.elementAt(2)).element.getName(),
                emptyProcess2.getName());
        assertTrue(((ProcessEvent) events.elementAt(3)).eventType == EventType.REMOVED);
        assertEquals(((ProcessEvent) events.elementAt(3)).element.getName(),
                emptyProcess2.getName());
    }

    public void testNonRespondingProcesses() throws IMCException,
            InterruptedException {
        Node node = new Node();
        NonEndingProcess nonEndingProcess = new NonEndingProcess();
        NonEndingProcess nonEndingProcess2 = new NonEndingProcess();
        SleepingProcess sleepingProcess = new SleepingProcess();
        SleepingProcess sleepingProcess2 = new SleepingProcess();

        node.addNodeProcess(nonEndingProcess);
        node.addNodeProcess(nonEndingProcess2);
        node.addNodeProcess(sleepingProcess);
        node.addNodeProcess(sleepingProcess2);

        /* under windows this seems to be necessary... */
        Thread.sleep(2000);

        try {
            node.close();

            /* must not get here since there are non responding processes */
            fail();
        } catch (IMCException e) {
            e.printStackTrace();
            /*
             * check that only the two non ending processes have not actually
             * terminated
             */
            assertTrue(e.getMessage().indexOf(nonEndingProcess.getName()) > 0);
            assertTrue(e.getMessage().indexOf(nonEndingProcess2.getName()) > 0);
            assertFalse(e.getMessage().indexOf(sleepingProcess.getName()) > 0);
            assertFalse(e.getMessage().indexOf(sleepingProcess2.getName()) > 0);
        }

        try {
            node.addNodeProcess(sleepingProcess);
            /* must not get here since the node is already closed */
            fail();
        } catch (IMCException e) {
            e.printStackTrace();
            assertEquals(e.getMessage(), "sorry we're closed");
        }
    }

    /**
     * This spawns a process that replicates itself, in order to see what
     * happens if a process tries to spawn another process when the node is
     * closed.
     * 
     * @throws IMCException
     */
    public void testReplicatingProcess() throws IMCException {
        Node node = new Node();

        node.addNodeProcess(new ReplicatingProcess());
        node.addNodeProcess(new ReplicatingProcess());
        node.addNodeProcess(new ReplicatingProcess());

        node.close();

        /* some process must have experienced and exception */
        assertTrue(error.length() > 0);

        System.out.println("error received by process: " + error);

        assertEquals("sorry we're closed", error);
    }

}
