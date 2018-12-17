/*
 * Created on Apr 5, 2006
 */
/**
 * 
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventGenerator;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;

/**
 * A system of nodes.
 * 
 * It provides means to add a node to an application, remove a node from the
 * application, and deal with common listeners and event manager.
 * 
 * When the application is closed, all the associated nodes will be close.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class Application implements EventGenerator {
    protected String appName;

    NodeContainer nodeContainer = new NodeContainer();

    EventManager eventManager = new EventManager();

    /**
     * @param appName
     */
    public Application(String appName) {
        this.appName = appName;
        initApplication();
    }

    /**
     * 
     */
    public Application() {
        appName = getClass().getName();
        initApplication();
    }

    /**
     * Performs the initial settings.
     */
    protected void initApplication() {
        nodeContainer.setEventManager(eventManager);
    }

    /**
     * Closes all the nodes of this application.
     * 
     * @throws IMCException
     */
    public void close() throws IMCException {
        nodeContainer.close();
    }

    /**
     * Adds a node to this application
     * 
     * @param node
     * @throws IMCException
     */
    public void addNode(Node node) throws IMCException {
        nodeContainer.addElement(node);
        node.setApplication(this);
    }

    /**
     * Removes a node from this application
     * 
     * @param node
     * @throws IMCException
     */
    public void removeNode(Node node) throws IMCException {
        nodeContainer.removeElement(node);
    }

    /**
     * @return Returns the eventManager.
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * @param eventManager
     *            The eventManager to set.
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * @see org.mikado.imc.events.EventManager#addListener(java.lang.String,
     *      org.mikado.imc.events.EventListener)
     */
    public void addListener(String event, EventListener eventListener) {
        eventManager.addListener(event, eventListener);
    }

    /**
     * @see org.mikado.imc.events.EventManager#removeListener(java.lang.String,
     *      org.mikado.imc.events.EventListener)
     */
    public void removeListener(String event, EventListener eventListener) {
        eventManager.removeListener(event, eventListener);
    }

}
