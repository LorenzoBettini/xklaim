package klava;

/**
 * Remote processes are not accepted by this node
 */

public class KlavaRemoteProcessNotAllowed extends KlavaException {
    /**
     * 
     */
    private static final long serialVersionUID = 6277808691311875599L;

    public KlavaRemoteProcessNotAllowed(String s) {
        super(s);
    }
}