/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.events;

/**
 * An event listener that simply stores all the events in a log (in the
 * shape of strings, each on a separate line).
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class LogEventListener implements EventListener {
    private StringBuffer buffer = new StringBuffer();

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        buffer.append(event.toString() + "\n");
    }

    /**
     * Resets the log and returns the current contents.
     *
     * @return The contents of the log before resetting.
     */
    public String resetBuffer() {
        String current = buffer.toString();
        buffer = new StringBuffer();

        return current;
    }

    /**
     * Returns the contents of the log.
     *
     * @return The contents of the log.
     */
    public String toString() {
        return buffer.toString();
    }
}
