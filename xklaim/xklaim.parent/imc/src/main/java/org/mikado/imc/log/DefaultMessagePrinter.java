package org.mikado.imc.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MessagePrinter specialized to simply print on the screen
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class DefaultMessagePrinter implements MessagePrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessagePrinter.class);

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
        LOGGER.atInfo().log(() -> (name.length() > 0 ? name + ": " : "") + s);
    }
}
