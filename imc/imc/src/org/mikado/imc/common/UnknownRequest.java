/*
 * Created on 1-feb-2005
 */
package org.mikado.imc.common;

/**
 * Specialized IMCException representing a bad request.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class UnknownRequest extends IMCException {
    private static final long serialVersionUID = 3258130275686035504L;

	/**
     * Creates a new UnknownRequest object.
     */
    public UnknownRequest() {
        super();
    }

    /**
     * Creates a new UnknownRequest object.
     *
     * @param detail  
     */
    public UnknownRequest(String detail) {
        super(detail);
    }

    /**
     * Creates a new UnknownRequest object.
     *
     * @param detail  
     * @param cause  
     */
    public UnknownRequest(String detail, Throwable cause) {
        super(detail, cause);
    }
}
