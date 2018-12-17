/*
 * Created on Feb 27, 2006
 */
package org.mikado.imc.gui;

import javax.swing.JPanel;

import org.mikado.imc.topology.Node;

import java.awt.GridLayout;

/**
 * Shows two panels: one for establishing a Session and one for accepting a
 * Session.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class AcceptEstablishSessionPanel extends JPanel {
    private EstablishSessionPanel establishSessionPanel;

    private AcceptSessionPanel acceptSessionPanel;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param establishSessionPanel
     * @param acceptSessionPanel
     */
    public AcceptEstablishSessionPanel(
            EstablishSessionPanel establishSessionPanel,
            AcceptSessionPanel acceptSessionPanel) {
        this.establishSessionPanel = establishSessionPanel;
        this.acceptSessionPanel = acceptSessionPanel;
        initialize();
    }

    /**
     * This is the default constructor
     */
    public AcceptEstablishSessionPanel(Node node) {
        super();
        establishSessionPanel = new EstablishSessionPanel(node);
        acceptSessionPanel = new AcceptSessionPanel(node);
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setRows(2);
        this.setLayout(gridLayout);
        this.setSize(300, 200);
        this.add(establishSessionPanel);
        this.add(acceptSessionPanel);
    }

    /**
     * @return Returns the establishSessionPanel.
     */
    public EstablishSessionPanel getEstablishSessionPanel() {
        return establishSessionPanel;
    }

    /**
     * @param establishSessionPanel
     *            The establishSessionPanel to set.
     */
    public void setEstablishSessionPanel(
            EstablishSessionPanel establishSessionPanel) {
        remove(this.establishSessionPanel);
        this.establishSessionPanel = establishSessionPanel;
        add(this.establishSessionPanel);
    }

    /**
     * @return Returns the acceptSessionPanel.
     */
    public AcceptSessionPanel getAcceptSessionPanel() {
        return acceptSessionPanel;
    }

    /**
     * @param acceptSessionPanel
     *            The acceptSessionPanel to set.
     */
    public void setAcceptSessionPanel(AcceptSessionPanel acceptSessionPanel) {
        remove(this.acceptSessionPanel);
        this.acceptSessionPanel = acceptSessionPanel;
        add(this.acceptSessionPanel);
    }

}
