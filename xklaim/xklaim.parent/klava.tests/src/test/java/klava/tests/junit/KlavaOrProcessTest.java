package klava.tests.junit;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
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
 * <p>Retrieval guard tests cover both a single matching tuple and guards that
 * both succeed before the OR winner is selected.</p>
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

    /** Records ordinary Java work before competing on the first IN. */
    private static class PreGuardProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final CountDownLatch started;
        private volatile boolean passedGuard;

        PreGuardProcess(CountDownLatch started) {
            this.started = started;
        }

        @Override
        public void executeProcess() throws KlavaException {
            started.countDown();
            try {
                started.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new KlavaException(e);
            }
            in(new Tuple(new KString("A")), self);
            passedGuard = true;
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

    /** Pauses one branch after its retrieval has succeeded, before the OR claim. */
    private static class PausingRetrievalNode extends KlavaNode {
        private volatile Thread pausedBranch;
        private final CountDownLatch retrieved = new CountDownLatch(1);
        private final CountDownLatch resume = new CountDownLatch(1);

        void pauseAfterRetrieval(Thread branch) {
            pausedBranch = branch;
        }

        private void afterRetrieval() {
            if (Thread.currentThread() != pausedBranch) {
                return;
            }
            retrieved.countDown();
            boolean interrupted = false;
            for (;;) {
                try {
                    resume.await();
                    break;
                } catch (InterruptedException e) {
                    // Keep the branch paused even when the winner interrupts it.
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void in(Tuple tuple, Locality destination) throws KlavaException {
            super.in(tuple, destination);
            afterRetrieval();
        }

        @Override
        public boolean in_nb(Tuple tuple, Locality destination) throws KlavaException {
            boolean succeeded = super.in_nb(tuple, destination);
            if (succeeded) {
                afterRetrieval();
            }
            return succeeded;
        }

        @Override
        public boolean in_t(Tuple tuple, Locality destination, long timeout) throws KlavaException {
            boolean succeeded = super.in_t(tuple, destination, timeout);
            if (succeeded) {
                afterRetrieval();
            }
            return succeeded;
        }

        @Override
        public void read(Tuple tuple, Locality destination) throws KlavaException {
            super.read(tuple, destination);
            afterRetrieval();
        }

        @Override
        public boolean read_nb(Tuple tuple, Locality destination) throws KlavaException {
            boolean succeeded = super.read_nb(tuple, destination);
            if (succeeded) {
                afterRetrieval();
            }
            return succeeded;
        }

        @Override
        public boolean read_t(Tuple tuple, Locality destination, long timeout) throws KlavaException {
            boolean succeeded = super.read_t(tuple, destination, timeout);
            if (succeeded) {
                afterRetrieval();
            }
            return succeeded;
        }
    }

    private static class RacingRetrievalProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;
        private final String operation;
        private final String expected;
        private final CountDownLatch startAfter;
        private final CountDownLatch bodyDone = new CountDownLatch(1);
        private volatile boolean bodyExecuted;

        RacingRetrievalProcess(String operation, String expected, CountDownLatch startAfter) {
            this.operation = operation;
            this.expected = expected;
            this.startAfter = startAfter;
        }

        @Override
        public void executeProcess() throws KlavaException {
            if (startAfter != null) {
                try {
                    startAfter.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new KlavaException(e);
                }
            }
            Tuple template = new Tuple(new KString(expected), new KString());
            switch (operation) {
                case "in" -> in(template, self);
                case "in_nb" -> assertSuccessful(in_nb(template, self));
                case "in_t" -> assertSuccessful(in_t(template, self, 5000));
                case "read" -> read(template, self);
                case "read_nb" -> assertSuccessful(read_nb(template, self));
                case "read_t" -> assertSuccessful(read_t(template, self, 5000));
                default -> throw new IllegalArgumentException(operation);
            }
            bodyExecuted = true;
            bodyDone.countDown();
        }

        private void assertSuccessful(boolean succeeded) {
            if (!succeeded) {
                throw new AssertionError(operation + " did not retrieve the available tuple");
            }
        }
    }

    /** Tries a missing tuple, then waits for the test to supply a matching one. */
    private static class RetryingGuardProcess extends KlavaOrProcess {

        private static final long serialVersionUID = 1L;

        private final String operation;
        private final CountDownLatch firstAttemptDone = new CountDownLatch(1);
        private final CountDownLatch tryAgain = new CountDownLatch(1);
        private boolean firstResult;
        private boolean secondResult;

        RetryingGuardProcess(String operation) {
            this.operation = operation;
        }

        @Override
        public void executeProcess() throws KlavaException {
            firstResult = retrieve(new Tuple(new KString("missing-" + operation)));
            firstAttemptDone.countDown();
            try {
                tryAgain.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new KlavaException(e);
            }
            secondResult = retrieve(new Tuple(new KString("present-" + operation)));
        }

        private boolean retrieve(Tuple tuple) throws KlavaException {
            if ("in_nb".equals(operation)) {
                return in_nb(tuple, self);
            }
            if ("read_nb".equals(operation)) {
                return read_nb(tuple, self);
            }
            if ("in_t".equals(operation)) {
                return in_t(tuple, self, 1);
            }
            return read_t(tuple, self, 1);
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

    /** Child used by EVAL and executeNodeProcess; its flag records that it ran. */
    private static class ChildProcess extends KlavaProcess {
        private static final long serialVersionUID = 1L;
        private volatile boolean executed;

        @Override
        public void executeProcess() {
            executed = true;
        }
    }

    /**
     * The branch whose first operation is selected by the test parameter:
     * {@code out("side-effect")@self}, {@code eval(child)@self}, or
     * {@code executeNodeProcess(child)}. The optional latch parks this branch
     * until another branch has won, so it can test rejection before execution.
     */
    private static class OperationGuardProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;
        private final String operation;
        private final ChildProcess child = new ChildProcess();
        private final CountDownLatch ready;
        private volatile boolean completed;
        private volatile boolean rejected;

        OperationGuardProcess(String operation, CountDownLatch ready) {
            this.operation = operation;
            this.ready = ready;
        }

        @Override
        public void executeProcess() throws KlavaException {
            if (ready != null) {
                // Tell the competing branch that this process has started.
                ready.countDown();
                try {
                    // The competitor interrupts this wait after winning.
                    new CountDownLatch(1).await();
                } catch (InterruptedException e) {
                    // The winning guard releases us; attempt the operation anyway.
                }
            }
            try {
                performOperation();
                completed = true;
            } catch (KlavaException e) {
                rejected = e.wasCausedByInterruptedException();
                throw e;
            }
        }

        private void performOperation() throws KlavaException {
            if ("out".equals(operation)) {
                out(new Tuple(new KString("side-effect")), self);
            } else if ("eval".equals(operation)) {
                eval(child, self);
            } else {
                executeNodeProcess(child);
            }
        }
    }

    /**
     * Tries OUT or EVAL at an unmapped locality, catches the failure, then
     * retries with {@code in("retry-<operation>")@self} as its guard.
     */
    private static class FailingGuardProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;
        private final String operation;
        private volatile boolean failed;
        private volatile boolean retried;

        FailingGuardProcess(String operation) {
            this.operation = operation;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                if ("out".equals(operation)) {
                    out(new Tuple(new KString("failed")), new LogicalLocality("unmapped"));
                } else {
                    eval(new ChildProcess(), new LogicalLocality("unmapped"));
                }
            } catch (KlavaException e) {
                failed = true;
            }
            in(new Tuple(new KString("retry-" + operation)), self);
            retried = true;
        }
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Each iteration runs this OR, with {@code op} standing for OUT, EVAL, or
     * executeNodeProcess (the last is the Java runtime operation):
     * <pre>{@code
     * or(
     *     proc { op("side-effect" or child)@self },
     *     proc { in("absent-<op>")@self }
     * )
     * }</pre>
     * The second branch has no matching tuple. Verify both that the first
     * branch ran and that it interrupted the waiting branch.
     */
    public void testOutEvalAndExecuteNodeProcessCanWin() throws Exception {
        for (String operation : List.of("out", "eval", "executeNodeProcess")) {
            OperationGuardProcess winner = new OperationGuardProcess(operation, null);
            InGuardProcess competitor = new InGuardProcess("absent-" + operation);
            OrOrchestrator orchestrator = new OrOrchestrator(List.of(winner, competitor));
            node.eval(orchestrator);
            orchestrator.join(5000);

            assertFalse(operation + " should finish", orchestrator.isAlive());
            assertTrue(operation + " should complete", winner.completed);
            assertTrue(operation + " should interrupt the competitor", competitor.isInterrupted());
            if ("out".equals(operation)) {
                assertTrue(node.in_nb(new Tuple(new KString("side-effect"))));
            } else {
                winner.child.join(5000);
                assertTrue(operation + " child should execute", winner.child.executed);
            }
        }
    }

    /**
     * Runs the same OR with a READ guard that is guaranteed to win first:
     * <pre>{@code
     * or(
     *     proc { op("side-effect" or child)@self },
     *     proc { read("ready-<op>")@self }
     * )
     * }</pre>
     * The loser announces that it has started, then waits. The READ branch
     * waits for that announcement before claiming the mutex. Its interrupt
     * releases the loser, which deliberately attempts its operation anyway.
     * That attempt must throw before producing a tuple or starting a child.
     */
    public void testOutEvalAndExecuteNodeProcessLoseBeforeExecution() throws Exception {
        for (String operation : List.of("out", "eval", "executeNodeProcess")) {
            CountDownLatch ready = new CountDownLatch(1);
            OperationGuardProcess loser = new OperationGuardProcess(operation, ready);
            KlavaOrProcess winner = new KlavaOrProcess() {
                private static final long serialVersionUID = 1L;

                @Override
                public void executeProcess() throws KlavaException {
                    try {
                        ready.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new KlavaException(e);
                    }
                    read(new Tuple(new KString("ready-" + operation)), self);
                }
            };
            node.out(new Tuple(new KString("ready-" + operation)));
            OrOrchestrator orchestrator = new OrOrchestrator(List.of(loser, winner));
            node.eval(orchestrator);
            orchestrator.join(5000);

            assertFalse(operation + " should finish", orchestrator.isAlive());
            assertTrue(operation + " should be rejected", loser.rejected);
            assertTrue(operation + " loser should be interrupted", loser.isInterrupted());
            assertNull(loser.getFinalException());
            assertFalse(loser.completed);
            if ("out".equals(operation)) {
                assertFalse(node.in_nb(new Tuple(new KString("side-effect"))));
            } else {
                assertFalse(operation + " child should not start", loser.child.isAlive());
                assertFalse(loser.child.executed);
            }
        }
    }

    /**
     * An exception must leave the same branch's guard pending:
     * <pre>{@code
     * or(
     *     proc { try { op(...)@unmapped } catch (KlavaException e) {}
     *            in("retry-<op>")@self },
     *     proc { in("absent-<op>")@self }
     * )
     * }</pre>
     * Only the retry tuple exists. It must let the first branch claim the OR.
     */
    public void testFailedOutAndEvalLeaveGuardAvailableForRetry() throws Exception {
        for (String operation : List.of("out", "eval")) {
            node.out(new Tuple(new KString("retry-" + operation)));
            FailingGuardProcess winner = new FailingGuardProcess(operation);
            InGuardProcess competitor = new InGuardProcess("absent-" + operation);
            OrOrchestrator orchestrator = new OrOrchestrator(List.of(winner, competitor));
            node.eval(orchestrator);
            orchestrator.join(5000);

            assertFalse(operation + " should finish", orchestrator.isAlive());
            assertTrue(operation + " should throw", winner.failed);
            assertTrue(operation + " should permit a later guard", winner.retried);
            assertTrue(competitor.isInterrupted());
        }
    }

    /**
     * A failed OUT or EVAL must also leave the mutex available to a different
     * branch:
     * <pre>{@code
     * or(
     *     proc { op(...)@unmapped },
     *     proc { in("available-<op>")@self }
     * )
     * }</pre>
     * The second branch waits until the first call has returned or thrown;
     * otherwise it could win before the failure and miss the behavior under test.
     */
    public void testFailedOutAndEvalAllowAnotherBranchToWin() throws Exception {
        for (String operation : List.of("out", "eval")) {
            CountDownLatch failed = new CountDownLatch(1);
            KlavaOrProcess failing = new KlavaOrProcess() {
                private static final long serialVersionUID = 1L;

                @Override
                public void executeProcess() throws KlavaException {
                    try {
                        if ("out".equals(operation)) {
                            out(new Tuple(new KString("failed")), new LogicalLocality("unmapped"));
                        } else {
                            eval(new ChildProcess(), new LogicalLocality("unmapped"));
                        }
                    } finally {
                        // Permit the other branch to claim only after this attempt.
                        failed.countDown();
                    }
                }
            };
            KlavaOrProcess succeeding = new KlavaOrProcess() {
                private static final long serialVersionUID = 1L;

                @Override
                public void executeProcess() throws KlavaException {
                    try {
                        failed.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new KlavaException(e);
                    }
                    in(new Tuple(new KString("available-" + operation)), self);
                }
            };
            node.out(new Tuple(new KString("available-" + operation)));
            OrOrchestrator orchestrator = new OrOrchestrator(List.of(failing, succeeding));
            node.eval(orchestrator);
            orchestrator.join(5000);

            assertFalse(operation + " should finish", orchestrator.isAlive());
            assertNotNull(operation + " failure should propagate", failing.getFinalException());
            assertFalse(operation + " should not interrupt other branches", succeeding.isInterrupted());
            assertFalse(node.in_nb(new Tuple(new KString("available-" + operation))));
        }
    }

    /**
     * Once IN wins, later operations in that branch run as ordinary operations:
     * <pre>{@code
     * or(
     *     proc { in("guard")@self; out("after-guard")@self;
     *            eval(child)@self; executeNodeProcess(child) },
     *     proc { in("absent")@self }
     * )
     * }</pre>
     * This verifies that the new overrides do not try to claim the mutex again.
     */
    public void testOperationsAfterWinningRetrievalBypassOrCompetition() throws Exception {
        node.out(new Tuple(new KString("guard")));
        KlavaOrProcess winner = new KlavaOrProcess() {
            private static final long serialVersionUID = 1L;
            private final ChildProcess child = new ChildProcess();

            @Override
            public void executeProcess() throws KlavaException {
                in(new Tuple(new KString("guard")), self);
                out(new Tuple(new KString("after-guard")), self);
                eval(child, self);
                executeNodeProcess(new ChildProcess());
                try {
                    child.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new KlavaException(e);
                }
                if (!child.executed) {
                    throw new KlavaException("EVAL child did not execute");
                }
            }
        };
        InGuardProcess competitor = new InGuardProcess("absent");
        OrOrchestrator orchestrator = new OrOrchestrator(List.of(winner, competitor));
        node.eval(orchestrator);
        orchestrator.join(5000);

        assertFalse(orchestrator.isAlive());
        assertNull(winner.getFinalException());
        assertTrue(node.in_nb(new Tuple(new KString("after-guard"))));
        assertTrue(competitor.isInterrupted());
    }

    public void testBooleanRetrievalsOnlyClaimOrAfterSuccess() throws Exception {
        for (String operation : List.of("in_nb", "read_nb", "in_t", "read_t")) {
            RetryingGuardProcess process = new RetryingGuardProcess(operation);
            InGuardProcess competitor = new InGuardProcess("competitor-" + operation);
            OrOrchestrator orchestrator = new OrOrchestrator(List.of(process, competitor));
            node.eval(orchestrator);
            try {
                assertTrue(operation + " first attempt should complete",
                        process.firstAttemptDone.await(5, TimeUnit.SECONDS));
                assertFalse(operation + " should return false for a missing tuple", process.firstResult);
                assertFalse(operation + " must not interrupt the competitor on failure",
                        competitor.isInterrupted());

                node.out(new Tuple(new KString("present-" + operation)));
                process.tryAgain.countDown();
                orchestrator.join(5000);

                assertFalse(operation + " orchestrator should complete", orchestrator.isAlive());
                assertTrue(operation + " should return true for a matching tuple", process.secondResult);
                assertFalse("Competitor body should not execute", competitor.isBodyExecuted());
                assertTrue(operation + " should interrupt the competitor on success",
                        competitor.isInterrupted());
            } finally {
                process.tryAgain.countDown();
                process.interrupt();
                competitor.interrupt();
                process.join(5000);
                competitor.join(5000);
                orchestrator.join(5000);
            }
        }
    }

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

    public void testBothBranchesRunJavaBeforeOnlyOnePassesFirstIn() throws Exception {
        node.out(new Tuple(new KString("A")));

        CountDownLatch started = new CountDownLatch(2);
        PreGuardProcess first = new PreGuardProcess(started);
        PreGuardProcess second = new PreGuardProcess(started);
        OrOrchestrator orchestrator = new OrOrchestrator(List.of(first, second));
        node.eval(orchestrator);
        orchestrator.join(5000);

        assertFalse("OR should finish", orchestrator.isAlive());
        assertEquals("Both branches should run their Java prelude", 0, started.getCount());
        assertTrue("Only one branch should pass IN", first.passedGuard ^ second.passedGuard);
        assertTrue("The loser should be interrupted", first.isInterrupted() ^ second.isInterrupted());
        assertNull(first.getFinalException());
        assertNull(second.getFinalException());
    }

    public void testLosingInRestoresTheExactRemovedTuple() throws Exception {
        for (String operation : List.of("in", "in_nb", "in_t")) {
            assertTwoSuccessfulRetrievals(operation, true);
        }
    }

    public void testLosingReadTerminatesBeforeItsBody() throws Exception {
        for (String operation : List.of("read", "read_nb", "read_t")) {
            assertTwoSuccessfulRetrievals(operation, false);
        }
    }

    private void assertTwoSuccessfulRetrievals(String operation, boolean consuming) throws Exception {
        node.close();
        PausingRetrievalNode pausingNode = new PausingRetrievalNode();
        node = pausingNode;
        String loserValue = consuming ? "loser" : "shared";
        String winnerValue = consuming ? "winner" : "shared";
        Tuple loserTuple = new Tuple(new KString(loserValue), new KString("payload"));
        pausingNode.out(loserTuple);
        if (consuming) {
            pausingNode.out(new Tuple(new KString(winnerValue), new KString("winner-payload")));
        }

        RacingRetrievalProcess loser = new RacingRetrievalProcess(operation, loserValue, null);
        RacingRetrievalProcess winner = new RacingRetrievalProcess(operation, winnerValue, pausingNode.retrieved);
        pausingNode.pauseAfterRetrieval(loser);
        OrOrchestrator orchestrator = new OrOrchestrator(List.of(loser, winner));
        pausingNode.eval(orchestrator);
        try {
            assertTrue(operation + ": loser should retrieve before claiming",
                    pausingNode.retrieved.await(5, TimeUnit.SECONDS));
            assertTrue(operation + ": winner should complete its guard and body",
                    winner.bodyDone.await(5, TimeUnit.SECONDS));
            pausingNode.resume.countDown();
            orchestrator.join(5000);
            assertFalse(operation + ": OR should finish", orchestrator.isAlive());
            assertFalse(operation + ": loser must not run its body", loser.bodyExecuted);
            assertTrue(operation + ": winner should run its body", winner.bodyExecuted);
            assertTrue(operation + ": loser should be interrupted", loser.isInterrupted());
            assertNull(operation + ": loser termination should be normal", loser.getFinalException());

            Tuple restored = new Tuple(new KString(loserValue), new KString());
            assertTrue(operation + ": the loser's tuple should be present", pausingNode.in_nb(restored));
            assertEquals(operation + ": original tuple identity should be preserved",
                    loserTuple.getTupleId(), restored.getMatched().getTupleId());
            assertEquals("payload", restored.getItem(1).toString());
            assertFalse(operation + ": tuple should occur only once",
                    pausingNode.in_nb(new Tuple(new KString(loserValue), new KString())));
            if (consuming) {
                assertFalse(operation + ": winner's tuple should remain consumed",
                        pausingNode.in_nb(new Tuple(new KString(winnerValue), new KString())));
            }
        } finally {
            pausingNode.resume.countDown();
            loser.interrupt();
            winner.interrupt();
            loser.join(5000);
            winner.join(5000);
            orchestrator.join(5000);
        }
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
     * which propagates as a {@link klava.KlavaException}. Klava treats that as
     * normal interruption-driven termination, restoring the loser's interrupt
     * status without recording an uncaught {@code finalException}. TOKEN is
     * inserted after a short delay so the winner can complete its body.</p>
     *
     * <p>The test verifies:</p>
     * <ul>
     *   <li>Exactly one process completed its body (the winner).</li>
     *   <li>Exactly one process has interrupted status (the loser, interrupted
     *       on the blocking body operation).</li>
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

        /* The loser should be interrupted, but interruption should not be
         * stored as an uncaught finalException. */
        assertTrue("Exactly one process should have been interrupted on its body operation",
                processA.isInterrupted() != processB.isInterrupted());
        assertNull("Process A should not store interruption as finalException",
                processA.getFinalException());
        assertNull("Process B should not store interruption as finalException",
                processB.getFinalException());

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
     * {@link klava.KlavaException} and re-throws. Klava treats that as
     * interruption-driven termination, restoring the orchestrator interrupt
     * status without storing a {@code finalException}.</p>
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

        assertNull("Orchestrator should not store interruption as finalException",
                orchestrator.getFinalException());
        assertTrue("Orchestrator should keep interrupted status",
                orchestrator.isInterrupted());

        /* clean up: the OR process is still blocking on in("A"); interrupt it
         * so the test leaves no dangling threads */
        process.interrupt();
        process.join(5000);
    }
}
