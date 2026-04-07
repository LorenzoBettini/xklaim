/*
 * Klava OR operator process
 */
package klava.topology;

import klava.KlavaException;
import klava.Locality;
import klava.Tuple;

/**
 * Abstract base class for processes participating in an OR (nondeterministic
 * choice) operator.
 * 
 * A KlavaOrProcess extends KlavaProcess by redefining the tuple retrieval
 * operations (in, read, in_nb, read_nb, in_t, read_t) to synchronize with a
 * shared KlavaOrMutex. This ensures that:
 * 
 * - Exactly one process in the OR succeeds with its retrieval operation
 * - All other processes are interrupted
 * - If a non-first process removed a tuple (in, in_nb, in_t), it re-inserts
 * the tuple before terminating
 * 
 * KlavaOrProcess can also be used standalone (without an OR operator), in which
 * case the mutex field remains null and the retrieval operations behave exactly
 * like the superclass.
 * 
 * @author Lorenzo Bettini
 */
public abstract class KlavaOrProcess extends KlavaProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The shared mutex for coordinating with other processes in the OR. Null if
	 * this process is not part of an OR.
	 */
	protected KlavaOrMutex mutex;

	/**
	 * Creates a KlavaOrProcess.
	 */
	protected KlavaOrProcess() {
		super();
	}

	/**
	 * Creates a KlavaOrProcess with the given name.
	 * 
	 * @param name
	 *            the name of the process
	 */
	protected KlavaOrProcess(String name) {
		super(name);
	}

	/**
	 * Sets the mutex for this process. This is called by the or() method in
	 * KlavaProcess before executing the process.
	 * 
	 * @param mutex
	 *            the KlavaOrMutex to use for synchronization
	 */
	void setMutex(KlavaOrMutex mutex) {
		this.mutex = mutex;
	}

	/**
	 * Handles the result of an OR retrieval operation. This method is called by
	 * the overridden retrieval methods after a successful retrieval.
	 * 
	 * If this process is not part of an OR (mutex is null), this method does
	 * nothing.
	 * 
	 * If this process is the first to succeed (as determined by a synchronized
	 * check on the mutex), it interrupts all other processes in the OR.
	 * 
	 * If this process is not the first (i.e., another process already succeeded):
	 * - If the retrieval was a removal operation (isRemoval=true), the process
	 * re-inserts the retrieved tuple by spawning an eval'd process that performs
	 * an out operation at self.
	 * - The process then throws a KlavaException to terminate itself.
	 * 
	 * @param retrievedTuple
	 *            the tuple that was successfully retrieved (for re-insertion if
	 *            needed)
	 * @param destination
	 *            the locality where the retrieval was performed
	 * @param isRemoval
	 *            true if the retrieval was a removal operation (in, in_nb, in_t),
	 *            false if it was a read operation (read, read_nb, read_t)
	 * @throws KlavaException
	 *             if this process lost the race (not the first to succeed)
	 */
	private void handleOrRetrieval(Tuple retrievedTuple, Locality destination,
			boolean isRemoval) throws KlavaException {
		/* If not part of an OR, do nothing */
		if (mutex == null) {
			return;
		}

		/* Check atomically if this is the first successful retrieval */
		if (mutex.isFirst()) {
			/* This process is the winner: interrupt all others */
			mutex.interruptOthers(this);
		} else {
			/* This process is the loser: another process already succeeded */
			
			/*
			 * Clear the interrupt flag: the thread may have been interrupted by
			 * interruptOthers(), and we need to proceed with re-insertion if needed
			 */
			Thread.interrupted();

			if (isRemoval) {
				/*
				 * Re-insert the tuple by spawning a new process that performs an
				 * out operation at self (local tuple space)
				 */
				KlavaProcess reinsertProcess = new KlavaProcess() {
					private static final long serialVersionUID = 1L;

					@Override
					public void executeProcess() throws KlavaException {
						out(retrievedTuple, self);
					}
				};
				eval(reinsertProcess, self);
			}

			/* Terminate this process by throwing an exception */
			throw new KlavaException("OR: another branch succeeded");
		}
	}

	@Override
	protected void in(Tuple tuple, Locality destination) throws KlavaException {
		super.in(tuple, destination);
		handleOrRetrieval(tuple, destination, true);
	}

	@Override
	protected void read(Tuple tuple, Locality destination)
			throws KlavaException {
		super.read(tuple, destination);
		handleOrRetrieval(tuple, destination, false);
	}

	@Override
	protected boolean in_nb(Tuple tuple, Locality destination)
			throws KlavaException {
		boolean result = super.in_nb(tuple, destination);
		if (result) {
			handleOrRetrieval(tuple, destination, true);
		}
		return result;
	}

	@Override
	protected boolean read_nb(Tuple tuple, Locality destination)
			throws KlavaException {
		boolean result = super.read_nb(tuple, destination);
		if (result) {
			handleOrRetrieval(tuple, destination, false);
		}
		return result;
	}

	@Override
	protected boolean in_t(Tuple tuple, Locality destination, long timeout)
			throws KlavaException {
		boolean result = super.in_t(tuple, destination, timeout);
		if (result) {
			handleOrRetrieval(tuple, destination, true);
		}
		return result;
	}

	@Override
	protected boolean read_t(Tuple tuple, Locality destination, long timeout)
			throws KlavaException {
		boolean result = super.read_t(tuple, destination, timeout);
		if (result) {
			handleOrRetrieval(tuple, destination, false);
		}
		return result;
	}
}
