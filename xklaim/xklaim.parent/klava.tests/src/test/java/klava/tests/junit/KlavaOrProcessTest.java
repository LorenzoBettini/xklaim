package klava.tests.junit;

import java.util.List;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.Tuple;
import klava.topology.KlavaNode;
import klava.topology.KlavaOrProcess;
import klava.topology.KlavaProcess;

/**
 * Tests for the OR operator — {@link KlavaProcess#or(List)} and
 * {@link KlavaOrProcess}.
 *
 * <p>Each test creates a {@link KlavaNode}, pre-populates its tuple space with
 * one or more tuples, then runs an orchestrator process that calls
 * {@link KlavaProcess#or(List)} with a set of {@link KlavaOrProcess}es. After
 * the orchestrator terminates (detected via {@link Thread#join()}), the test
 * inspects the recorded state of each process.</p>
 *
 * <p>The mutual exclusion assumption for guard operations is honoured in all
 * tests: only the tuple required by the winning process is inserted into the
 * tuple space before the OR is started.</p>
 */
public class KlavaOrProcessTest extends TestCase {

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
     * An OR process whose guard is {@code read(guardValue)} and whose body
     * performs a blocking {@code in(bodyValue)}.
     *
     * <p>Used to verify that the mutual exclusion enforced by
     * {@link klava.topology.KlavaOrMutex} holds even when the guard is a
     * non-consuming {@code read}: after both guards complete without blocking,
     * the winner calls {@code interruptOthers()}, setting the loser's thread
     * interrupt flag. When the loser enters the body's {@code in(bodyValue)},
     * the first {@code wait()} inside the operation throws
     * {@link InterruptedException} immediately (Java threading specification:
     * if the interrupt flag is already set, {@code wait()} throws at once),
     * propagated as a {@link klava.KlavaException}.</p>
     */
    private static class ReadThenInProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final String guardValue;
        private final String bodyValue;
        private boolean bodyCompleted = false;

        ReadThenInProcess(String guardValue, String bodyValue) {
            this.guardValue = guardValue;
            this.bodyValue = bodyValue;
        }

        @Override
        public void executeProcess() throws KlavaException {
            /* guard — first retrieval operation, subject to OR synchronization */
            read(new Tuple(new KString(guardValue)), self);
            /* body — second retrieval, not subject to OR synchronization
             * (isFirstOperation is now false). If this process is the loser,
             * its thread's interrupt flag is already set; wait() throws
             * InterruptedException immediately, propagated as KlavaException. */
            in(new Tuple(new KString(bodyValue)), self);
            bodyCompleted = true;
        }

        boolean isBodyCompleted() {
            return bodyCompleted;
        }
    }

    /**
     * An orchestrator process that runs {@link KlavaProcess#or(List)} over the
     * provided OR processes and then terminates.
     */
    private static class OrOrchestrator extends KlavaProcess {

        private static final long serialVersionUID = 1L;

        private final List<KlavaOrProcess> orProcesses;

        OrOrchestrator(List<KlavaOrProcess> orProcesses) {
            this.orProcesses = orProcesses;
        }

        @Override
        public void executeProcess() throws KlavaException {
            or(orProcesses);
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

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(processA, processB));
        node.eval(orchestrator);
        orchestrator.join();

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

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(processA, processB));
        node.eval(orchestrator);
        orchestrator.join();

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

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(processA, processB));
        node.eval(orchestrator);
        orchestrator.join();

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

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(processA, processB));
        node.eval(orchestrator);
        orchestrator.join();

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

        OrOrchestrator orchestrator =
                new OrOrchestrator(List.of(processA, processB, processC));
        node.eval(orchestrator);
        orchestrator.join();

        assertFalse("Process A body should NOT have executed", processA.isBodyExecuted());
        assertTrue("Process B body should have executed", processB.isBodyExecuted());
        assertFalse("Process C body should NOT have executed", processC.isBodyExecuted());
    }

    /**
     * Tests that the {@link klava.topology.KlavaOrMutex} enforces mutual
     * exclusion even when both guards complete without blocking.
     *
     * <p>Both OR processes perform {@code read("A")} as their guard. Because
     * {@code read} does not consume the tuple, both guards match the present
     * tuple immediately without entering {@code wait()}. After each guard the
     * overridden {@link KlavaOrProcess#read} calls {@code handleOrRetrieval()}:
     * the synchronized {@link klava.topology.KlavaOrMutex#isFirst()} gives
     * {@code true} to exactly one process (the winner), which then calls
     * {@code interruptOthers()}, setting the loser's thread interrupt flag.</p>
     *
     * <p>Both processes then attempt {@code in("TOKEN")} in their bodies
     * (TOKEN is initially absent). The loser's interrupt flag is already set,
     * so the very first {@code wait()} inside that operation throws
     * {@link InterruptedException} immediately (Java threading specification),
     * which propagates as a {@link klava.KlavaException} and is recorded as a
     * non-null {@code finalException} on the loser. TOKEN is inserted after a
     * short delay so the winner can complete its body.</p>
     *
     * <p>The test verifies:</p>
     * <ul>
     *   <li>Exactly one process completed its body (the winner).</li>
     *   <li>Exactly one process has a non-null {@code finalException} (the
     *       loser, interrupted on the blocking body operation).</li>
     *   <li>Tuple {@code "A"} is still present ({@code read} does not
     *       consume).</li>
     * </ul>
     */
    public void testOrReadSameTupleMutexEnforcesExclusiveExecution() throws Exception {
        node.out(new Tuple(new KString("A")));

        ReadThenInProcess processA = new ReadThenInProcess("A", "TOKEN");
        ReadThenInProcess processB = new ReadThenInProcess("A", "TOKEN");

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(processA, processB));
        node.eval(orchestrator);

        /* Wait long enough for both guards to complete (they are instantaneous
         * since read does not block when the tuple is present) and for both
         * processes to enter wait() on TOKEN. By then the loser has already
         * been interrupted via interruptOthers(), so its wait() will throw. */
        Thread.sleep(200);

        /* Unblock the winner; the loser has already terminated with an exception */
        node.out(new Tuple(new KString("TOKEN")));

        orchestrator.join(5000);
        assertFalse("Orchestrator should have completed", orchestrator.isAlive());

        /* Exactly one process should have completed its body */
        assertTrue("Exactly one process should have completed its body",
                processA.isBodyCompleted() != processB.isBodyCompleted());

        /* The loser should have a non-null finalException: the interrupted
         * wait() inside in() propagates as KlavaException, which execute()
         * wraps as IMCException and stores as finalException */
        assertTrue("Exactly one process should have been interrupted on its body operation",
                (processA.getFinalException() != null) != (processB.getFinalException() != null));

        /* read does not consume — 'A' must still be in the tuple space */
        assertTrue("Tuple 'A' should still be present after read guard",
                node.in_nb(new Tuple(new KString("A"))));
    }

    /**
     * Tests that when the process calling {@link KlavaProcess#or(List)} is
     * interrupted while joining the OR processes, it terminates with a
     * {@link klava.KlavaException}.
     *
     * <p>The single OR process blocks indefinitely on a guard retrieval whose
     * tuple is never inserted. An external thread interrupts the orchestrator
     * after a short delay; at that point the orchestrator is inside
     * {@link Thread#join()} on the OR process. {@code join()} throws
     * {@link InterruptedException}, which {@code or()} wraps in a
     * {@link klava.KlavaException} and re-throws. That exception propagates
     * through {@link klava.topology.KlavaProcess#execute() execute()} as an
     * {@link org.mikado.imc.common.IMCException} and is stored as
     * {@code finalException}.</p>
     *
     * <p>The OR process is still alive after the orchestrator terminates; it is
     * explicitly interrupted in the test tear-down to avoid dangling threads.</p>
     */
    public void testOrCallerInterruptedTerminatesWithKlavaException() throws Exception {
        /* no tuple inserted — the OR process blocks forever on its guard */
        InGuardProcess process = new InGuardProcess("A");

        OrOrchestrator orchestrator = new OrOrchestrator(List.of(process));
        node.eval(orchestrator);

        /* interrupt the orchestrator after a short delay to let it reach join() */
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            orchestrator.interrupt();
        });
        interrupter.start();
        interrupter.join();

        orchestrator.join(5000);
        assertFalse("Orchestrator should have terminated", orchestrator.isAlive());

        /* or() wraps the InterruptedException in KlavaException;
         * execute() wraps that in IMCException and stores it as finalException */
        assertNotNull("Orchestrator should have a non-null finalException",
                orchestrator.getFinalException());
        assertTrue("The finalException cause should be a KlavaException",
                orchestrator.getFinalException().getCause() instanceof KlavaException);

        /* clean up: the OR process is still blocking on in("A"); interrupt it
         * so the test leaves no dangling threads */
        process.interrupt();
        process.join(5000);
    }
}
