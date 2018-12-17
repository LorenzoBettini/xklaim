package org.mikado.imc.common;

/**
 * Base class for all IMC Exceptions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class IMCException extends Exception {
    private static final long serialVersionUID = 3257008756763342648L;

    public IMCException() {
        super("Unknown reason");
    }

    /**
     * Creates an <tt>IMCException</tt> with an attached detail message.
     * 
     * @param detail
     *            detailed message explaning why this exception was thrown.
     */
    public IMCException(String detail) {
        super(detail);
    }

    /**
     * Creates an <tt>IMCException</tt> with an attached detail message and
     * cause.
     * 
     * @param detail
     *            a detailed message explaning why this exception was thrown.
     * @param cause
     *            the cause for throwing this exception.
     */
    public IMCException(String detail, Throwable cause) {
        super(detail, cause);
    }

    /**
     * @param cause
     */
    public IMCException(Throwable cause) {
        super(cause);
    }
}
