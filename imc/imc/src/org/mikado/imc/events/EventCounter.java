/*
 * Created on Mar 29, 2006
 */
/**
 * 
 */
package org.mikado.imc.events;

/**
 * Counts the number of events received.  It can also be
 * used to wait for a specific number of events.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EventCounter implements EventListener {

    protected int eventNumber = 0;

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public synchronized void notify(Event event) {
        ++eventNumber;
        notifyAll();
    }

    /**
     * wait for the number of events to reach the desired number.
     * 
     * @param numOfEvents
     * @throws InterruptedException
     */
    synchronized public void waitForEventNumber(int numOfEvents)
            throws InterruptedException {
        while (eventNumber < numOfEvents)
            wait();
    }
    
    /**
     * wait for the number of events to reach the desired number
     * within a specified timeout (in millisecs).
     * 
     * @param numOfEvents
     * @throws InterruptedException
     */
    synchronized public void waitForEventNumber(int numOfEvents, long timeOut)
            throws InterruptedException {
        while (eventNumber < numOfEvents) {
            wait(timeOut);
        }
    }

    /**
     * @return Returns the eventNumber.
     */
    public int getEventNumber() {
        return eventNumber;
    }

    /**
     * @param eventNumber
     */
    public EventCounter(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public EventCounter() {
        
    }
}
