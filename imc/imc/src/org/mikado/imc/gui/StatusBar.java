/*
 * Created on Apr 10, 2006
 */
package org.mikado.imc.gui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * The generic status bar used in imc frames
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class StatusBar extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
	private JLabel mainMessageLabel = null;
	private JPanel additionalPanel = null;

    public StatusBar() {
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        mainMessageLabel = new JLabel();
        mainMessageLabel.setText(" ");
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setSize(300, 200);
        this.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        this.add(mainMessageLabel, null);
        this.add(Box.createGlue());
        this.add(getAdditionalPanel(), null);
    }

    /**
     * Shows the passed message in the main message label
     * 
     * @param message
     */
    public void setMainMessage(String message) {
        mainMessageLabel.setText(message);
    }

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAdditionalPanel() {
	    if (additionalPanel == null) {
	        additionalPanel = new JPanel();
	    }
	    return additionalPanel;
	}
    
    /**
     * Adds the passed component to the additional panel
     * 
     * @param component
     */
    public void addAdditionalPanel(Component component) {
        getAdditionalPanel().add(component);
    }
}
