/*
 * Created on 15-gen-2005
 */
package org.mikado.imc.gui;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.SessionEvent;
import org.mikado.imc.events.SessionEvent.SessionEventType;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.topology.SessionManager;

/**
 * Stores all the sessions into a List graphical object. It is an event listener
 * so it is able to update itself when receiving a SessionEvent.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class SessionListPanel extends PanelWithTitle implements EventListener {
    private static final long serialVersionUID = 3258134669403697463L;

    public DefaultListPanel sessionList;

    /**
     * @param eventManager
     *            Used to register this instance as a listener for session
     *            events.
     */
    public SessionListPanel(EventManager eventManager) {
        super("Sessions", eventManager);
        initialize();
        setEventManager(eventManager);
    }

    /**
     * @param title
     * @param eventManager
     *            Used to register this instance as a listener for session
     *            events.
     */
    public SessionListPanel(String title, EventManager eventManager) {
        super(title, eventManager);
        initialize();
        setEventManager(eventManager);
    }

    public SessionListPanel(String title) {
        super(title);
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        sessionList = new DefaultListPanel();

        addMainPanel(sessionList);

        sessionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int selected = sessionList.getSelectedIndex();

                    if (selected < 0)
                        return;

                    Session sessionId = (Session) sessionList
                            .getElementAt(selected);

                    SessionDetails connectionDetails = new SessionDetails(
                            sessionId.toString(), sessionId);
                    connectionDetails.pack();
                    connectionDetails.setVisible(true);
                }
            }
        });

    }

    /**
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        if (!(event instanceof SessionEvent))
            return;

        SessionEvent connectionEvent = (SessionEvent) event;

        Session session = connectionEvent.getSession();
        if (connectionEvent.type == SessionEventType.CONNECTION) {
            if (!sessionList.contains(session)) {
                sessionList.addElement(session);
            }
        } else if (connectionEvent.type == SessionEventType.DISCONNECTION) {
            if (sessionList.contains(session)) {
                sessionList.removeElement(session);
            }
        } else {
            return;
        }
    }

    /**
     * @see org.mikado.imc.gui.PanelWithTitle#setEventManager(org.mikado.imc.events.EventManager)
     */
    @Override
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);
        eventManager.addListener(SessionManager.EventClass, this);
    }

}
