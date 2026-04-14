package klava.topology;

import java.util.ArrayList;
import java.util.List;

/**
 * A mutex shared among the processes participating in an OR operator.
 *
 * <p>The OR operator allows a set of {@link KlavaOrProcessInterface} instances to
 * compete for the first successful retrieval operation (the "guard"). This
 * mutex coordinates the competition:</p>
 * <ul>
 *   <li>It tracks which process wins (via {@link #isFirst()}).</li>
 *   <li>It holds references to all participating processes so that the winner
 *       can interrupt the others (via {@link #interruptOthers(KlavaOrProcessInterface)}).</li>
 * </ul>
 *
 * <p>A new instance is created by {@link KlavaProcess#or(List)} and is shared
 * among all the processes in the OR before any of them is started.</p>
 *
 * @see KlavaOrProcess
 * @see KlavaProcess#or(List)
 */
public class KlavaOrMutex {

	/**
	 * Whether the first successful retrieval has not yet happened.
	 * Starts as {@code true}; set to {@code false} atomically by the first
	 * call to {@link #isFirst()}.
	 */
	private boolean first = true;

	/**
	 * All the processes participating in this OR operator.
	 * Populated by {@link KlavaProcess#or(List)} before any process is started.
	 */
	private final List<KlavaOrProcessInterface> processes = new ArrayList<>();

	/**
	 * Returns {@code true} if the calling process is the first to succeed in
	 * its guard retrieval operation, and atomically marks the slot as taken so
	 * that all subsequent callers receive {@code false}.
	 *
	 * <p>This method is {@code synchronized} to guarantee that exactly one
	 * process gets {@code true}, even under concurrent access.</p>
	 *
	 * @return {@code true} for the first caller; {@code false} for all subsequent callers
	 */
	public synchronized boolean isFirst() {
		if (first) {
			first = false;
			return true;
		}
		return false;
	}

	/**
	 * Adds a process to the collection of OR processes tracked by this mutex.
	 *
	 * <p>Called by {@link KlavaProcess#or(List)} before the processes are
	 * started, so that {@link #interruptOthers(KlavaOrProcessInterface)} has a complete
	 * list available at interrupt time.</p>
	 *
	 * @param process the process to register
	 */
	void addProcess(KlavaOrProcessInterface process) {
		processes.add(process);
	}

	/**
	 * Interrupts all OR processes <em>except</em> the given winner process.
	 *
	 * <p>Called by the winning process (the first to complete its guard) to
	 * terminate all competitors. Each interrupted process will receive an
	 * {@link InterruptedException} from its blocking retrieval operation, which
	 * will be wrapped in a {@link klava.KlavaException}.</p>
	 *
	 * @param winner the process that must <em>not</em> be interrupted
	 */
	void interruptOthers(KlavaOrProcessInterface winner) {
		for (KlavaOrProcessInterface p : processes) {
			if (p != winner) {
				p.interrupt();
			}
		}
	}
}
