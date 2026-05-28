package klava.tests.junit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
     * 
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

	/**
	 * {@link KlavaNode#waitForCompletion()} waits for both the main coordinator
	 * and managed child nodes when both are present.
	 */
	public void testWaitForCompletionWithMainCoordinatorAndManagedNodes()
			throws IMCException, InterruptedException {
		TestNode parentNode = new TestNode();
		DoneCoordinator mainCoord = new DoneCoordinator();
		parentNode.startMainCoordinator(mainCoord);

		TestNode child = new TestNode();
		DoneCoordinator childCoord = new DoneCoordinator();
		child.startMainCoordinator(childCoord);
		parentNode.addManagedChildNode(child);

		parentNode.waitForCompletion();
		assertFalse("Main coordinator must have run and exited via done()",
				mainCoord.reached);
		assertFalse("Child coordinator must have run and exited via done()",
				childCoord.reached);
	}

	/**
	 * {@link KlavaNode#waitForCompletion(long)} returns within the timeout when
	 * the coordinator finishes quickly.
	 */
	public void testWaitForCompletionWithTimeoutCompletes()
			throws IMCException, InterruptedException {
		TestNode node = new TestNode();
		DoneCoordinator coordinator = new DoneCoordinator();
		node.startMainCoordinator(coordinator);
		node.waitForCompletion(5000);
		assertFalse("Coordinator must have run and exited via done()",
				coordinator.reached);
	}

	/**
	 * {@link KlavaNode#waitForCompletion(long)} returns when the timeout
	 * elapses even if the coordinator has not finished.
	 */
	public void testWaitForCompletionWithTimeoutExpires()
			throws IMCException, InterruptedException {
		TestNode node = new TestNode();
		KlavaNodeCoordinator blockingCoordinator = new KlavaNodeCoordinator() {
			private static final long serialVersionUID = 1L;

			@Override
			public void executeProcess() throws KlavaException {
				try {
					Thread.sleep(60_000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		node.startMainCoordinator(blockingCoordinator);
		long start = System.currentTimeMillis();
		node.waitForCompletion(200);
		long elapsed = System.currentTimeMillis() - start;
		assertTrue("waitForCompletion should have returned after the timeout",
				elapsed < 5000);
		blockingCoordinator.interrupt();
	}

	/**
	 * {@link KlavaNode#isCompleted()} returns {@code true} after
	 * {@link KlavaNode#waitForCompletion()} when the main coordinator finishes
	 * normally via {@link KlavaNodeCoordinator#done()}.
	 */
	public void testIsCompletedAfterWaitForCompletion()
			throws IMCException, InterruptedException {
		TestNode node = new TestNode();
		DoneCoordinator coordinator = new DoneCoordinator();
		node.startMainCoordinator(coordinator);
		node.waitForCompletion(5000);
		assertTrue("isCompleted() should be true after the coordinator finished",
				node.isCompleted());
	}

	/**
	 * {@link KlavaNode#isCompleted()} returns {@code true} after
	 * {@link KlavaNode#waitForCompletion()} when all managed child nodes have
	 * finished normally.
	 */
	public void testIsCompletedWithManagedNodesAfterWaitForCompletion()
			throws IMCException, InterruptedException {
		TestNode parentNode = new TestNode();

		TestNode child1 = new TestNode();
		child1.startMainCoordinator(new DoneCoordinator());
		parentNode.addManagedChildNode(child1);

		TestNode child2 = new TestNode();
		child2.startMainCoordinator(new DoneCoordinator());
		parentNode.addManagedChildNode(child2);

		parentNode.waitForCompletion(5000);
		assertTrue("isCompleted() should be true when all managed nodes finished",
				parentNode.isCompleted());
	}

	/**
	 * {@link KlavaNode#isCompleted()} returns {@code false} while the main
	 * coordinator is still running.
	 */
	public void testIsCompletedReturnsFalseWhileRunning()
			throws IMCException, InterruptedException {
		TestNode node = new TestNode();
		CountDownLatch started = new CountDownLatch(1);
		KlavaNodeCoordinator blockingCoordinator = new KlavaNodeCoordinator() {
			private static final long serialVersionUID = 1L;

			@Override
			public void executeProcess() throws KlavaException {
				started.countDown();
				try {
					Thread.sleep(60_000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		node.startMainCoordinator(blockingCoordinator);
		started.await(5, TimeUnit.SECONDS);
		assertFalse("isCompleted() should be false while coordinator is still running",
				node.isCompleted());
		blockingCoordinator.interrupt();
	}
}
