/*
 * Created on Jan 17, 2005
 *
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mikado.imc.common.ClassEntry;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.topology.AcceptNodeCoordinator;
import org.mikado.imc.topology.AcceptOnceNodeCoordinator;
import org.mikado.imc.topology.Node;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * A panel for establishing a Session.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.14 $
 */
public class AcceptSessionPanel extends JPanel {
    protected Node node;

    private static final long serialVersionUID = 3978983275916374322L;

    private JTextField sessionIdText = null;

    private JButton OkButton = null;

    private JTextField protocolIdText = null;

    private JPanel buttonPanel = null;

    private JTextField protocolText = null;

    protected ClassChooserDialog protocolChooser;

    protected ClassChooserDialog factoryChooser;

    /**
     * Whether this is used for accepting sessions; if false, then it will be
     * used to establish sessions
     */
    private boolean doAccept = false;

    private JCheckBox onlyOnceCheckBox = null;

    private JPanel inputPanel = null;

    private JLabel jLabel1 = null;

    private JLabel jLabel4 = null;

    private JLabel protocolLabel = null;

    private JButton protocolButton = null;

    private JButton factoryButton = null;

    /**
     * @param node
     */
    public AcceptSessionPanel(Node node) {
        this.node = node;
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        protocolChooser = new ClassChooserFilteredDialog(Protocol.class.getName(), null,
                "Choose a Protocol", true);
        factoryChooser = new ClassChooserFilteredDialog(ProtocolFactory.class.getName(),
                null, "Choose a ProtocolFactory", true);

        this.setLayout(new BorderLayout());
        this.setSize(664, 235);
        this
                .setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
        this.add(getInputPanel(), java.awt.BorderLayout.CENTER);

        /*
         * the protocol button must be enabled only if only once is selected,
         * which by default is not selected
         */
        getProtocolButton().setEnabled(false);
    }

    /**
     * This method initializes protocolText
     * 
     * @return javax.swing.JTextField
     */
    protected JTextField getSessionIdText() {
        if (sessionIdText == null) {
            sessionIdText = new JTextField();
            sessionIdText.setName("address");
            sessionIdText.setPreferredSize(new java.awt.Dimension(300, 19));
        }
        return sessionIdText;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getOkButton() {
        if (OkButton == null) {
            OkButton = new JButton();
            OkButton.setText("Accept session");
            OkButton.setName("OkButton");
            OkButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    performAction();
                }
            });
        }
        return OkButton;
    }

    /**
     * This method initializes protocolText
     * 
     * @return javax.swing.JTextField
     */
    protected JTextField getProtocolIdText() {
        if (protocolIdText == null) {
            protocolIdText = new JTextField();
            protocolIdText.setPreferredSize(new Dimension(300, 19));
            protocolIdText.setName("address");
        }
        return protocolIdText;
    }

    /**
     * This method initializes jPanel1
     * 
     * @return javax.swing.JPanel
     */
    protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.add(getOnlyOnceCheckBox(), null);
            buttonPanel.add(getOkButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes protocolText
     * 
     * @return javax.swing.JTextField
     */
    protected JTextField getProtocolText() {
        if (protocolText == null) {
            protocolText = new JTextField();
        }
        return protocolText;
    }

    /**
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    public void setProtocolIdText(String t) {
        getProtocolIdText().setText(t);
    }

    /**
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    public void setProtocolText(String t) {
        getProtocolText().setText(t);
    }

    /**
     * /**
     * 
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    public void setAddressText(String t) {
        getSessionIdText().setText(t);
    }

    /**
     * @return Returns the doAccept.
     */
    public boolean isDoAccept() {
        return doAccept;
    }

    /**
     * @param doAccept
     *            The doAccept to set.
     */
    public void setDoAccept(boolean doAccept) {
        this.doAccept = doAccept;
    }

    /**
     * This is called when the button is pressed.
     * 
     * This class accepts a Session with the data inserted in the text fields.
     */
    protected void performAction() {
        try {
            Object protocolObject = Class.forName(getProtocolText().getText())
                    .newInstance();
            if (protocolObject instanceof ProtocolFactory) {
                if (getOnlyOnceCheckBox().isSelected()) {
                    node
                            .addNodeCoordinator(new AcceptOnceNodeCoordinator(
                                    ((ProtocolFactory) protocolObject)
                                            .createProtocol(), new SessionId(
                                            getProtocolIdText().getText(),
                                            getSessionIdText().getText())));
                } else {
                    node.addNodeCoordinator(new AcceptNodeCoordinator(
                            (ProtocolFactory) protocolObject, new SessionId(
                                    getProtocolIdText().getText(),
                                    getSessionIdText().getText())));
                }
            } else {
                node.addNodeCoordinator(new AcceptOnceNodeCoordinator(
                        (Protocol) protocolObject, new SessionId(
                                getProtocolIdText().getText(),
                                getSessionIdText().getText())));
            }
        } catch (Exception e) {
            showException(e);
        }
    }

    /**
     * Shows the exception in a message box
     * 
     * @param e
     * @throws HeadlessException
     */
    protected void showException(Exception e) throws HeadlessException {
        e.printStackTrace();
        new ExceptionMessageBox(null, e).setVisible(true);
    }

    /**
     * @see javax.swing.AbstractButton#setText(java.lang.String)
     */
    public void setButtonOkText(String text) {
        getOkButton().setText(text);
    }

    /**
     * This method initializes onlyOnceCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    protected JCheckBox getOnlyOnceCheckBox() {
        if (onlyOnceCheckBox == null) {
            onlyOnceCheckBox = new JCheckBox();
            onlyOnceCheckBox.setText("Only once");
            onlyOnceCheckBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (!onlyOnceCheckBox.isSelected()) {
                        getProtocolButton().setEnabled(false);
                    } else {
                        getProtocolButton().setEnabled(true);
                    }
                }
            });
        }
        return onlyOnceCheckBox;
    }

    /**
     * This method initializes inputPanel
     * 
     * @return javax.swing.JPanel
     */
    protected JPanel getInputPanel() {
        if (inputPanel == null) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 3;
            gridBagConstraints21.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints21.gridy = 2;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 2;
            gridBagConstraints12.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints12.gridy = 2;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints11.insets = new java.awt.Insets(0, 10, 0, 10);
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.gridy = 2;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints4.gridx = 1;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints3.insets = new java.awt.Insets(0, 10, 0, 10);
            gridBagConstraints3.gridy = 2;
            protocolLabel = new JLabel();
            protocolLabel.setText("protocol");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.gridx = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints1.insets = new java.awt.Insets(0, 10, 0, 10);
            gridBagConstraints1.gridy = 1;
            jLabel4 = new JLabel();
            jLabel4.setText("session id");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints.gridx = 1;
            jLabel1 = new JLabel();
            jLabel1.setText("protocol id");
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());
            inputPanel.add(jLabel1, gridBagConstraints11);
            inputPanel.add(getProtocolIdText(), gridBagConstraints);
            inputPanel.add(jLabel4, gridBagConstraints1);
            inputPanel.add(getSessionIdText(), gridBagConstraints2);
            inputPanel.add(protocolLabel, gridBagConstraints3);
            inputPanel.add(getProtocolText(), gridBagConstraints4);
            inputPanel.add(getProtocolButton(), gridBagConstraints12);
            inputPanel.add(getFactoryButton(), gridBagConstraints21);
        }
        return inputPanel;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    protected JButton getProtocolButton() {
        if (protocolButton == null) {
            protocolButton = new JButton();
            protocolButton.setText("Protocol...");
            protocolButton
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            protocolChooser.setVisible(true);
                            ClassEntry protocolClass = protocolChooser
                                    .getChosenEntry();
                            if (protocolClass != null) {
                                getProtocolText().setText(
                                        protocolClass
                                                .getFullyQualifiedClassName());
                            }
                        }
                    });
        }
        return protocolButton;
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    protected JButton getFactoryButton() {
        if (factoryButton == null) {
            factoryButton = new JButton();
            factoryButton.setText("Factory...");
            factoryButton
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            factoryChooser.setVisible(true);
                            ClassEntry protocolClass = factoryChooser
                                    .getChosenEntry();
                            if (protocolClass != null) {
                                getProtocolText().setText(
                                        protocolClass
                                                .getFullyQualifiedClassName());
                            }
                        }
                    });
        }
        return factoryButton;
    }

    /**
     * Removes the input stuff for protocol choice
     */
    public void hideProtocolChoice() {
        getInputPanel().remove(protocolLabel);
        getInputPanel().remove(getProtocolText());
        getInputPanel().remove(getProtocolButton());
        getInputPanel().remove(getFactoryButton());
    }

    /**
     * @return Returns the protocolChooser.
     */
    public ClassChooserDialog getProtocolChooser() {
        return protocolChooser;
    }

    /**
     * @param protocolChooser The protocolChooser to set.
     */
    public void setProtocolChooser(ClassChooserDialog protocolChooser) {
        this.protocolChooser = protocolChooser;
    }

    /**
     * @return Returns the factoryChooser.
     */
    public ClassChooserDialog getFactoryChooser() {
        return factoryChooser;
    }

    /**
     * @param factoryChooser The factoryChooser to set.
     */
    public void setFactoryChooser(ClassChooserDialog factoryChooser) {
        this.factoryChooser = factoryChooser;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
