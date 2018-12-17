/*
 * Created on Apr 14, 2006
 */
/**
 * 
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mikado.imc.common.IMCException;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ExceptionMessageBox extends JDialog {

    /** */
    private static final long serialVersionUID = -3655136175834599361L;

    private JPanel jContentPane = null;

    private JOptionPane jOptionPane = null;

    private JPanel exceptionPanel = null;

    private JPanel exceptionMessagePanel = null;

    private JLabel exceptionLabel = null;

    private JButton detailButton = null;

    private JPanel detailPanel = null;

    private JScrollPane jScrollPane = null;

    private JTextArea jTextArea = null;

    /**
     * This is the default constructor
     */
    public ExceptionMessageBox() {
        super();
        initialize();
    }

    /**
     * Constructs an ExceptionMessageBox with an Exception
     * 
     * @param throwable
     */
    public ExceptionMessageBox(Frame owner, Throwable throwable) {
        super(owner);
        StringWriter stringWriter = new StringWriter();
        PrintWriter stackTrace = new PrintWriter(stringWriter);
        throwable.printStackTrace(stackTrace);
        getJTextArea().setText(stringWriter.toString());
        setTitle("Exception: " + throwable.getClass().getName());
        getExceptionLabel().setText(throwable.getMessage());
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setContentPane(getJContentPane());
        pack();
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
            jContentPane.add(getJOptionPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jOptionPane
     * 
     * @return javax.swing.JOptionPane
     */
    private JOptionPane getJOptionPane() {
        if (jOptionPane == null) {
            jOptionPane = new JOptionPane();
            jOptionPane.setMessageType(javax.swing.JOptionPane.ERROR_MESSAGE);
            Object[] options = { getExceptionPanel() };
            jOptionPane.setMessage(options);
            jOptionPane.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            //String prop = e.getPropertyName();

                            if (ExceptionMessageBox.this.isVisible()) {
                                ExceptionMessageBox.this.dispose();
                            }
                        }
                    });
        }
        return jOptionPane;
    }

    /**
     * This method initializes exceptionPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getExceptionPanel() {
        if (exceptionPanel == null) {
            exceptionPanel = new JPanel();
            exceptionPanel.setLayout(new BoxLayout(getExceptionPanel(),
                    BoxLayout.Y_AXIS));
            exceptionPanel.add(getExceptionMessagePanel(), null);
            exceptionPanel.add(getDetailPanel(), null);
        }
        return exceptionPanel;
    }

    /**
     * This method initializes exceptionMessagePanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getExceptionMessagePanel() {
        if (exceptionMessagePanel == null) {
            exceptionMessagePanel = new JPanel();
            exceptionMessagePanel.setLayout(new BoxLayout(
                    getExceptionMessagePanel(), BoxLayout.X_AXIS));
            exceptionMessagePanel.setBorder(javax.swing.BorderFactory
                    .createEmptyBorder(10, 10, 10, 10));
            exceptionMessagePanel.add(getExceptionLabel(), null);
            exceptionMessagePanel
                    .add(Box.createRigidArea(new Dimension(30, 0)));
            exceptionMessagePanel.add(Box.createGlue());
            exceptionMessagePanel.add(getDetailButton(), null);
        }
        return exceptionMessagePanel;
    }

    /**
     * @return Returns the exceptionLabel.
     */
    public JLabel getExceptionLabel() {
        if (exceptionLabel == null) {
            exceptionLabel = new JLabel("Exception");
            exceptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        }

        return exceptionLabel;
    }

    /**
     * This method initializes detailButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getDetailButton() {
        if (detailButton == null) {
            detailButton = new JButton();
            detailButton.setText("Details >>");
            detailButton.setAlignmentX(RIGHT_ALIGNMENT);
            detailButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (detailButton.getText().endsWith(">>")) {
                        getDetailPanel().setVisible(true);
                        detailButton.setText("Details <<");
                        pack();
                    } else {
                        getDetailPanel().setVisible(false);
                        detailButton.setText("Details >>");
                        pack();
                    }
                }
            });
        }
        return detailButton;
    }

    /**
     * This method initializes detailPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getDetailPanel() {
        if (detailPanel == null) {
            detailPanel = new JPanel();
            detailPanel.setLayout(new BorderLayout());
            detailPanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            detailPanel.setVisible(false);
        }
        return detailPanel;
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea
     * 
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setColumns(50);
            jTextArea.setEditable(false);
            jTextArea.setRows(20);
        }
        return jTextArea;
    }

    /**
     * Just a demo of this MessageBox with a fake exception
     * 
     * @param args
     */
    public static void main(String args[]) {
        JDialog dialog = new ExceptionMessageBox(null, new IMCException(
                "just a fake exception"));
        dialog.setVisible(true);
    }
}
