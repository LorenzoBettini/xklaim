/**
 * created: Jan 27, 2006
 */
package org.mikado.imc.protocols;

/**
 * Specialized ProtocolException representing an attempt of binding a
 * SessionId that's already in use.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class SessionIdBindException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param s
     */
    public SessionIdBindException(String s) {
        super(s);
    }

    /**
     * @param s
     */
    public SessionIdBindException(SessionId s) {
        super("session id: " + s);
    }

    /**
     * @param exception
     */
    public SessionIdBindException(Exception exception) {
        super(exception);
    }

}
