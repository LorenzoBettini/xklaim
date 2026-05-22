/*
    KlavaException
    Base class of evety klava packet exceptions
*/

package klava ;

public class KlavaException extends RuntimeException implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2954589010559418658L;
    public KlavaException() { super() ; }
    public KlavaException(Throwable t) { super(t); }
    public KlavaException( String s ) { super(s) ; }

    public boolean wasCausedByInterruptedException() {
        return wasCausedByInterruptedException(this);
    }

    public static boolean wasCausedByInterruptedException(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof InterruptedException)
                return true;

            throwable = throwable.getCause();
        }

        return false;
    }
}
