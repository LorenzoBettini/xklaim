/*
 * Created on Mar 28, 2007
 */
/**
 * 
 */
package org.mikado.imc.common;

/**
 * Generic exception concerning a timeout.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TimeoutException extends IMCException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TimeoutException() {
    }

    /**
     * @param detail
     */
    public TimeoutException(String detail) {
        super(detail);
    }

    /**
     * @param detail
     * @param cause
     */
    public TimeoutException(String detail, Throwable cause) {
        super(detail, cause);
    }

    /**
     * @param cause
     */
    public TimeoutException(Throwable cause) {
        super(cause);
    }

}
