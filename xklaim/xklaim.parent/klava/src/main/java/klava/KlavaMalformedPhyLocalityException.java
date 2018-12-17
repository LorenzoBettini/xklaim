package klava;

/**
 * A physical locality must be IP:port
 */

public class KlavaMalformedPhyLocalityException extends KlavaException {

    /**
     * 
     */
    private static final long serialVersionUID = -9043999620308871178L;

    public KlavaMalformedPhyLocalityException() {
        super();
    }

    public KlavaMalformedPhyLocalityException(String s) {
        super(s);
    }
}