/*
 * Created on Dec 5, 2005
 */
package org.mikado.imc.events;

/**
 * Blocks until an Event is generated, by calling the method
 * <tt>waitForEvent</tt>. When Event is generated this method returns it.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface WaitForEventListener extends EventListener {

    /**
     * Blocks until an Event is generated. When Event is generated this method
     * returns it.
     * 
     * @return The generated event.
     * @throws InterruptedException
     */
    public abstract Event waitForEvent() throws InterruptedException;

}