/*
 * Created on Mar 3, 2006
 */
package org.mikado.imc.protocols;

/**
 * An exception due to a malformed SessionId
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class SessionIdException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param s
     */
    public SessionIdException(String s) {
        super(s);
    }

    /**
     * @param exception
     */
    public SessionIdException(Exception exception) {
        super(exception);
    }

}
