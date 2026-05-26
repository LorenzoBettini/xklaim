/*
 * Created on Jan 16, 2005
 */
package org.mikado.imc.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the details of a Session.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class SessionDetails extends Frame {
    private static final long serialVersionUID = 3906646401629631800L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionDetails.class);

    private JButton closeSessionButton = null;

    private JTextField sessionText = null;

    private Session session;

    private JPanel mainPanel = null;

    /**
     * @param title
     * @param session
     * @throws java.awt.HeadlessException
     */
    public SessionDetails(String title, Session session)
            throws HeadlessException {
        super(title);
        this.session = session;
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(381, 200);
        this.setTitle("Connection details");
        this.add(getMainPanel(), java.awt.BorderLayout.NORTH);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                LOGGER.debug("windowClosing()");
                dispose();
            }
        });
    }

    /**
     * This method initializes closeSessionButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCloseSessionButton() {
        if (closeSessionButton == null) {
            closeSessionButton = new JButton();
            closeSessionButton.setText("Close Session");
            closeSessionButton.setAlignmentX(CENTER_ALIGNMENT);
            closeSessionButton
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            LOGGER.debug("actionPerformed()");
                            try {
                                session.close();
                            } catch (ProtocolException e1) {
                                LOGGER.error("error closing session", e1);
                            } finally {
                                dispose();
                            }
                        }
                    });
        }
        return closeSessionButton;
    }

    /**
     * This method initializes jTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getSessionText() {
        if (sessionText == null) {
            sessionText = new JTextField();
            sessionText.setText(session.toString());
            sessionText.setAlignmentX(CENTER_ALIGNMENT);
        }
        return sessionText;
    }

    /**
     * This method initializes mainPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel
                    .setLayout(new BoxLayout(getMainPanel(), BoxLayout.Y_AXIS));
            mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,
                    10, 10, 10));
            mainPanel.add(getSessionText(), null);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(getCloseSessionButton(), null);
        }
        return mainPanel;
    }
}
