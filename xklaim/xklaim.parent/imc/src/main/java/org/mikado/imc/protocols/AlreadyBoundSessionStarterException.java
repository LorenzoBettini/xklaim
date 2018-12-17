/**
 * created: Jan 27, 2006
 */
package org.mikado.imc.protocols;

/**
 * Specialized ProtocolException representing an attempt of binding a
 * SessionStarter that's already bound.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class AlreadyBoundSessionStarterException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param s
     */
    public AlreadyBoundSessionStarterException(String s) {
        super(s);
    }

    /**
     * @param exception
     */
    public AlreadyBoundSessionStarterException(Exception exception) {
        super(exception);
    }

}
