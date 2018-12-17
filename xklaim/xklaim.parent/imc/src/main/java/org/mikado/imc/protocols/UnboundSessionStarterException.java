/**
 * created: Jan 27, 2006
 */
package org.mikado.imc.protocols;

/**
 * Specialized ProtocolException representing an attempt of using a
 * SessionStarter that's not bound yet.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class UnboundSessionStarterException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param s
     */
    public UnboundSessionStarterException(String s) {
        super(s);
    }

    /**
     * @param exception
     */
    public UnboundSessionStarterException(Exception exception) {
        super(exception);
    }

}
