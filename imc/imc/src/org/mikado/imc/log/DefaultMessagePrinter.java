package org.mikado.imc.log;

/**
 * A MessagePrinter specialized to simply print on the screen
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class DefaultMessagePrinter implements MessagePrinter {
    /**
     * If specified, each printed line is prefixed with it.
     */
    String name = "";

    /**
     * Creates a new DefaultMessagePrinter object.
     */
    public DefaultMessagePrinter() {

    }

    /**
     * Creates a new DefaultMessagePrinter object.
     * 
     * @param s
     *            name for this message printer
     */
    public DefaultMessagePrinter(String s) {
        name = s;
    }

    /**
     * print the message on the screen
     * 
     * @param s
     *            the message to print
     */
    public void Print(String s) {
        System.out.println((name.length() > 0 ? name + ": " : "") + s);
    }
}
