/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

/**
 * Base class for every imc events.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class Event {
    /** The source that generated the event. */
    protected Object source;

    /**
     * Creates a new Event object.
     */
    public Event() {
    }

    /**
     * Creates a new Event with an associated source.
     *
     * @param source
     */
    public Event(Object source) {
        this.source = source;
    }

    /**
     * Returns the source of the event.
     *
     * @return Returns the source.
     */
    public final Object getSource() {
        return source;
    }

    /**
     * Returns a string with information about the source of event.
     *
     * @return A string with information about the source of event.
     */
    public String toString() {
        return "Event from " + source.toString();
    }
}
