/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * The manager of the events.  One can register an EventListener for a specific
 * Event.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EventManager {
    /** The table storing the listeners for specific events. */
    protected Hashtable<String,LinkedList<EventListener>> events = 
    	new Hashtable<String,LinkedList<EventListener>>();

    /**
     * Creates a new EventManager object.
     */
    public EventManager() {
    }
    
    public synchronized void addListener(String event, EventListener eventListener) {
        LinkedList<EventListener> listeners = events.get(event);
        
        if (listeners == null) {
            listeners = new LinkedList<EventListener>();
            events.put(event, listeners);
        }
        
        listeners.add(eventListener);
    }
    
    public synchronized void removeListener(String event, EventListener eventListener) {
        LinkedList<EventListener> listeners = events.get(event);
        
        if (listeners == null) {
            return;
        }
        
        listeners.remove(eventListener);
    }
    
    public synchronized void generate(String event_id, Event event) {
        LinkedList<EventListener> listeners = events.get(event_id);
        
        if (listeners == null)
            return;
        
        Iterator<EventListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().notify(event);
        }
    }
}
