/*
 * Created on Jan 14, 2005
 *
 */
package org.mikado.imc.events;

/**
 * The interface that all the event listeners should implement. The main method
 * is notify, that is invoked on a listener when an event, for which the
 * listener had previously registered, takes place.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface EventListener {
    /**
     * Invoked on a listener when an event, for which the listener had
     * previously registered, takes place.
     *
     * @param event
     */
    void notify(Event event);
}
