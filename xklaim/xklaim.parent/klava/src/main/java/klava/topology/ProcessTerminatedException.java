/*
 * Created on Feb 7, 2006
 */
package klava.topology;

/**
 * A RuntimeException indicating that the process has terminated
 * its execution (e.g., after migration to a remote site, the
 * local instance of the process is terminated)
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ProcessTerminatedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public ProcessTerminatedException() {
    }

    /**
     * @param message
     */
    public ProcessTerminatedException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ProcessTerminatedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ProcessTerminatedException(Throwable cause) {
        super(cause);
    }

}
