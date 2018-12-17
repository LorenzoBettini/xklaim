/*
 * Created on 15-gen-2005
 */
package org.mikado.imc.gui;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.ProcessEvent;

/**
 * Stores all the processes currently running into a List graphical object. It
 * is an event listener so it is able to update itself when receiving a
 * ProcessEvent.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class ProcessListPanel extends PanelWithTitle implements EventListener {
    private static final long serialVersionUID = 3258134669403697463L;

    public DefaultListPanel processList;

    /**
     * This method initializes
     * 
     */
    public ProcessListPanel() {
        super("Processes");
        initialize();
    }
    
    /**
     * @param eventManager
     */
    public ProcessListPanel(EventManager eventManager) {
        super("Processes", eventManager);
        initialize();
        setEventManager(eventManager);
    }

    /**
     * @param title
     */
    public ProcessListPanel(String title) {
        super(title);
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        processList = new DefaultListPanel();

        addMainPanel(processList);
    }

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        if (!(event instanceof ProcessEvent))
            return;

        ProcessEvent processEvent = (ProcessEvent) event;

        if (processEvent.eventType == ProcessEvent.EventType.ADDED) {
            processList.addElement(processEvent.element.getName());
        } else if (processEvent.eventType == ProcessEvent.EventType.REMOVED) {
            processList.removeElement(processEvent.element.getName());
        }
    }

    /**
     * @see org.mikado.imc.gui.PanelWithTitle#setEventManager(org.mikado.imc.events.EventManager)
     */
    @Override
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);
        eventManager.addListener(ProcessEvent.ProcessEventId, this);
    }
}
