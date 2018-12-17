/**
 * Created on Sep 17, 2005
 */
package org.mikado.imc.protocols;

/**
 * A specialized subclass dealing with errors: "expecting ... received ..."
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 */
public class WrongStringProtocolException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param expected
     * @param received
     */
    public WrongStringProtocolException(String expected, String received) {
        super(format(expected, received));
    }

    /**
     * Formats an error string, showing what was expected and what was received
     * instead.
     * 
     * @param expected
     * @param received
     * @return the formatted error string
     */
    public static String format(String expected, String received) {
        return "expecting " + expected + " received " + received;
    }
}
