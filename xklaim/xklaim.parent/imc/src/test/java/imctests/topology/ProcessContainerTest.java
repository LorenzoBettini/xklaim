/*
 * Created on May 22, 2006
 */
package imctests.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.topology.CollectableThread;
import org.mikado.imc.topology.ProcessContainer;
import org.mikado.imc.topology.ThreadContainer;

import junit.framework.TestCase;

/**
 * Tests for the ProcessContainer class
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ProcessContainerTest extends TestCase {

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class SimpleThread extends Thread {
        int sleepTime = 500;

        public void run() {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class SimpleCollectableThread extends CollectableThread {
        int sleepTime = 500;

        public void execute() {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleThreads() throws InterruptedException, IMCException {
        ProcessContainer<Thread> processContainer = new ProcessContainer<Thread>();

        for (int i = 0; i < 6; ++i) {
            SimpleThread simpleThread = new SimpleThread();
            simpleThread.start();
            processContainer.addElement(simpleThread);
        }

        processContainer.join(0);

        /* then reuse the container */

        for (int i = 0; i < 6; ++i) {
            SimpleThread simpleThread = new SimpleThread();
            simpleThread.start();
            processContainer.addElement(simpleThread);
        }

        processContainer.join(0);
    }

    public void testTimeout() throws InterruptedException, IMCException {
        ProcessContainer<Thread> processContainer = new ProcessContainer<Thread>();

        SimpleThread simpleThread = new SimpleThread();
        simpleThread.sleepTime = 100000; // very long sleep
        simpleThread.start();
        processContainer.addElement(simpleThread);

        /* test timeout */
        processContainer.join(500);

        /* then interrupt the thread manually */
        simpleThread.interrupt();
        simpleThread.join();
    }

    public void testCollactableThreads() throws IMCException,
            InterruptedException {
        ThreadContainer processContainer = new ThreadContainer();

        SimpleCollectableThread simpleThread = new SimpleCollectableThread();

        simpleThread.sleepTime = 100000; // very long sleep
        simpleThread.start();
        processContainer.addElement(simpleThread);

        assertTrue(processContainer.size() > 0);
        
        /* test timeout */
        processContainer.join(500);

        /* then interrupt the thread manually */
        simpleThread.interrupt();
        simpleThread.join();
        
        assertTrue(processContainer.size() == 0);
    }
}
