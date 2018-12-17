/*
 * Created on Mar 1, 2006
 */
package klava.gui;

import java.awt.event.KeyEvent;

import org.mikado.imc.gui.AcceptEstablishSessionPanel;
import org.mikado.imc.gui.NodeDesktopFrame;

import klava.topology.KlavaNode;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;

/**
 * A NodeFrame specific for Klava nodes
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class NodeFrame extends NodeDesktopFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JCheckBoxMenuItem tupleSpaceViewMenuItem = null;

    private JCheckBoxMenuItem environmentViewMenuItem = null;

    protected JInternalFrame tuplespaceListFrame = null;

    protected TupleListPanel tupleListPanel = null;

    protected JInternalFrame environmentListFrame = null;

    protected EnvironmentListPanel environmentListPanel = null;

    /**
     * @param node
     */
    public NodeFrame(KlavaNode node) {
        super(node);

        tupleListPanel = new TupleListPanel(node.getTupleSpace());
        environmentListPanel = new EnvironmentListPanel(node.getEnvironment());

        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        getViewMenu().add(getTupleSpaceViewMenuItem());
        getViewMenu().add(getEnvironmentViewMenuItem());
    }

    /**
     * @see org.mikado.imc.gui.NodeDesktopFrame#createAcceptEstablishSessionPanel()
     */
    @Override
    public AcceptEstablishSessionPanel createAcceptEstablishSessionPanel() {
        AcceptEstablishSessionPanel acceptEstablishSessionPanel = super
                .createAcceptEstablishSessionPanel();

        acceptEstablishSessionPanel
                .setAcceptSessionPanel(new AcceptSessionPanel((KlavaNode) node));
        acceptEstablishSessionPanel
                .setEstablishSessionPanel(new EstablishSessionPanel(
                        (KlavaNode) node));

        return acceptEstablishSessionPanel;
    }

    /**
     * This method initializes tupleSpaceViewMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    protected JCheckBoxMenuItem getTupleSpaceViewMenuItem() {
        if (tupleSpaceViewMenuItem == null) {
            tupleSpaceViewMenuItem = new JCheckBoxMenuItem();
            tupleSpaceViewMenuItem.setText("TupleSpace");
            tupleSpaceViewMenuItem.setMnemonic(KeyEvent.VK_T);
            tupleSpaceViewMenuItem.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (tupleSpaceViewMenuItem.isSelected()) {
                        if (tuplespaceListFrame == null) {
                            tuplespaceListFrame = addFrame(tupleListPanel);
                        } else {
                            tuplespaceListFrame.setVisible(true);
                        }
                    } else {
                        tuplespaceListFrame.setVisible(false);
                    }
                }
            });
        }
        return tupleSpaceViewMenuItem;
    }

    /**
     * This method initializes environmentViewMenuItem
     * 
     * @return javax.swing.JCheckBoxMenuItem
     */
    protected JCheckBoxMenuItem getEnvironmentViewMenuItem() {
        if (environmentViewMenuItem == null) {
            environmentViewMenuItem = new JCheckBoxMenuItem();
            environmentViewMenuItem.setText("Environment");
            environmentViewMenuItem.setMnemonic(KeyEvent.VK_E);
            environmentViewMenuItem
                    .addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent e) {
                            if (environmentViewMenuItem.isSelected()) {
                                if (environmentListFrame == null) {
                                    environmentListFrame = addFrame(environmentListPanel);
                                } else {
                                    environmentListFrame.setVisible(true);
                                }
                            } else {
                                if (environmentListFrame != null) {
                                    environmentListFrame.setVisible(false);
                                }
                            }
                        }
                    });
        }
        return environmentViewMenuItem;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        NodeFrame nodeFrame = new NodeFrame(new KlavaNode());
        nodeFrame.setSize(400, 500);
        nodeFrame.setVisible(true);
    }

}
