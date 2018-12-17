/*
 * Created on Apr 10, 2006
 */
/**
 * 
 */
package org.mikado.imc.gui;

import javax.swing.JInternalFrame;

/**
 * This is used by DesktopFrame to build an internal frame when requested to add
 * a component to the desktop
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class DesktopInternalFrame extends JInternalFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static int openFrameCount = 0;

    static final int xOffset = 30, yOffset = 30;

    public DesktopInternalFrame() {
        super("Frame #" + (++openFrameCount), true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable

        setSize(300, 300);

        // Set the window's location.
        setLocation(xOffset * openFrameCount, yOffset * openFrameCount);
    }

}
