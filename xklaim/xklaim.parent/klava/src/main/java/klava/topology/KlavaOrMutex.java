/*
 * Klava OR operator synchronization mutex
 */
package klava.topology;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronization mutex used to coordinate retrieval operations in an OR
 * (nondeterministic choice) construct.
 * 
 * The mutex ensures that exactly one process in the OR succeeds in retrieving
 * a tuple, while all other processes are interrupted and terminated after
 * potentially re-inserting any removed tuple.
 * 
 * @author Lorenzo Bettini
 */
public class KlavaOrMutex {

	/**
	 * Flag indicating whether the first process has already succeeded in a
	 * retrieval operation. Starts true; set to false by the first call to
	 * isFirst().
	 */
	private boolean firstSucceeded = true;

	/**
	 * List of all processes participating in this OR. Used to interrupt all
	 * processes except the winner when the first successful retrieval occurs.
	 */
	private final List<KlavaOrProcess> processes = new ArrayList<>();

	/**
	 * Adds a process to the list of processes in this OR.
	 * 
	 * @param process
	 *            the KlavaOrProcess to add
	 */
	public void addProcess(KlavaOrProcess process) {
		processes.add(process);
	}

	/**
	 * Checks whether this is the first successful call to isFirst(). This method
	 * is synchronized to ensure atomicity: exactly one caller will return true,
	 * and all subsequent callers will return false.
	 * 
	 * @return true if this is the first call, false otherwise
	 */
	public synchronized boolean isFirst() {
		if (firstSucceeded) {
			firstSucceeded = false;
			return true;
		}
		return false;
	}

	/**
	 * Interrupts all processes in the OR except the one passed as parameter
	 * (typically the winner).
	 * 
	 * The interruption is done by calling Thread.interrupt() on each process's
	 * thread. The interrupted process will handle the interrupt appropriately
	 * when it reaches a blocking operation or checks the interrupt flag.
	 * 
	 * @param except
	 *            the process to exclude from interruption (typically the first
	 *            process to succeed)
	 */
	public void interruptOthers(KlavaOrProcess except) {
		for (KlavaOrProcess process : processes) {
			if (process != except) {
				process.interrupt();
			}
		}
	}
}
