/**
 * Tests for the Klava OR operator
 */
package klava.tests.junit;

import java.util.ArrayList;
import java.util.List;

import org.mikado.imc.common.IMCException;

import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.Tuple;
import klava.topology.KlavaOrMutex;
import klava.topology.KlavaOrProcess;
import klava.topology.KlavaProcess;

/**
 * Tests for the OR operator in Klava processes.
 * 
 * The OR operator allows a process to execute multiple retrieval branches in
 * parallel, with only the first successful retrieval proceeding. All other
 * processes are interrupted, and non-removal operations (read) leave their
 * tuples in the space, while removal operations (in) re-insert the tuple.
 * 
 * @author Lorenzo Bettini
 */
public class KlavaOrTest extends ClientServerBase {

    /**
     * Abstract base class for test processes that participate in an OR. Tracks
     * whether the process successfully completed its retrieval.
     */
    private abstract class OrTestProcess extends KlavaOrProcess {
        private static final long serialVersionUID = 1L;

        public volatile boolean succeeded = false;
        public volatile boolean failed = false;
    }

    /**
     * A process that performs an in (removal) operation on a template.
     */
    private class InOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;

        public InOrProcess(Tuple template, Locality destination) {
            this.template = template;
            this.destination = destination;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                in(template, destination);
                succeeded = true;
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that performs a read (non-removal) operation on a template.
     */
    private class ReadOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;

        public ReadOrProcess(Tuple template, Locality destination) {
            this.template = template;
            this.destination = destination;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                read(template, destination);
                succeeded = true;
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that performs a non-blocking in (in_nb) operation.
     */
    private class InNbOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;
        public volatile boolean nbResult = false;

        public InNbOrProcess(Tuple template, Locality destination) {
            this.template = template;
            this.destination = destination;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                nbResult = in_nb(template, destination);
                if (nbResult) {
                    succeeded = true;
                }
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that performs a non-blocking read (read_nb) operation.
     */
    private class ReadNbOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;
        public volatile boolean nbResult = false;

        public ReadNbOrProcess(Tuple template, Locality destination) {
            this.template = template;
            this.destination = destination;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                nbResult = read_nb(template, destination);
                if (nbResult) {
                    succeeded = true;
                }
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that performs a timed in (in_t) operation.
     */
    private class InTOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;
        private final long timeout;

        public InTOrProcess(Tuple template, Locality destination, long timeout) {
            this.template = template;
            this.destination = destination;
            this.timeout = timeout;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                boolean result = in_t(template, destination, timeout);
                if (result) {
                    succeeded = true;
                }
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that performs a timed read (read_t) operation.
     */
    private class ReadTOrProcess extends OrTestProcess {
        private static final long serialVersionUID = 1L;

        private final Tuple template;
        private final Locality destination;
        private final long timeout;

        public ReadTOrProcess(Tuple template, Locality destination, long timeout) {
            this.template = template;
            this.destination = destination;
            this.timeout = timeout;
        }

        @Override
        public void executeProcess() throws KlavaException {
            try {
                boolean result = read_t(template, destination, timeout);
                if (result) {
                    succeeded = true;
                }
            } catch (KlavaException e) {
                failed = true;
                throw e;
            }
        }
    }

    /**
     * A process that calls the OR operator on a list of processes. This is needed
     * because the or() method must be called from within a process execution
     * context (which provides the klavaNodeProcessProxy).
     */
    private class OrCallerProcess extends KlavaProcess {
        private static final long serialVersionUID = 1L;

        private final List<KlavaOrProcess> branches;

        public OrCallerProcess(List<KlavaOrProcess> branches) {
            this.branches = branches;
        }

        @Override
        public void executeProcess() throws KlavaException {
            or(branches);
        }
    }

    /**
     * Test scenario: exactly one tuple matching only one branch's template. The
     * matching branch should succeed; the non-matching branch should be interrupted
     * and terminated.
     */
    public void testOrOneSucceeds() throws InterruptedException, IMCException, KlavaException {
        // Put a tuple that matches only branch A's template
        Tuple tuple1 = new Tuple(new KString("typeA"));
        clientNode.out(tuple1, self);

        // Create two branches with different templates
        Tuple templateA = new Tuple(new KString("typeA"));
        templateA.setHandleRetrieved(false);

        Tuple templateB = new Tuple(new KString("typeB"));
        templateB.setHandleRetrieved(false);

        InOrProcess branchA = new InOrProcess(templateA, self);
        InOrProcess branchB = new InOrProcess(templateB, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Branch A should have succeeded
        assertTrue("Branch A should succeed", branchA.succeeded);
        // Branch B should have failed (interrupted)
        assertTrue("Branch B should fail", branchB.failed);
    }

    /**
     * Test scenario: two identical tuples in the tuple space; two in-processes with
     * the same template race to retrieve them. Exactly one should succeed, and the
     * tuple space should end up with exactly one tuple (the loser re-inserts the
     * one it retrieved).
     */
    public void testOrBothCouldSucceedInRace() throws InterruptedException, IMCException, KlavaException {
        // Put two tuples matching the template
        Tuple tuple1 = new Tuple(new KString("value"));
        Tuple tuple2 = new Tuple(new KString("value"));
        clientNode.out(tuple1, self);
        clientNode.out(tuple2, self);

        // Create two branches with the same template
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        InOrProcess branchA = new InOrProcess(template1, self);
        InOrProcess branchB = new InOrProcess(template2, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        // The tuple space should have exactly one tuple (the inserted one by the loser)
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Should have a tuple in tuple space", clientNode.in_nb(template, self));
        assertFalse("Should not have a second tuple", clientNode.in_nb(template, self));
    }

    /**
     * Test scenario: one tuple in the tuple space; two read-processes with the same
     * template. Only one should have succeeded=true; the other should have failed.
     * The tuple should remain in the tuple space (because read is non-destructive).
     */
    public void testOrWithRead() throws InterruptedException, IMCException, KlavaException {
        // Put one tuple
        Tuple tuple = new Tuple(new KString("data"));
        clientNode.out(tuple, self);

        // Create two read branches with the same template
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        ReadOrProcess branchA = new ReadOrProcess(template1, self);
        ReadOrProcess branchB = new ReadOrProcess(template2, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        // The tuple should still be in the tuple space
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Tuple should remain in tuple space", clientNode.in_nb(template, self));
    }

    /**
     * Test scenario: tuple matches only branch B's template, not A's. Branch A will
     * block waiting for a non-existent tuple; branch B will immediately succeed.
     */
    public void testOrFirstFails() throws InterruptedException, IMCException, KlavaException {
        // Put a tuple that matches only branch B's template
        Tuple tuple = new Tuple(new KString("typeB"));
        clientNode.out(tuple, self);

        // Create two branches with different templates
        Tuple templateA = new Tuple(new KString("typeA"));
        templateA.setHandleRetrieved(false);

        Tuple templateB = new Tuple(new KString("typeB"));
        templateB.setHandleRetrieved(false);

        InOrProcess branchA = new InOrProcess(templateA, self);
        InOrProcess branchB = new InOrProcess(templateB, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Only branch B should have succeeded
        assertFalse("Branch A should fail", branchA.succeeded);
        assertTrue("Branch B should succeed", branchB.succeeded);
    }

    /**
     * Test scenario: three branches, only one matching tuple. Exactly one should
     * succeed; the others should be interrupted.
     */
    public void testOrThreeProcesses() throws InterruptedException, IMCException, KlavaException {
        // Put one tuple
        Tuple tuple = new Tuple(new KString("typeA"));
        clientNode.out(tuple, self);

        // Create three branches with different templates
        Tuple templateA = new Tuple(new KString("typeA"));
        templateA.setHandleRetrieved(false);

        Tuple templateB = new Tuple(new KString("typeB"));
        templateB.setHandleRetrieved(false);

        Tuple templateC = new Tuple(new KString("typeC"));
        templateC.setHandleRetrieved(false);

        InOrProcess branchA = new InOrProcess(templateA, self);
        InOrProcess branchB = new InOrProcess(templateB, self);
        InOrProcess branchC = new InOrProcess(templateC, self);

        // Execute all branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);
        branches.add(branchC);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0) + (branchC.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);
        // And that one should be A (since it matches the only tuple)
        assertTrue("Branch A should succeed", branchA.succeeded);
    }

    /**
     * Test scenario: no tuples in the tuple space; in_nb on all branches returns
     * false without any mutex interaction. The or() should complete normally
     * without blocking.
     */
    public void testOrWithInNb() throws InterruptedException, IMCException, KlavaException {
        // No tuples in the tuple space

        // Create two non-blocking in branches
        Tuple template1 = new Tuple(new KString("data"));
        Tuple template2 = new Tuple(new KString("data"));

        InNbOrProcess branchA = new InNbOrProcess(template1, self);
        InNbOrProcess branchB = new InNbOrProcess(template2, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Neither should have succeeded (no tuples available)
        assertFalse("Branch A should not succeed", branchA.succeeded);
        assertFalse("Branch B should not succeed", branchB.succeeded);
        // But no errors should have occurred
        assertFalse("Branch A should not fail", branchA.failed);
        assertFalse("Branch B should not fail", branchB.failed);
    }

    /**
     * Test scenario: KlavaOrProcess can be used standalone (not in an OR) without a
     * mutex. A simple in operation should work normally.
     */
    public void testKlavaOrProcessStandalone() throws InterruptedException, IMCException, KlavaException {
        // Put a tuple
        Tuple tuple = new Tuple(new KString("standalone"));
        clientNode.out(tuple, self);

        // Create a standalone or-process without an OR
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);

        InOrProcess standalone = new InOrProcess(template, self);

        // Execute it directly (not through or())
        clientNode.eval(standalone);
        standalone.join();

        // Should have succeeded
        assertTrue("Standalone OrProcess should succeed", standalone.succeeded);
        // Tuple should be gone
        assertFalse("Tuple should be removed", clientNode.in_nb(template, self));
    }

    /**
     * Deterministic test verifying the re-insertion destination is correct.
     *
     * Remote in_nb is NOT truly non-blocking in klava: it blocks internally on
     * Response.waitForResponse (Object.wait), which is interruptible. A race-based
     * test where two branches both try remote in_nb simultaneously can lose a tuple
     * if the server dequeues it and sends the response just as the client thread is
     * interrupted, causing the response to be dropped.
     *
     * Instead this test uses a custom "always-loser" mutex (isFirst() always returns
     * false). With a single branch and no interrupt, the branch completes its remote
     * in_nb, reaches handleOrRetrieval, takes the loser path, re-inserts the tuple
     * at the retrieval destination (serverLoc), and then throws. This is deterministic
     * and directly tests that the re-insertion uses the correct destination.
     */
    public void testOrLoserReinssertsAtRemoteDestination() throws InterruptedException,
            IMCException, KlavaException {
        clientLoginsToServer();

        // Put a tuple on the server so the branch's in_nb returns true
        serverNode.out(new Tuple(new KString("value")));
        Tuple template = new Tuple(new KString());

        // Custom mutex that always reports the caller as the loser.
        // No interrupt is sent (interruptOthers does nothing), so the branch
        // completes its remote retrieval, reaches handleOrRetrieval, and
        // re-inserts the tuple at destination before throwing.
        KlavaOrMutex alwaysLoserMutex = new KlavaOrMutex() {
            @Override
            public synchronized boolean isFirst() {
                return false;
            }

            @Override
            public void interruptOthers(KlavaOrProcess except) {
                // No other processes, nothing to interrupt
            }
        };

        InNbOrProcess branch = new InNbOrProcess(template, serverLoc);
        branch.setMutex(alwaysLoserMutex);
        clientNode.eval(branch);
        branch.join();

        // The branch took the loser path: failed=true, succeeded=false
        assertTrue("Branch should have failed (loser path)", branch.failed);
        assertFalse("Branch should not have succeeded", branch.succeeded);

        /*
         * The loser re-inserts the retrieved tuple at serverLoc (the retrieval
         * destination) via an async eval'd process. With the old bug (out at self
         * instead of destination), the tuple would appear at the client node instead.
         */
        Tuple verifyTemplate = new Tuple(new KString());
        assertTrue("Server should have the re-inserted tuple (re-insertion at serverLoc)",
                clientNode.in_t(verifyTemplate, serverLoc, 5000));

        Tuple selfTemplate = new Tuple(new KString());
        assertFalse("No tuple should be at client self (wrong re-insertion destination)",
                clientNode.in_nb(selfTemplate, self));
    }

    /**
     * Test scenario: two identical tuples in the tuple space; two in_nb-processes
     * both retrieve a tuple (in_nb returns true for each). The OR logic ensures
     * exactly one succeeds and the loser re-inserts its tuple at the destination.
     */
    public void testOrWithInNbBothSucceed() throws InterruptedException, IMCException,
            KlavaException {
        // Put two tuples so both in_nb branches can each retrieve one
        clientNode.out(new Tuple(new KString("value")), self);
        clientNode.out(new Tuple(new KString("value")), self);

        // Create two branches with the same template
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        InNbOrProcess branchA = new InNbOrProcess(template1, self);
        InNbOrProcess branchB = new InNbOrProcess(template2, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // At least one branch should have gotten nbResult=true
        assertTrue("At least one branch should have gotten nbResult=true",
                branchA.nbResult || branchB.nbResult);

        // Exactly one should have succeeded
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        /*
         * The tuple space should have exactly one tuple: winner consumed one,
         * loser re-inserted one (or only one branch got a tuple at all).
         */
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Should have a tuple in tuple space", clientNode.in_nb(template, self));
        assertFalse("Should not have a second tuple", clientNode.in_nb(template, self));
    }

    /**
     * Test scenario: one tuple in the tuple space; two read_nb-processes both see
     * it (read_nb returns true for both, since read is non-destructive). The OR
     * logic ensures exactly one succeeds. The tuple remains in the space because
     * read does not remove it and the loser does not re-insert.
     */
    public void testOrWithReadNb() throws InterruptedException, IMCException, KlavaException {
        // Put one tuple; both read_nb branches can see it (read is non-destructive)
        clientNode.out(new Tuple(new KString("data")), self);

        // Create two read_nb branches with the same template
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        ReadNbOrProcess branchA = new ReadNbOrProcess(template1, self);
        ReadNbOrProcess branchB = new ReadNbOrProcess(template2, self);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded; the other is the loser
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        // The tuple should still be in the tuple space (read does not remove it)
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Tuple should remain in tuple space", clientNode.in_nb(template, self));
    }

    /**
     * Test scenario: two identical tuples in the tuple space; two in_t-processes
     * both retrieve a tuple (in_t returns true since tuples are present). The OR
     * logic ensures exactly one succeeds and the loser re-inserts the tuple.
     */
    public void testOrWithInTBothSucceed() throws InterruptedException, IMCException,
            KlavaException {
        // Put two tuples so both in_t branches can succeed
        clientNode.out(new Tuple(new KString("value")), self);
        clientNode.out(new Tuple(new KString("value")), self);

        // Create two branches with the same template and a generous timeout
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        InTOrProcess branchA = new InTOrProcess(template1, self, 2000);
        InTOrProcess branchB = new InTOrProcess(template2, self, 2000);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        /*
         * The tuple space should have exactly one tuple: winner consumed one,
         * loser re-inserted one (or loser was interrupted before retrieving).
         */
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Should have a tuple in tuple space", clientNode.in_nb(template, self));
        assertFalse("Should not have a second tuple", clientNode.in_nb(template, self));
    }

    /**
     * Test scenario: one tuple in the tuple space; two read_t-processes both see
     * it (read_t returns true since read is non-destructive). The OR logic ensures
     * exactly one succeeds. The tuple remains in the space.
     */
    public void testOrWithReadT() throws InterruptedException, IMCException, KlavaException {
        // Put one tuple; both read_t branches can see it (read is non-destructive)
        clientNode.out(new Tuple(new KString("data")), self);

        // Create two read_t branches with the same template and a generous timeout
        Tuple template1 = new Tuple(new KString());
        template1.setHandleRetrieved(false);

        Tuple template2 = new Tuple(new KString());
        template2.setHandleRetrieved(false);

        ReadTOrProcess branchA = new ReadTOrProcess(template1, self, 2000);
        ReadTOrProcess branchB = new ReadTOrProcess(template2, self, 2000);

        // Execute both branches in an OR
        List<KlavaOrProcess> branches = new ArrayList<>();
        branches.add(branchA);
        branches.add(branchB);

        OrCallerProcess caller = new OrCallerProcess(branches);
        clientNode.eval(caller);
        caller.join();

        // Exactly one should have succeeded; the other is the loser
        int successCount = (branchA.succeeded ? 1 : 0) + (branchB.succeeded ? 1 : 0);
        assertEquals("Exactly one branch should succeed", 1, successCount);

        // The tuple should still be in the tuple space (read does not remove it)
        Tuple template = new Tuple(new KString());
        template.setHandleRetrieved(false);
        assertTrue("Tuple should remain in tuple space", clientNode.in_nb(template, self));
    }
}
