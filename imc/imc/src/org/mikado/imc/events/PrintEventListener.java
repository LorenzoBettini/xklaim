/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.events;

import java.io.PrintStream;

/**
 * An event listener that simply prints all the event using a PrintStream (by
 * default it's System.out).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class PrintEventListener implements EventListener {
    private PrintStream printStream;

    /**
     * @param printStream
     */
    public PrintEventListener(PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Uses System.out by default.
     */
    public PrintEventListener() {
        this(System.out);
    }

    /**
     * Prints the event using the associated print stream.
     * 
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        printStream.println(event.toString());
    }

}
