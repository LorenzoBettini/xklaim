package klava.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import klava.KlavaException;

/**
 * A mutex shared among the processes participating in an OR operator.
 *
 * <p>
 * The OR operator allows a set of {@link KlavaOrProcess} instances to compete
 * for the first successful guard operation. This mutex
 * coordinates the competition:
 * </p>
 * <ul>
 * <li>It tracks which process wins (via {@link #isFirst()}).</li>
 * <li>It holds references to all participating processes so that the winner can
 * interrupt the others (via {@link #interruptOthers(KlavaOrProcess)}).</li>
 * </ul>
 *
 * <p>
 * A new instance is created by {@link KlavaProcess#or(List)} and is shared
 * among all the processes in the OR before any of them is started.
 * </p>
 *
 * @see KlavaOrProcess
 * @see KlavaProcess#or(List)
 */
public class KlavaOrMutex {

    /**
     * Whether the first successful guard has not yet happened. Starts as
     * {@code true}; set to {@code false} atomically by the first call to
     * {@link #isFirst()}.
     */
    private boolean first = true;

    /**
     * All the processes participating in this OR operator. Populated by
     * {@link KlavaProcess#or(List)} before any process is started.
     */
    private final List<KlavaOrProcess> processes = new ArrayList<>();

    /**
     * Returns {@code true} if the calling process is the first to succeed in its
     * guard operation, and atomically marks the slot as taken so that all
     * subsequent callers receive {@code false}.
     *
     * <p>
     * This method is {@code synchronized} to guarantee that exactly one process
     * gets {@code true}, even under concurrent access.
     * </p>
     *
     * @return {@code true} for the first caller; {@code false} for all subsequent
     *         callers
     */
    public synchronized boolean isFirst() {
        if (first) {
            first = false;
            return true;
        }
        return false;
    }

    /**
     * Runs an OUT or EVAL guard while holding the winner slot. A failed operation
     * leaves the slot available for another branch; a successful one commits the
     * winner before releasing the lock. Retrieval guards use the same lock through
     * {@link #isFirst()}.
     */
    synchronized void executeIfFirst(KlavaOrProcess process, Runnable operation) {
        if (!first || process.isInterrupted()) {
            process.interrupt();
            throw new KlavaException(new InterruptedException("OR branch lost"));
        }
        operation.run();
        first = false;
        interruptOthers(process);
    }

    /**
     * Adds a process to the collection of OR processes tracked by this mutex.
     *
     * <p>
     * Called by {@link KlavaProcess#or(List)} before the processes are started, so
     * that {@link #interruptOthers(KlavaOrProcess)} has a complete list available
     * at interrupt time.
     * </p>
     *
     * @param process the process to register
     */
    void addProcess(KlavaOrProcess process) {
        processes.add(process);
    }

    /**
     * Interrupts all OR processes <em>except</em> the given winner process.
     *
     * <p>
     * Called by the winning process (the first to complete its guard) to
     * interrupt all competitors. Plain Java code in a losing process can keep
     * running until an interruptible wait or a guarded Klaim operation observes
     * the interruption. An interruption propagated as a {@link KlavaException}
     * is treated as normal cancellation by the process wrapper.
     * </p>
     *
     * @param winner the process that must <em>not</em> be interrupted
     */
    void interruptOthers(KlavaOrProcess winner) {
        for (KlavaOrProcess p : processes) {
            if (p != winner) {
                p.interrupt();
            }
        }
    }

    void executeInOr(List<KlavaOrProcess> processes, Consumer<KlavaOrProcess> executor) throws KlavaException {
        /*
         * register all processes in the mutex before starting any of them, so that
         * interruptOthers() has a complete list when called
         */
        for (KlavaOrProcess p : processes) {
            p.setOrMutex(this);
            this.addProcess(p);
        }
        /* start all processes in parallel at the local node */
        for (KlavaOrProcess p : processes) {
            executor.accept(p);
        }
        /*
         * join all processes; accumulate any InterruptedException so we finish joining
         * all threads even if this thread is interrupted
         */
        InterruptedException interrupted = null;
        for (KlavaOrProcess p : processes) {
            try {
                p.join();
            } catch (InterruptedException e) { // NOSONAR: we handle this in the following lines
                interrupted = e;
            }
        }
        if (interrupted != null) {
            Thread.currentThread().interrupt();
            throw new KlavaException(interrupted);
        }
    }
}
