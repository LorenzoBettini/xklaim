/*
 * Created on Feb 21, 2006
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;

import org.mikado.imc.events.EventManager;
import java.awt.FlowLayout;

/**
 * A generic panel which also contains a title (in the north part).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class PanelWithTitle extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JLabel jLabel = null;

    private String title = "";

    private JPanel northPanel = null;

    private JCheckBox visibleCheckBox = null;

    private JPanel mainPanel = null;

    /**
     * This is used for registering for listening to events.
     */
    protected EventManager eventManager = null;

    /**
     * @param title
     * @param eventManager
     */
    public PanelWithTitle(String title, EventManager eventManager) {
        super();
        this.title = title;
        this.eventManager = eventManager;
        initialize();
    }

    /**
     * @param eventManager
     *            used for registering for listening to events.
     */
    public PanelWithTitle(EventManager eventManager) {
        super();
        this.eventManager = eventManager;
        initialize();
    }

    /**
     * 
     */
    public PanelWithTitle() {
        super();

        initialize();
    }

    /**
     * 
     */
    public PanelWithTitle(String title) {
        this.title = title;

        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJPanel(), java.awt.BorderLayout.NORTH);
        this.add(getMainPanel(), java.awt.BorderLayout.CENTER);
    }

    /**
     * Adds the panel as the center element of this panel.
     * 
     * @param panel
     */
    public void addMainPanel(JPanel panel) {
        getMainPanel().add(panel, java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (northPanel == null) {
            jLabel = new JLabel();
            jLabel.setText(title);
            northPanel = new JPanel();
            northPanel.setLayout(new FlowLayout());
            northPanel.add(jLabel, null);
            northPanel.add(getVisibleCheckBox(), null);
        }
        return northPanel;
    }

    /**
     * This method initializes jCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getVisibleCheckBox() {
        if (visibleCheckBox == null) {
            visibleCheckBox = new JCheckBox();
            visibleCheckBox.setVisible(false); // by default not shown
            visibleCheckBox.setText("visible");
            visibleCheckBox.setSelected(true);
            visibleCheckBox
                    .setToolTipText("whether the panel should be visible or not");
            visibleCheckBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        /* hide the panel */
                        getMainPanel().setVisible(false);
                    } else {
                        /* make it visible again */
                        getMainPanel().setVisible(true);
                    }
                }
            });
        }
        return visibleCheckBox;
    }

    /**
     * This method initializes the main panel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
        }
        return mainPanel;
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
     * Shows the exception in a message box
     * 
     * @param e
     * @throws HeadlessException
     */
    protected void showException(Exception e) throws HeadlessException {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getClass().getName() + "\n"
                + e.getMessage(), "alert", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
        jLabel.setText(title);
    }

    /**
     * Adds the specified component to the north part of the panel.
     * 
     * @param component
     *            The component to add to the north part of the panel.
     */
    public void addComponent(Component component) {
        getJPanel().add(component);
    }
}
