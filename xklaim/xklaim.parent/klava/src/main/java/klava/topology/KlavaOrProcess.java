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
 * In the OR operator, several processes run concurrently and compete on their
 * first successful Klaim operation. Java statements before that operation run
 * independently in each branch. The winner interrupts all other processes;
 * ordinary Java statements in a losing branch may continue until an
 * interruptible wait or a guarded Klaim operation observes the interruption.
 * </p>
 *
 * <h2>Custom Behavior — First Successful Operation Only</h2>
 * <p>
 * Blocking, timed, and nonblocking retrievals claim the OR after succeeding;
 * OUT and EVAL reserve the mutex while executing and claim it only if they
 * return normally. A failed operation leaves the guard pending.
 * executeNodeProcess claims before starting its child.
 * Subsequent operations do not trigger further OR synchronization.
 * </p>
 *
 * <h2>Important Assumption</h2>
 * <p>
 * Retrieval guards should be <strong>mutually exclusive</strong>. A retrieval
 * may consume a tuple before checking the mutex, even if another branch wins.
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
     * Whether the first successful operation (the guard) is still pending. Set
     * to {@code false} after the guard succeeds, so that
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

    @Override
    protected void out(Tuple tuple, Locality destination) throws KlavaException {
        if (isFirstOperation) {
            orMutex.executeIfFirst(this, () -> super.out(tuple, destination));
            isFirstOperation = false;
        } else {
            super.out(tuple, destination);
        }
    }

    @Override
    protected void eval(KlavaProcess klavaProcess, Locality destination) throws KlavaException {
        if (isFirstOperation) {
            orMutex.executeIfFirst(this, () -> super.eval(klavaProcess, destination));
            isFirstOperation = false;
        } else {
            super.eval(klavaProcess, destination);
        }
    }

    @Override
    public void executeNodeProcess(KlavaProcess klavaProcess) throws KlavaException {
        if (isFirstOperation) {
            if (!orMutex.isFirst()) {
                interruptAndTerminate();
            }
            isFirstOperation = false;
            orMutex.interruptOthers(this);
        }
        super.executeNodeProcess(klavaProcess);
    }

    @Override
    protected boolean in_nb(Tuple tuple, Locality destination) throws KlavaException { // NOSONAR: we want this name
        boolean succeeded = super.in_nb(tuple, destination);
        if (succeeded) {
            handleOrRetrieval();
        }
        return succeeded;
    }

    @Override
    protected boolean in_t(Tuple tuple, Locality destination, long timeout) throws KlavaException { // NOSONAR: we want this name
        boolean succeeded = super.in_t(tuple, destination, timeout);
        if (succeeded) {
            handleOrRetrieval();
        }
        return succeeded;
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
     * OR competition), the resulting {@link KlavaException} propagates to the
     * process wrapper, terminating this process without calling
     * {@link #handleOrRetrieval()} and without treating the interruption as an
     * uncaught failure.
     * </p>
     *
     * {@inheritDoc}
     */
    @Override
    protected void in(Tuple tuple, Locality destination) throws KlavaException {
        super.in(tuple, destination);
        handleOrRetrieval();
    }

    @Override
    protected boolean read_nb(Tuple tuple, Locality destination) throws KlavaException { // NOSONAR: we want this name
        boolean succeeded = super.read_nb(tuple, destination);
        if (succeeded) {
            handleOrRetrieval();
        }
        return succeeded;
    }

    @Override
    protected boolean read_t(Tuple tuple, Locality destination, long timeout) throws KlavaException { // NOSONAR: we want this name
        boolean succeeded = super.read_t(tuple, destination, timeout);
        if (succeeded) {
            handleOrRetrieval();
        }
        return succeeded;
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
     * the OR competition), the resulting {@link KlavaException} propagates to the
     * process wrapper, terminating this process without calling
     * {@link #handleOrRetrieval()} and without treating the interruption as an
     * uncaught failure.
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
     * Handles OR synchronization after a retrieval operation succeeds.
     *
     * <p>
     * This method is a no-op unless the current retrieval is the first one (the
     * guard). When called for the guard, it:
     * </p>
     * <ol>
     * <li>Calls {@link KlavaOrMutex#isFirst()} to check atomically whether this
     * process is the first to succeed in its guard.</li>
     * <li>If first: calls {@link KlavaOrMutex#interruptOthers(KlavaOrProcess)} to
     * interrupt all competing processes, then returns so that this process
     * continues its body.</li>
     * <li>Otherwise, interrupts this branch and terminates it.</li>
     * </ol>
     *
     */
    private void handleOrRetrieval() {
        if (!isFirstOperation) {
            /* not the guard — no OR synchronization needed */
            return;
        }
        if (orMutex.isFirst()) {
            isFirstOperation = false;
            /* this process won the OR competition — interrupt all others */
            orMutex.interruptOthers(this);
        } else {
            interruptAndTerminate();
        }
    }

    private void interruptAndTerminate() {
        interrupt();
        throw new KlavaException(new InterruptedException("OR branch lost"));
    }
}
