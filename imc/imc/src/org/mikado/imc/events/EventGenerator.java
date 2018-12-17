/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

/**
 * Interface that should be implemented by those classes that can generate
 * events.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public interface EventGenerator {
    /**
     * Used by other objects to set the EventManager.
     *
     * @param eventManager
     */
    void setEventManager(EventManager eventManager);
    
    /**
     * @return The EventManager used by this EventGenerator
     */
    EventManager getEventManager();
}
