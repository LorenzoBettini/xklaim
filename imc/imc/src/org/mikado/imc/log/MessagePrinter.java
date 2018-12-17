package org.mikado.imc.log;

/**
 * Class providing method(s) for printing a message somewhere. It can be
 * specialized in order to print to the screen or to a file.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public interface MessagePrinter {
    /**
     * Prints a message to the associated device.
     * 
     * @param s
     *            the string to print
     */
    public void Print(String s);
}
