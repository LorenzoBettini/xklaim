/*
 * Created on Dec 20, 2005
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.SessionId;

/**
 * Specifies that we can reach a specific destination.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NoRouteException extends IMCException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param detail
     */
    public NoRouteException(String detail) {
        super(detail);
    }
    
    /**
     * @param sessionId
     */
    public NoRouteException(SessionId sessionId) {
        super(sessionId.toString());
    }

    /**
     * @param detail
     * @param cause
     */
    public NoRouteException(String detail, Throwable cause) {
        super(detail, cause);
    }

}
