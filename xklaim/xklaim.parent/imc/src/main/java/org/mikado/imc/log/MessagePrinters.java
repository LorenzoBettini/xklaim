/*
 * Created on Feb 9, 2006
 */
package org.mikado.imc.log;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A collection of MessagePrinter's. When Print is called on this object, this
 * is delegated to each message printer.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class MessagePrinters implements MessagePrinter {
    protected Vector<MessagePrinter> messagePrinters = new Vector<MessagePrinter>();

    /**
     * Adds a MessagePrinter to the collection.
     * 
     * @param messagePrinter
     */
    public synchronized void addMessagePrinter(MessagePrinter messagePrinter) {
        messagePrinters.addElement(messagePrinter);
    }

    /**
     * Calls print method on each stored MessagePrinter
     * 
     * @see org.mikado.imc.log.MessagePrinter#Print(java.lang.String)
     */
    public synchronized void Print(String s) {
        Enumeration<MessagePrinter> printers = messagePrinters.elements();

        while (printers.hasMoreElements())
            printers.nextElement().Print(s);
    }

}
