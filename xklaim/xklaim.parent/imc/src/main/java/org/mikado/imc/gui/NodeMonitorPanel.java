/*
 * Created on Feb 23, 2006
 */
package org.mikado.imc.gui;

import javax.swing.JPanel;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.topology.Node;

import java.awt.GridLayout;

/**
 * A panel showing sessions and processes of a Node (it can also be
 * added further monitoring panels).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class NodeMonitorPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private SessionListPanel sessionListPanel = null;
    private ProcessListPanel processListPanel = null;
    private GridLayout gridLayout;

    /**
     * Uses the passed node to retrieve the EventManager and thus
     * sets the event manager in all the listeners.
     * 
     * @param node
     */
    public NodeMonitorPanel(Node node) {
        super();
        initialize();
        EventManager eventManager = node.getEventManager();
        sessionListPanel.setEventManager(eventManager);
        processListPanel.setEventManager(eventManager);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        gridLayout = new GridLayout();
        gridLayout.setRows(1);
        gridLayout.setColumns(2);
        this.setLayout(gridLayout);
        this.setSize(300, 200);
        this.add(getSessionListPanel(), null);
        this.add(getProcessListPanel(), null);
    }

    /**
     * This method initializes sessionListPanel	
     * 	
     * @return org.mikado.imc.gui.SessionListPanel	
     */
    private SessionListPanel getSessionListPanel() {
        if (sessionListPanel == null) {
            sessionListPanel = new SessionListPanel("Sessions");
        }
        return sessionListPanel;
    }

    /**
     * This method initializes processListPanel	
     * 	
     * @return org.mikado.imc.gui.ProcessListPanel	
     */
    private ProcessListPanel getProcessListPanel() {
        if (processListPanel == null) {
            processListPanel = new ProcessListPanel();
        }
        return processListPanel;
    }

    /**
     * Adds a panel to the grid.
     * 
     * @param panel
     */
    public void addPanel(JPanel panel) {
        gridLayout.setColumns(gridLayout.getColumns()+1);
        this.add(panel);
    }
}
