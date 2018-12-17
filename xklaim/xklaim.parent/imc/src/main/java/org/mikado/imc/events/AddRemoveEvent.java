/*
 * Created on Feb 23, 2006
 */
package org.mikado.imc.events;

/**
 * A specialized event representing something that has been added or removed
 * (e.g., from a collection).
 * 
 * It is parameterized over the class of the object being added or removed.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class AddRemoveEvent<E> extends Event {
    public enum EventType {
        ADDED, REMOVED
    }

    /**
     * The type of event
     */
    public EventType eventType;

    /**
     * The element being added or removed
     */
    public E element;

    /**
     * @param source
     * @param eventType
     * @param element
     */
    public AddRemoveEvent(Object source, EventType eventType, E element) {
        super(source);
        this.eventType = eventType;
        this.element = element;
    }

    /**
     * @see org.mikado.imc.events.Event#toString()
     */
    @Override
    public String toString() {
        return eventType + ": " + element;
    }

}
