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
        public volatile Throwable exception = null;
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
                exception = e;
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
                exception = e;
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
                exception = e;
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
}
