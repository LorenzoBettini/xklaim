package klava.topology;

/**
 * Exception thrown by {@link KlavaNodeCoordinator#done()} to signal normal completion
 * of a node's main process. This is caught by {@link KlavaNodeCoordinator#execute()}
 * and treated as a clean exit, not an error.
 * 
 * @author Lorenzo Bettini
 */
public class KlavaNodeDoneException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
