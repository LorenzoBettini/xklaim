package klava.topology;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

/** Tests that a tentative OUT or EVAL claim serializes other guard claims. */
public class KlavaOrMutexTest extends TestCase {

    private static class GuardProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;

        @Override
        public void executeProcess() {
            // The tests drive the mutex directly rather than starting a node.
        }
    }

    public void testSuccessfulOperationCommitsBeforeRetrievalCanClaim() throws Exception {
        assertContendingClaim(false);
    }

    public void testFailedOperationReleasesSlotForRetrieval() throws Exception {
        assertContendingClaim(true);
    }

    private void assertContendingClaim(boolean fail) throws Exception {
        KlavaOrMutex mutex = new KlavaOrMutex();
        GuardProcess operation = new GuardProcess();
        GuardProcess retrieval = new GuardProcess();
        mutex.addProcess(operation);
        mutex.addProcess(retrieval);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch claiming = new CountDownLatch(1);
        CountDownLatch claimFinished = new CountDownLatch(1);
        AtomicBoolean claimed = new AtomicBoolean();
        Thread operationThread = new Thread(() -> {
            try {
                mutex.executeIfFirst(operation, () -> {
                    entered.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                    if (fail) {
                        throw new IllegalStateException("operation failed");
                    }
                });
            } catch (IllegalStateException e) {
                if (!fail) {
                    throw e;
                }
            }
        });
        Thread retrievalThread = new Thread(() -> {
            claiming.countDown();
            claimed.set(mutex.isFirst());
            claimFinished.countDown();
        });
        try {
            operationThread.start();
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            retrievalThread.start();
            assertTrue(claiming.await(5, TimeUnit.SECONDS));
            assertFalse("retrieval must wait for the operation",
                    claimFinished.await(50, TimeUnit.MILLISECONDS));
            release.countDown();
            operationThread.join(5000);
            retrievalThread.join(5000);
            assertFalse(operationThread.isAlive());
            assertFalse(retrievalThread.isAlive());
            assertEquals("claim should depend on operation success", fail, claimed.get());
        } finally {
            release.countDown();
            operationThread.join(5000);
            retrievalThread.join(5000);
        }
    }
}
