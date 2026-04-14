package klava.tests.junit;

import java.util.List;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.Tuple;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaOrProcess;

/**
 * Tests for the OR operator ({@link KlavaNodeCoordinator#or(List)}) executed
 * through a {@link KlavaNodeCoordinator}.
 *
 * <p>Each test creates a {@link KlavaNode}, pre-populates its tuple space with
 * one or more tuples, then runs an {@link OrCoordinator} (a
 * {@link KlavaNodeCoordinator} subclass) that calls
 * {@link KlavaNodeCoordinator#or(List)} with a set of {@link KlavaOrProcess}es.
 * After the coordinator terminates (detected via {@link Thread#join()}), the
 * test inspects the recorded state of each process.</p>
 *
 * <p>The mutual exclusion assumption for guard operations is honoured in all
 * tests: only the tuple required by the winning process is inserted into the
 * tuple space before the OR is started.</p>
 */
public class KlavaNodeCoordinatorOrTest extends TestCase {

    private KlavaNode node;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        node = new KlavaNode();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        node.close();
    }

    // -----------------------------------------------------------------------
    // Helper process classes
    // -----------------------------------------------------------------------

    /**
     * An OR process whose guard is {@code in(expectedValue)}. Records whether
     * the body (the code after the guard) was executed.
     */
    private static class InGuardProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final String expectedValue;
        private boolean bodyExecuted = false;

        InGuardProcess(String expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public void executeProcess() throws KlavaException {
            in(new Tuple(new KString(expectedValue)), self);
            /* everything below is the "body" — executed only if the guard
             * succeeded and this process won the OR competition */
            bodyExecuted = true;
        }

        boolean isBodyExecuted() {
            return bodyExecuted;
        }
    }

    /**
     * An OR process whose guard is {@code read(expectedValue)}. Records whether
     * the body was executed.
     */
    private static class ReadGuardProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final String expectedValue;
        private boolean bodyExecuted = false;

        ReadGuardProcess(String expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public void executeProcess() throws KlavaException {
            read(new Tuple(new KString(expectedValue)), self);
            bodyExecuted = true;
        }

        boolean isBodyExecuted() {
            return bodyExecuted;
        }
    }

    /**
     * An OR process with two sequential retrieval operations. The first is the
     * guard (subject to OR synchronization); the second is a normal (non-guard)
     * retrieval that should proceed without any OR synchronization.
     */
    private static class TwoRetrievalsProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final String guardValue;
        private final String secondValue;
        private boolean guardExecuted = false;
        private boolean secondExecuted = false;

        TwoRetrievalsProcess(String guardValue, String secondValue) {
            this.guardValue = guardValue;
            this.secondValue = secondValue;
        }

        @Override
        public void executeProcess() throws KlavaException {
            in(new Tuple(new KString(guardValue)), self);   // guard (first retrieval)
            guardExecuted = true;
            in(new Tuple(new KString(secondValue)), self);  // body (non-guard retrieval)
            secondExecuted = true;
        }

        boolean isGuardExecuted() {
            return guardExecuted;
        }

        boolean isSecondExecuted() {
            return secondExecuted;
        }
    }

    /**
     * A coordinator that calls {@link KlavaNodeCoordinator#or(List)} over the
     * provided OR processes and then terminates.
     *
     * <p>Any {@link KlavaException} thrown by {@code or()} is captured in
     * {@link #caughtKlavaException} so that tests can inspect it after the
     * coordinator thread has finished.</p>
     */
    private static class OrCoordinator extends KlavaNodeCoordinator {

        private static final long serialVersionUID = 1L;

        private final List<KlavaOrProcess> orProcesses;

        volatile KlavaException caughtKlavaException;

        OrCoordinator(List<KlavaOrProcess> orProcesses) {
            this.orProcesses = orProcesses;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                or(orProcesses);
            } catch (KlavaException e) {
                caughtKlavaException = e;
                throw e;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Tests that when the first branch's IN guard succeeds, only that branch
     * runs its body and the second branch is interrupted.
     *
     * <p>Pre-condition: tuple "A" is in the node; "B" is not.</p>
     */
    public void testOrWithInGuard_firstBranchWins() throws Exception {
        node.out(new Tuple(new KString("A")));

        InGuardProcess processA = new InGuardProcess("A");
        InGuardProcess processB = new InGuardProcess("B");

        OrCoordinator coordinator = new OrCoordinator(List.of(processA, processB));
        node.addNodeCoordinator(coordinator);
        coordinator.join();

        assertTrue("Process A body should have executed", processA.isBodyExecuted());
        assertFalse("Process B body should NOT have executed", processB.isBodyExecuted());
        /* "A" was consumed by the IN operation */
        assertFalse("Tuple 'A' should have been consumed",
                node.in_nb(new Tuple(new KString("A"))));
    }

    /**
     * Tests that when the second branch's IN guard succeeds, only that branch
     * runs its body and the first branch is interrupted.
     *
     * <p>Pre-condition: tuple "B" is in the node; "A" is not.</p>
     */
    public void testOrWithInGuard_secondBranchWins() throws Exception {
        node.out(new Tuple(new KString("B")));

        InGuardProcess processA = new InGuardProcess("A");
        InGuardProcess processB = new InGuardProcess("B");

        OrCoordinator coordinator = new OrCoordinator(List.of(processA, processB));
        node.addNodeCoordinator(coordinator);
        coordinator.join();

        assertFalse("Process A body should NOT have executed", processA.isBodyExecuted());
        assertTrue("Process B body should have executed", processB.isBodyExecuted());
        /* "B" was consumed by the IN operation */
        assertFalse("Tuple 'B' should have been consumed",
                node.in_nb(new Tuple(new KString("B"))));
    }

    /**
     * Tests that the OR operator works correctly with READ guards. Because READ
     * does not consume the tuple, it must still be present in the tuple space
     * after the OR completes.
     *
     * <p>Pre-condition: tuple "A" is in the node; "B" is not.</p>
     */
    public void testOrWithReadGuard() throws Exception {
        node.out(new Tuple(new KString("A")));

        ReadGuardProcess processA = new ReadGuardProcess("A");
        ReadGuardProcess processB = new ReadGuardProcess("B");

        OrCoordinator coordinator = new OrCoordinator(List.of(processA, processB));
        node.addNodeCoordinator(coordinator);
        coordinator.join();

        assertTrue("Process A body should have executed", processA.isBodyExecuted());
        assertFalse("Process B body should NOT have executed", processB.isBodyExecuted());
        /* READ does not consume — "A" must still be present */
        assertTrue("Tuple 'A' should still be present after READ",
                node.in_nb(new Tuple(new KString("A"))));
    }

    /**
     * Tests that the custom OR synchronization is applied <em>only</em> to the
     * first retrieval operation (the guard). Subsequent retrieval operations in
     * the winning process proceed as normal blocking operations without any OR
     * synchronization.
     *
     * <p>Process A: guard = {@code in("A")}, second op = {@code in("C")}.
     * Process B: guard = {@code in("B")}.</p>
     *
     * <p>Pre-conditions: tuples "A" and "C" are in the node; "B" is not.</p>
     */
    public void testOrFirstOperationIsGuardOnly() throws Exception {
        node.out(new Tuple(new KString("A")));
        node.out(new Tuple(new KString("C")));

        TwoRetrievalsProcess processA = new TwoRetrievalsProcess("A", "C");
        InGuardProcess processB = new InGuardProcess("B");

        OrCoordinator coordinator = new OrCoordinator(List.of(processA, processB));
        node.addNodeCoordinator(coordinator);
        coordinator.join();

        /* A's guard and second operation both ran */
        assertTrue("Process A guard should have executed", processA.isGuardExecuted());
        assertTrue("Process A second retrieval should have executed",
                processA.isSecondExecuted());
        /* B was interrupted after A's guard succeeded */
        assertFalse("Process B body should NOT have executed", processB.isBodyExecuted());
        /* both "A" and "C" were consumed by A's two IN operations */
        assertFalse("Tuple 'A' should have been consumed",
                node.in_nb(new Tuple(new KString("A"))));
        assertFalse("Tuple 'C' should have been consumed",
                node.in_nb(new Tuple(new KString("C"))));
    }

    /**
     * Tests the OR operator with three competing processes, where the middle
     * branch wins. Both other branches must be interrupted.
     *
     * <p>Pre-condition: only tuple "B" is in the node.</p>
     */
    public void testOrWithThreeProcesses() throws Exception {
        node.out(new Tuple(new KString("B")));

        InGuardProcess processA = new InGuardProcess("A");
        InGuardProcess processB = new InGuardProcess("B");
        InGuardProcess processC = new InGuardProcess("C");

        OrCoordinator coordinator =
                new OrCoordinator(List.of(processA, processB, processC));
        node.addNodeCoordinator(coordinator);
        coordinator.join();

        assertFalse("Process A body should NOT have executed", processA.isBodyExecuted());
        assertTrue("Process B body should have executed", processB.isBodyExecuted());
        assertFalse("Process C body should NOT have executed", processC.isBodyExecuted());
    }

    /**
     * Tests that when the coordinator calling {@link KlavaNodeCoordinator#or(List)}
     * is interrupted while joining the OR processes, it terminates with a
     * {@link KlavaException}.
     *
     * <p>The single OR process blocks indefinitely on a guard retrieval whose
     * tuple is never inserted. An external thread interrupts the coordinator
     * after a short delay; at that point the coordinator is inside
     * {@link Thread#join()} on the OR process. {@code join()} throws
     * {@link InterruptedException}, which {@code or()} wraps in a
     * {@link KlavaException} and re-throws. That exception propagates through
     * {@link KlavaNodeCoordinator#execute() execute()} as an
     * {@link org.mikado.imc.common.IMCException} and is stored as
     * {@code finalException}.</p>
     *
     * <p>The OR process is still alive after the coordinator terminates; it is
     * explicitly interrupted in the test to avoid dangling threads.</p>
     */
    public void testOrCallerInterruptedTerminatesWithKlavaException() throws Exception {
        /* no tuple inserted — the OR process blocks forever on its guard */
        InGuardProcess process = new InGuardProcess("A");

        OrCoordinator coordinator = new OrCoordinator(List.of(process));
        node.addNodeCoordinator(coordinator);

        /* interrupt the coordinator after a short delay to let it reach join() */
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            coordinator.interrupt();
        });
        interrupter.start();
        interrupter.join();

        coordinator.join(5000);
        assertFalse("Coordinator should have terminated", coordinator.isAlive());

        /* or() wraps the InterruptedException in KlavaException and re-throws it;
         * OrCoordinator.executeProcess() captures it in caughtKlavaException */
        assertNotNull("Coordinator should have caught a KlavaException",
                coordinator.caughtKlavaException);
        assertTrue("The KlavaException cause should be an InterruptedException",
                coordinator.caughtKlavaException.getCause() instanceof InterruptedException);

        /* clean up: the OR process is still blocking on in("A"); interrupt it
         * so the test leaves no dangling threads */
        process.interrupt();
        process.join(5000);
    }
}
