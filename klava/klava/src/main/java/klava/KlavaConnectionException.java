/*
 * Created on Nov 21, 2005
 */
package klava;

/**
 * Exception dealing with an error during a login or subscribe operation
 * 
 * @author Lorenzo Bettini
 */
public class KlavaConnectionException extends KlavaException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public KlavaConnectionException() {
        super();
    }

    /**
     * @param t
     */
    public KlavaConnectionException(Throwable t) {
        super(t);
    }

    /**
     * @param s
     */
    public KlavaConnectionException(String s) {
        super(s);
    }

}
