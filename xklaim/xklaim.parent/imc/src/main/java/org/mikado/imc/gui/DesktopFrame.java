/*
 * Created on Apr 10, 2006
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mikado.imc.common.IMCException;

public class DesktopFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private StatusBar statusBar = null;

    private JMenuBar menuBar = null;

    private JMenu fileMenu = null;

    private JMenuItem quitMenuItem = null;

    private MDIDesktopPane jDesktopPane = null;

    private JMenu optionMenu = null;

    private JMenu viewMenu = null;

    private JScrollPane scrollPane;

    private WindowMenu windowMenu;

    /**
     * This method initializes fileMenu
     * 
     * @return javax.swing.JMenu
     */
    public JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setText("File");
            fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);
            fileMenu.add(getQuitMenuItem());
        }
        return fileMenu;
    }

    public DesktopFrame() throws HeadlessException {
        initialize();
    }

    public DesktopFrame(String title) throws HeadlessException {
        super(title);

        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(300, 200);
        this.setJMenuBar(getJJMenuBar());
        this.setContentPane(getJContentPane());
        this.setTitle("Desktop frame");
        setDefaultLookAndFeelDecorated(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    closeDesktop();
                } catch (IMCException e1) {
                    e1.printStackTrace();
                    showException(e1);
                }
            }
        });
    }

    /**
     * Shows the exception in a message box
     * 
     * @param e
     */
    public void showException(Exception e) {
        /*
         * JOptionPane.showMessageDialog(null, e.getClass().getName() + ":\n" +
         * e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
         */
        new ExceptionMessageBox(this, e).setVisible(true);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanelJPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getStatusBar(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes statusBar
     * 
     * @return org.mikado.imc.gui.StatusBar
     */
    public StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar();
        }
        return statusBar;
    }

    /**
     * This method initializes jJMenuBar
     * 
     * @return javax.swing.JMenuBar
     */
    public JMenuBar getJJMenuBar() {
        if (menuBar == null) {
            menuBar = new JMenuBar();
            menuBar.add(getFileMenu());
            menuBar.add(getOptionMenu());
            menuBar.add(getViewMenu());
            menuBar.add(getWindowMenu());
        }
        return menuBar;
    }

    /**
     * This is called when the frame is closed.
     * 
     * Subclasses can override this method to perform specific closing actions.
     * 
     * The default implementation does nothing.
     * 
     * @throws IMCException
     */
    protected void close() throws IMCException {

    }

    /**
     * Calls close and then dispose all the internal frames and this frame
     * itself. This is called when the frame is closed, or when "Quit" is
     * chosen.
     * 
     * @throws IMCException
     */
    protected void closeDesktop() throws IMCException {
        /*
         * first calls close, so that subclasses can perform their closing
         * operations.
         */
        close();

        /* disposes all the internal frames */
        JInternalFrame[] internalFrames = getJDesktopPane().getAllFrames();
        for (int i = 0; i < internalFrames.length; ++i) {
            internalFrames[i].dispose();
        }

        /* finally let's dispose ourselves */
        dispose();
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getQuitMenuItem() {
        if (quitMenuItem == null) {
            quitMenuItem = new JMenuItem();
            quitMenuItem.setText("Quit");
            quitMenuItem.setMnemonic(KeyEvent.VK_Q);
            quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        closeDesktop();
                    } catch (IMCException e1) {
                        e1.printStackTrace();
                        showException(e1);
                    }
                }
            });
        }
        return quitMenuItem;
    }

    /**
     * This method initializes jDesktopPane
     * 
     * @return javax.swing.JDesktopPane
     */
    public MDIDesktopPane getJDesktopPane() {
        if (jDesktopPane == null) {
            jDesktopPane = new MDIDesktopPane();
        }
        return jDesktopPane;
    }

    /**
     * This method initializes optionMenu
     * 
     * @return javax.swing.JMenu
     */
    protected JMenu getOptionMenu() {
        if (optionMenu == null) {
            optionMenu = new JMenu();
            optionMenu.setText("Options");
            optionMenu.setMnemonic(KeyEvent.VK_O);
        }
        return optionMenu;
    }

    /**
     * Adds the specified internal frame into the desktop area
     * 
     * @param internalFrame
     * @return a reference to the internal frame added to the desktop
     */
    public JInternalFrame addFrame(JInternalFrame internalFrame) {
        if (!internalFrame.isPreferredSizeSet())
            internalFrame.pack();
        getJDesktopPane().add(internalFrame);

        internalFrame.setVisible(true);
        return internalFrame;
    }

    /**
     * Adds the specified component into an (internal) frame and then adds the
     * frame into the desktop area
     * 
     * @param component
     * @return a reference to the internal frame added to the desktop
     */
    public JInternalFrame addFrame(Component component) {
        JInternalFrame internalFrame = new DesktopInternalFrame();
        internalFrame.add(component);
        return addFrame(internalFrame);
    }

    /**
     * Adds the specified frame into the desktop area after adapting it with an
     * internal frame.
     * 
     * @param frame
     * 
     * @return a reference to the internal frame added to the desktop
     */
    public JInternalFrame addFrame(JFrame frame) {
        JInternalFrame internalFrame = new DesktopInternalFrameAdapter(frame);
        return addFrame(internalFrame);
    }

    /**
     * Removes the passed internal frame from the desktop
     * 
     * @param internalFrame
     */
    public void removeFrame(JInternalFrame internalFrame) {
        remove(internalFrame);
    }

    /**
     * @see org.mikado.imc.gui.StatusBar#setMainMessage(java.lang.String)
     */
    public void setStatusBarMessage(String message) {
        statusBar.setMainMessage(message);
    }

    /**
     * This method initializes jMenu
     * 
     * @return javax.swing.JMenu
     */
    protected JMenu getViewMenu() {
        if (viewMenu == null) {
            viewMenu = new JMenu();
            viewMenu.setText("View");
            viewMenu.setMnemonic(java.awt.event.KeyEvent.VK_V);
        }
        return viewMenu;
    }

    /**
     * @return Returns the scrollPane.
     */
    protected JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.getViewport().add(getJDesktopPane());
        }

        return scrollPane;
    }

    /**
     * @return Returns the windowMenu.
     */
    protected WindowMenu getWindowMenu() {
        if (windowMenu == null) {
            windowMenu = new WindowMenu(getJDesktopPane());
        }

        return windowMenu;
    }

}
