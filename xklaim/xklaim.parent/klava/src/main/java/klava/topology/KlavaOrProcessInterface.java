package klava.topology;

/**
 * Interface implemented by processes to allow the OR operator.
 * 
 * @author Lorenzo Bettini
 */
public interface KlavaOrProcessInterface {

    /**
     * Sets the shared OR mutex. Called by {@link KlavaProcess#or(java.util.List)}
     * before the process is started.
     *
     * @param orMutex the mutex shared with the other OR processes
     */
    void setOrMutex(KlavaOrMutex orMutex);

    /**
     * @see Thread#interrupt()
     */
    void interrupt();

}