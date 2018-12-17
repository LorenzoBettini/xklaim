/*
 * Created on 10-dic-2004
 *
 */
package org.mikado.imc.log;


/**
 * @author Lorenzo Bettini
 * 
 * @version $Revision: 1.1 $
 * 
 * A MessagePrinter that stores string into a StringBuffer
 */
public class BufferMessagePrinter implements MessagePrinter {
    /**
     * The buffer where messages are stored.
     */
    private StringBuffer buffer = new StringBuffer();

    /**
     * print a message into the buffer
     * 
     * @param s
     *            the message to print
     */
    public void Print(String s) {
        buffer.append(s + "\n");
    }

    /**
     * Return the contents of the buffer up-to-now
     * 
     * @return the contents of the buffer
     */
    public String getString() {
        return buffer.toString();
    }
}
