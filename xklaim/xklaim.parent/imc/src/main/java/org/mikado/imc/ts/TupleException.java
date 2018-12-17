/*
 * Created on Mar 28, 2007
 */
/**
 * 
 */
package org.mikado.imc.ts;

import org.mikado.imc.common.IMCException;

/**
 * Exception for Tuples
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleException extends IMCException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TupleException() {
        
    }

    /**
     * @param detail
     */
    public TupleException(String detail) {
        super(detail);
    }

    /**
     * @param detail
     * @param cause
     */
    public TupleException(String detail, Throwable cause) {
        super(detail, cause);
    }

    /**
     * @param cause
     */
    public TupleException(Throwable cause) {
        super(cause);
    }

}
