package klava.tests.junit;

import junit.framework.TestCase;
import klava.KlavaException;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import org.mikado.imc.common.IMCException;

/**
 * Tests for {@link KlavaNodeCoordinator#done()} and
 * {@link KlavaNode#waitForCompletion()}.
 */
public class KlavaNodeDoneTest extends TestCase {

	/**
	 * A subclass of {@link KlavaNode} that exposes the protected methods for
	 * testing.
	 */
	private static class TestNode extends KlavaNode {
		void startMainCoordinator(KlavaNodeCoordinator coordinator) throws IMCException {
			setMainCoordinator(coordinator);
			addNodeCoordinator(coordinator);
		}

		void addManagedChildNode(KlavaNode childNode) {
			addManagedNode(childNode);
		}
	}

	/**
	 * A coordinator that calls {@link #done()} before setting the
	 * {@code reached} flag, to verify that execution stops immediately.
	 */
	private static class DoneCoordinator extends KlavaNodeCoordinator {
		private static final long serialVersionUID = 1L;

		boolean reached = false;

		@Override
		public void executeProcess() throws KlavaException {
			done();
			reached = true;
		}
	}

	/**
	 * {@link KlavaNodeCoordinator#done()} causes
	 * {@link KlavaNodeCoordinator#execute()} to return without throwing an
	 * exception.
	 */
	public void testDoneStopsExecution() throws IMCException {
		DoneCoordinator coordinator = new DoneCoordinator();
		// Must not throw any exception
		coordinator.execute();
	}

	/**
	 * Code placed after the {@link KlavaNodeCoordinator#done()} call is never
	 * executed.
	 */
	public void testDoneExitsImmediately() throws IMCException {
		DoneCoordinator coordinator = new DoneCoordinator();
		coordinator.execute();
		assertFalse("Code after done() should not be executed", coordinator.reached);
	}

	/**
	 * {@link KlavaNode#waitForCompletion()} waits for the main coordinator
	 * thread to finish and then closes the node.
	 */
	public void testWaitForCompletionWithMainCoordinator()
			throws IMCException, InterruptedException {
		TestNode node = new TestNode();
		DoneCoordinator coordinator = new DoneCoordinator();
		node.startMainCoordinator(coordinator);
		node.waitForCompletion();
		assertFalse("Coordinator must have run and exited via done()",
				coordinator.reached);
	}

	/**
	 * {@link KlavaNode#waitForCompletion()} waits for all managed child nodes
	 * to complete before returning.
	 */
	public void testWaitForCompletionWithManagedNodes()
			throws IMCException, InterruptedException {
		TestNode parentNode = new TestNode();

		TestNode child1 = new TestNode();
		DoneCoordinator coord1 = new DoneCoordinator();
		child1.startMainCoordinator(coord1);
		parentNode.addManagedChildNode(child1);

		TestNode child2 = new TestNode();
		DoneCoordinator coord2 = new DoneCoordinator();
		child2.startMainCoordinator(coord2);
		parentNode.addManagedChildNode(child2);

		parentNode.waitForCompletion();
		assertFalse("Child 1 coordinator must have run and exited via done()",
				coord1.reached);
		assertFalse("Child 2 coordinator must have run and exited via done()",
				coord2.reached);
	}
}
