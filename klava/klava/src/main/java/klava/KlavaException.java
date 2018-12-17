/*
    KlavaException
    Base class of evety klava packet exceptions
*/

package klava ;

public class KlavaException extends Exception implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2954589010559418658L;
    public KlavaException() { super() ; }
    public KlavaException(Throwable t) { super(t); }
    public KlavaException( String s ) { super(s) ; }
}
