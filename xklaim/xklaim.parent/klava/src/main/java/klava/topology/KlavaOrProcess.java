package klava.topology;

import klava.KlavaException;
import klava.Locality;
import klava.Tuple;

/**
 * A specialization of {@link KlavaProcess} designed for use inside the OR
 * operator ({@link KlavaProcess#or(java.util.List)}).
 *
 * <h2>OR Operator Semantics</h2>
 * <p>
 * In the OR operator, several processes compete on their first (guard)
 * retrieval operation. Only the first process to complete its guard proceeds
 * with the rest of its body; all other processes are interrupted and terminate.
 * </p>
 *
 * <h2>Custom Behavior — First Retrieval Only</h2>
 * <p>
 * This class overrides the blocking retrieval operations
 * ({@link #in(Tuple, Locality)} and {@link #read(Tuple, Locality)}) to add OR
 * synchronization logic. The custom behavior is applied <strong>only to the
 * first retrieval operation</strong> executed by the process (i.e., the guard).
 * After the guard has been executed, subsequent retrieval operations are
 * delegated directly to the superclass without any synchronization overhead.
 * </p>
 *
 * <h2>Important Assumption</h2>
 * <p>
 * The guard operations of the processes in an OR are assumed to be
 * <strong>mutually exclusive</strong>: at most one process can have its guard
 * succeed at any given time. If two guards succeed simultaneously, the behavior
 * of the OR operator is undefined and it is not guaranteed that only one
 * process will proceed.
 * </p>
 *
 * @see KlavaOrMutex
 * @see KlavaProcess#or(java.util.List)
 */
public abstract class KlavaOrProcess extends KlavaProcess {

    private static final long serialVersionUID = 1L;

    /**
     * The mutex shared with all other processes participating in the same OR. Set
     * by {@link KlavaProcess#or(java.util.List)} before the process is started.
     */
    private transient KlavaOrMutex orMutex;

    /**
     * Whether the next retrieval operation is still the first one (the guard). Set
     * to {@code false} after the first retrieval operation completes, so that
     * subsequent operations proceed without OR synchronization.
     */
    private boolean isFirstOperation = true;

    /**
     * Sets the shared OR mutex. Called by {@link KlavaProcess#or(java.util.List)}
     * before the process is started.
     *
     * @param orMutex the mutex shared with the other OR processes
     */
    void setOrMutex(KlavaOrMutex orMutex) {
        this.orMutex = orMutex;
    }

    /**
     * Performs an IN retrieval at the given destination, with OR synchronization
     * for the first (guard) retrieval only.
     *
     * <p>
     * If this is the first retrieval operation (the guard): after the {@code in}
     * completes, {@link #handleOrRetrieval()} is called. If this process is the
     * first OR process to succeed, it interrupts all the others. Subsequent calls
     * bypass the OR synchronization entirely.
     * </p>
     *
     * <p>
     * If the underlying {@code in} is interrupted (because another process won the
     * OR competition), the resulting {@link KlavaException} propagates normally,
     * terminating this process without calling {@link #handleOrRetrieval()}.
     * </p>
     *
     * {@inheritDoc}
     */
    @Override
    protected void in(Tuple tuple, Locality destination) throws KlavaException {
        super.in(tuple, destination);
        handleOrRetrieval();
    }

    /**
     * Performs a READ retrieval at the given destination, with OR synchronization
     * for the first (guard) retrieval only.
     *
     * <p>
     * If this is the first retrieval operation (the guard): after the {@code read}
     * completes, {@link #handleOrRetrieval()} is called. If this process is the
     * first OR process to succeed, it interrupts all the others. Subsequent calls
     * bypass the OR synchronization entirely.
     * </p>
     *
     * <p>
     * If the underlying {@code read} is interrupted (because another process won
     * the OR competition), the resulting {@link KlavaException} propagates
     * normally, terminating this process without calling
     * {@link #handleOrRetrieval()}.
     * </p>
     *
     * {@inheritDoc}
     */
    @Override
    protected void read(Tuple tuple, Locality destination) throws KlavaException {
        super.read(tuple, destination);
        handleOrRetrieval();
    }

    /**
     * Handles OR synchronization after a guard retrieval operation completes.
     *
     * <p>
     * This method is a no-op unless the current retrieval is the first one (the
     * guard). When called for the guard, it:
     * </p>
     * <ol>
     * <li>Sets {@code isFirstOperation = false} so that subsequent retrievals do
     * not go through this path.</li>
     * <li>Calls {@link KlavaOrMutex#isFirst()} to check atomically whether this
     * process is the first to succeed in its guard.</li>
     * <li>If first: calls {@link KlavaOrMutex#interruptOthers(KlavaOrProcess)} to
     * interrupt all competing processes, then returns so that this process
     * continues its body.</li>
     * </ol>
     *
     * <p>
     * Note: if {@link KlavaOrMutex#isFirst()} returns {@code false} (which should
     * not happen under the mutual exclusion assumption), this process simply
     * continues. The other winning process will have already set the interrupt flag
     * on this process's thread, but since the guard retrieval already completed,
     * the interrupt will be observed by the next blocking operation (if any).
     * </p>
     */
    private void handleOrRetrieval() {
        if (!isFirstOperation) {
            /* not the guard — no OR synchronization needed */
            return;
        }
        /* mark the guard as consumed so subsequent retrievals are not affected */
        isFirstOperation = false;
        if (orMutex.isFirst()) {
            /* this process won the OR competition — interrupt all others */
            orMutex.interruptOthers(this);
        }
    }
}
