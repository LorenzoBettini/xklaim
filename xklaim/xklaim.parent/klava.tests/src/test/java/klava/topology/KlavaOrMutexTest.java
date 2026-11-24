package klava.topology;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

/**
 * Tests the mutex independently of a node. The simulated Klaim branches are:
 * <pre>{@code
 * or(
 *     proc { out(...)@destination }, // or eval(child)@destination
 *     proc { in(template)@self }
 * )
 * }</pre>
 * The first operation pauses before reporting success or failure. The retrieval
 * has already found its tuple and is trying to claim the winner slot.
 */
public class KlavaOrMutexTest extends TestCase {

    private static class GuardProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;

        @Override
        public void executeProcess() {
            // The tests drive the mutex directly rather than starting a node.
        }
    }

    /** OUT/EVAL returns normally, so the waiting retrieval loses its claim. */
    public void testSuccessfulOperationCommitsBeforeRetrievalCanClaim() throws Exception {
        assertContendingClaim(false);
    }

    /** OUT/EVAL throws, so the waiting retrieval can claim the free slot. */
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
                // executeIfFirst holds the mutex while the operation is paused.
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
            // This represents a retrieval that succeeded and now calls isFirst.
            claiming.countDown();
            claimed.set(mutex.isFirst());
            claimFinished.countDown();
        });
        try {
            operationThread.start();
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            retrievalThread.start();
            assertTrue(claiming.await(5, TimeUnit.SECONDS));
            // The retrieval cannot decide while the other operation is pending.
            assertFalse("retrieval must wait for the operation",
                    claimFinished.await(50, TimeUnit.MILLISECONDS));
            release.countDown();
            operationThread.join(5000);
            retrievalThread.join(5000);
            assertFalse(operationThread.isAlive());
            assertFalse(retrievalThread.isAlive());
            // Success commits the first branch; failure leaves the slot free.
            assertEquals("claim should depend on operation success", fail, claimed.get());
        } finally {
            release.countDown();
            operationThread.join(5000);
            retrievalThread.join(5000);
        }
    }
}
