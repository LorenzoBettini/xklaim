/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

/**
 * An adapter class implementing the EventGenerator interface. It stores an
 * EventManager and delegates the generate method.  This class can be
 * inherited by those class that must generate events and do not inherit from
 * existing classes.  If only Java provided multiple inheritance ;-)
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EventGeneratorAdapter implements EventGenerator {
    /** The wrapped EventManager. */
    protected EventManager eventManager;

    /**
     * Sets the EventManager.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Generates an event using the EventManager (if it is not null).
     *
     * @param event_id
     * @param event
     */
    public void generate(String event_id, Event event) {
        if (eventManager == null) {
            return;
        }

        eventManager.generate(event_id, event);
    }

    /**
     * @return Returns the eventManager.
     */
    public EventManager getEventManager() {
        return eventManager;
    }
}
