/*
 * Created on Mar 29, 2006
 */
/**
 * 
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.topology.Closeable;

/**
 * A generic frame that contains something that is closed when the close button
 * is pressed.
 * 
 * @author bettini
 * @version $Revision: 1.3 $
 */
public class CloseableFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    /**
     * The element that is closed when the close button is pressed.
     */
    Closeable closeable = null;

    /**
     * This is the default constructor
     */
    public CloseableFrame() {
        initialize();
    }

    /**
     * This is the default constructor
     */
    public CloseableFrame(Closeable closeable) {
        this.closeable = closeable;
        initialize();
    }
    
    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(300, 200);
        this.setContentPane(getJContentPane());
        this.setTitle("Frame");
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    if (closeable != null)
                        closeable.close();
                    dispose();
                } catch (IMCException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(null, e1.getClass().getName()
                            + "\n" + e1.getMessage(), "alert",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
        }
        return jContentPane;
    }

    /**
     * @return Returns the closeable.
     */
    public Closeable getCloseable() {
        return closeable;
    }

    /**
     * @param closeable The closeable to set.
     */
    public void setCloseable(Closeable closeable) {
        this.closeable = closeable;
    }

}
