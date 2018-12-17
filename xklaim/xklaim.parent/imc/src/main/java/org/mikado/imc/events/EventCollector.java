/*
 * Created on Mar 29, 2006
 */
/**
 * 
 */
package org.mikado.imc.events;

import java.util.Vector;

/**
 * Collects all the events of a specific type
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class EventCollector extends EventCounter {

    protected Vector<Event> collectedEvents = new Vector<Event>();

    /**
     * @see org.mikado.imc.events.EventCounter#notify(org.mikado.imc.events.Event)
     */
    @Override
    public synchronized void notify(Event event) {
        collectedEvents.addElement(event);
        super.notify(event);
    }

    /**
     * @return Returns the collectedEvents.
     */
    public Vector<Event> getCollectedEvents() {
        return collectedEvents;
    }

}
