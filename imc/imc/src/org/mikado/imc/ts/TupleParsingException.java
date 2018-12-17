/*
 * Created on Mar 28, 2007
 */
/**
 * 
 */
package org.mikado.imc.ts;

/**
 * Exception describing a problem during the parsing of a tuple expression
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleParsingException extends TupleException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TupleParsingException() {
    }

    /**
     * @param detail
     */
    public TupleParsingException(String detail) {
        super(detail);
    }

    /**
     * @param detail
     * @param cause
     */
    public TupleParsingException(String detail, Throwable cause) {
        super(detail, cause);
    }

    /**
     * @param cause
     */
    public TupleParsingException(Throwable cause) {
        super(cause);
    }

}
