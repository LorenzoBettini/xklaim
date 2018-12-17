/*
 * Created on Dec 5, 2005
 */
package org.mikado.imc.events;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Blocks until an Event is generated, by calling the method
 * <tt>waitForEvent</tt>. When Event is generated this method returns it.
 * This class is similar to WaitForEventSingleListener, but it stores the
 * received events in a queue (the events are removed from the queue each time
 * <tt>waitForEvent</tt> returns an event). The size of the queue is decided
 * by the user. If the queue is full the received event is simply discarded.
 * 
 * The user must decide a size that somehow ensures that events are not lost and
 * not too much memory is wasted.
 * 
 * If the size is not specified, then the queue has no bound (actually it is
 * {@link java.lang.Integer#MAX_VALUE java.lang.Integer.MAX_VALUE}).
 * 
 * @see org.mikado.imc.events.WaitForEventSingleListener
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class WaitForEventQueueListener implements WaitForEventListener {
    /**
     * The queue where the events are stored.
     */
    protected LinkedBlockingQueue<Event> queue;

    /**
     * Uses a queue with not size bounds.
     */
    public WaitForEventQueueListener() {
        queue = new LinkedBlockingQueue<Event>();
    }

    /**
     * Uses a queue with the specified size.
     * 
     * @param size
     */
    public WaitForEventQueueListener(int size) {
        queue = new LinkedBlockingQueue<Event>(size);
    }

    /**
     * 
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        queue.offer(event);
    }

    public Event waitForEvent() throws InterruptedException {
        return queue.take();
    }

}
