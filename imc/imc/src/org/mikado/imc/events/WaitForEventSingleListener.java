/*
 * Created on Nov 23, 2005
 */
package org.mikado.imc.events;

/**
 * Blocks until an Event is generated, by calling the method
 * <tt>waitForEvent</tt>. When Event is generated this method returns it.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class WaitForEventSingleListener implements WaitForEventListener {
    /**
     * The Event that will be returned by <tt>waitForEvent</tt>
     */
    private Event event = null;

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public synchronized void notify(Event event) {
        this.event = event;
        notifyAll();
    }

    /**
     * @see org.mikado.imc.events.WaitForEventListener#waitForEvent()
     */
    public synchronized Event waitForEvent() throws InterruptedException {
        while (event == null)
            wait();

        Event toReturn = event;
        event = null;
        
        return toReturn;
    }
}
