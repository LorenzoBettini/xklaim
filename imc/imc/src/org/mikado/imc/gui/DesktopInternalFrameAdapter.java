/*
 * Created on 10-apr-2006
 */
/**
 * 
 */
package org.mikado.imc.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

/**
 * Given a JFrame this internal frame will embed the contents of the JFrame so
 * that it can be inserted in a JDesktopPane.
 * 
 * It also delegates closing events to the original frame.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class DesktopInternalFrameAdapter extends DesktopInternalFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected JFrame embeddedFrame;

    private JMenuItem jAttachDetachMenu = null;

    /**
     * @param embeddedFrame
     */
    public DesktopInternalFrameAdapter(JFrame embeddedFrame) {
        this.embeddedFrame = embeddedFrame;

        initialize();
    }

    private void initialize() {
        this
                .addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
                    public void internalFrameClosing(
                            javax.swing.event.InternalFrameEvent e) {
                        /*
                         * propagates the window closing event to all the
                         * listeners of the embedded frame
                         */

                        WindowListener[] windowListeners = embeddedFrame
                                .getWindowListeners();
                        for (int i = 0; i < windowListeners.length; ++i) {
                            windowListeners[i].windowClosing(new WindowEvent(
                                    embeddedFrame, WindowEvent.WINDOW_CLOSING));
                        }
                        
                        /* and disposes this very internal frame */
                        dispose();
                    }
                });

        stealPanes();

        if (embeddedFrame.getJMenuBar() != null)
            embeddedFrame.getJMenuBar().add(getJAttachDetachMenu());
    }

    /**
     * @return Returns the embeddedFrame.
     */
    public JFrame getEmbeddedFrame() {
        return embeddedFrame;
    }

    protected void stealPanes() {
        setContentPane(embeddedFrame.getContentPane());
        setJMenuBar(embeddedFrame.getJMenuBar());
        embeddedFrame.setVisible(false);
    }

    protected void reattachPanes() {
        stealPanes();
        setVisible(true);
    }

    /**
     * This method initializes jMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenuItem getJAttachDetachMenu() {
        if (jAttachDetachMenu == null) {
            jAttachDetachMenu = new JMenuItem();
            jAttachDetachMenu.setText("detach");
            jAttachDetachMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (jAttachDetachMenu.getText().equals("detach")) {
                        givePanesBack();
                        jAttachDetachMenu.setText("attach");
                    } else {
                        reattachPanes();
                        jAttachDetachMenu.setText("detach");
                    }
                }
            });
        }
        return jAttachDetachMenu;
    }

    private void givePanesBack() {
        embeddedFrame.setContentPane(getContentPane());
        embeddedFrame.setJMenuBar(getJMenuBar());
        embeddedFrame.setVisible(true);
        setVisible(false);
    }
}
