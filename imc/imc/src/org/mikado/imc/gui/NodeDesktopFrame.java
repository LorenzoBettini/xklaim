/*
 * Created on Apr 11, 2006
 */
/**
 * 
 */
package org.mikado.imc.gui;

import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import org.mikado.imc.common.ClassEntry;
import org.mikado.imc.common.IMCException;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.NodeProcess;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Specialization of DesktopFrame for Nodes.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class NodeDesktopFrame extends DesktopFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The embedded node.
     */
    protected Node node;

    private JCheckBoxMenuItem viewSessionsMenuItem;

    private JCheckBoxMenuItem viewProcessesMenuItem = null;

    protected JInternalFrame sessionListFrame = null;

    protected SessionListPanel sessionListPanel = null;

    protected JInternalFrame processListFrame = null;

    protected ProcessListPanel processListPanel = null;

    private JMenu sessionsMenu = null;

    private JMenuItem acceptEstablishSessionMenuItem = null;

    private JMenuItem newProcessMenuItem = null;

    protected ClassChooserDialog processChooser;

    /**
     * @param node
     */
    public NodeDesktopFrame(Node node) {
        this.node = node;
        initialize();
    }

    /**
     * @param title
     * @param node
     * @throws HeadlessException
     */
    public NodeDesktopFrame(String title, Node node) {
        super(title);
        this.node = node;
        initialize();
    }

    private void initialize() {
        processChooser = new ClassChooserFilteredDialog(NodeProcess.class
                .getName(), this, "Choose a Process", true);

        this.setSize(620, 480);
        setTitle(node.getNodeName());

        processListPanel = new ProcessListPanel(node.getEventManager());
        sessionListPanel = new SessionListPanel(node.getEventManager());

        getViewMenu().add(getViewSessionsMenuItem());
        getViewMenu().add(getViewProcessesMenuItem());
        getJMenuBar().add(getSessionsMenu());
        getFileMenu().add(getNewProcessMenuItem(), 0);
    }

    /**
     * This method initializes viewSessionsMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getViewSessionsMenuItem() {
        if (viewSessionsMenuItem == null) {
            viewSessionsMenuItem = new JCheckBoxMenuItem();
            viewSessionsMenuItem.setText("Sessions");
            viewSessionsMenuItem.setMnemonic(KeyEvent.VK_S);
            viewSessionsMenuItem
                    .addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                if (sessionListFrame == null) {
                                    sessionListFrame = addFrame(sessionListPanel);
                                    sessionListFrame.setTitle("Sessions");
                                } else {
                                    sessionListFrame.setVisible(true);
                                }
                            } else {
                                if (sessionListFrame != null) {
                                    sessionListFrame.setVisible(false);
                                }
                            }
                        }
                    });
        }
        return viewSessionsMenuItem;
    }

    /**
     * This method initializes viewProcessesMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    private JCheckBoxMenuItem getViewProcessesMenuItem() {
        if (viewProcessesMenuItem == null) {
            viewProcessesMenuItem = new JCheckBoxMenuItem();
            viewProcessesMenuItem.setText("Processes");
            viewProcessesMenuItem.setMnemonic(KeyEvent.VK_P);
            viewProcessesMenuItem
                    .addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                if (processListFrame == null) {
                                    processListFrame = addFrame(processListPanel);
                                    processListFrame.setTitle("Processes");
                                } else {
                                    processListFrame.setVisible(true);
                                }
                            } else {
                                if (processListFrame != null) {
                                    processListFrame.setVisible(false);
                                }
                            }
                        }
                    });
        }
        return viewProcessesMenuItem;
    }

    /**
     * This method initializes sessionsMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getSessionsMenu() {
        if (sessionsMenu == null) {
            sessionsMenu = new JMenu();
            sessionsMenu.setText("Sessions");
            sessionsMenu.setMnemonic(KeyEvent.VK_S);
            sessionsMenu.add(getAcceptEstablishSessionMenuItem());
        }
        return sessionsMenu;
    }

    /**
     * This method initializes acceptEstablishSessionMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getAcceptEstablishSessionMenuItem() {
        if (acceptEstablishSessionMenuItem == null) {
            acceptEstablishSessionMenuItem = new JMenuItem();
            acceptEstablishSessionMenuItem
                    .setText("Accept/Establish Session...");
            acceptEstablishSessionMenuItem.setMnemonic(KeyEvent.VK_A);
            acceptEstablishSessionMenuItem
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            addFrame(createAcceptEstablishSessionPanel())
                                    .setTitle("Accept/Establish sessions");
                        }
                    });
        }
        return acceptEstablishSessionMenuItem;
    }

    /**
     * This method initializes newProcessMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getNewProcessMenuItem() {
        if (newProcessMenuItem == null) {
            newProcessMenuItem = new JMenuItem();
            newProcessMenuItem.setText("New Process");
            newProcessMenuItem
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            processChooser.setVisible(true);
                            ClassEntry processClassEntry = processChooser
                                    .getChosenEntry();
                            
                            if (processClassEntry == null)
                                return; // nothing chosen
                            
                            String processClass = processClassEntry
                                    .getFullyQualifiedClassName();
                            try {
                                getNode().addNodeProcess(
                                        (NodeProcess) Class.forName(
                                                processClass).newInstance());
                            } catch (Exception e1) {
                                showException(e1);
                            }
                        }
                    });
        }
        return newProcessMenuItem;
    }

    public static void main(String args[]) {
        NodeDesktopFrame nodeDesktopFrame = new NodeDesktopFrame(new Node(
                "NodeDesktopFrame"));
        nodeDesktopFrame.setVisible(true);
    }

    /**
     * Closes the node and all the views
     * 
     * @see org.mikado.imc.gui.DesktopFrame#close()
     */
    @Override
    protected void close() throws IMCException {
        super.close();

        try {
            node.close();
        } finally {
            if (sessionListFrame != null) {
                sessionListFrame.dispose();
            }

            if (processListFrame != null) {
                processListFrame.dispose();
            }
        }
    }

    /**
     * Factory method that creates the AcceptEstablishSessionPanel
     * 
     * @return the AcceptEstablishSessionPanel
     */
    public AcceptEstablishSessionPanel createAcceptEstablishSessionPanel() {
        return new AcceptEstablishSessionPanel(node);
    }

    /**
     * @return Returns the node.
     */
    public Node getNode() {
        return node;
    }
}
