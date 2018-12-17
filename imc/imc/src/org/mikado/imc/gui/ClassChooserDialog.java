/*
 * Created on Apr 6, 2006
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.mikado.imc.common.ClassEntry;

/**
 * A dialog that allows the user to select a class from a list
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ClassChooserDialog extends JDialog {

    /**
     * @author bettini
     * @version $Revision: 1.4 $
     */
    public class ClassEntryRenderer extends JLabel implements ListCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ClassEntryRenderer() {
            setOpaque(true);
        }

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
         *      java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null)
                return this;

            /*
             * for the selected entry show also the package otherwise, only the
             * class name
             */
            if (isSelected) {
                setText(value.toString());
                getPackageLabel().setText(((ClassEntry) value).packageName);
            } else {
                setText(((ClassEntry) value).className);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JPanel listPane = null;

    private JPanel buttonPane = null;

    private JButton okButton = null;

    private JButton cancelButton = null;

    private JScrollPane jScrollPane = null;

    private JList classList = null;

    private JLabel packageLabel = null;

    private ClassEntry chosenEntry = null;

    public ClassChooserDialog(Frame owner, String title, boolean modal)
            throws HeadlessException {
        super(owner, title, modal);
        initialize();
    }

    public ClassChooserDialog(Vector<String> classNames, Frame owner,
            String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
        initialize();

        Vector<ClassEntry> entries = new Vector<ClassEntry>();
        Iterator<String> iterator = classNames.iterator();
        while (iterator.hasNext()) {
            ClassEntry classEntry = ClassEntry.parseClassName(iterator.next());
            if (classEntry != null)
                entries.add(classEntry);
        }

        setJList(entries);
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    protected JPanel getListPane() {
        if (listPane == null) {
            packageLabel = new JLabel();
            packageLabel.setText(" ");
            packageLabel.setForeground(java.awt.SystemColor.activeCaption);
            packageLabel.setBackground(java.awt.SystemColor.info);
            listPane = new JPanel();
            listPane.setLayout(new BoxLayout(getListPane(), BoxLayout.Y_AXIS));
            listPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,
                    10, 10, 10));
            listPane.add(getJScrollPane(), null);
            listPane.add(packageLabel, null);
            listPane.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        return listPane;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (buttonPane == null) {
            buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout());
            buttonPane.add(getOKButton(), null);
            buttonPane.add(getCancelButton(), null);
        }
        return buttonPane;
    }

    /**
     * This method initializes okButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getOKButton() {
        if (okButton == null) {
            okButton = new JButton();
            okButton.setText("OK");
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    chosenEntry = (ClassEntry) getClassList()
                            .getSelectedValue();
                    dispose();
                }
            });
            getRootPane().setDefaultButton(okButton);
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    chosenEntry = null;
                    dispose();
                }
            });
        }
        return cancelButton;
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setAlignmentX(LEFT_ALIGNMENT);
            jScrollPane.setViewportView(getClassList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList
     * 
     * @return javax.swing.JList
     */
    protected JList getClassList() {
        if (classList == null) {
            classList = new JList();
            classList
                    .setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            classList.setCellRenderer(new ClassEntryRenderer());
        }
        return classList;
    }

    protected void setJList(Vector<ClassEntry> entries) {
        classList.setListData(entries);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassChooserDialog classChooser = new ClassChooserDialog(null,
                "Choose a NodeProcess", true);
        classChooser.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        classChooser.setVisible(true);

        if (classChooser.getChosenEntry() != null) {
            System.out.println("chosen: " + classChooser.getChosenEntry());
            classChooser.dispose();
        }
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(400, 500);
        this.setContentPane(getJContentPane());
        setDefaultLookAndFeelDecorated(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {   
        	public void windowClosing(java.awt.event.WindowEvent e) {    
                /* clear the chosen entry */
                chosenEntry = null;
        	}
            public void windowOpened(java.awt.event.WindowEvent e) {
                /* reset the chosen entry */
                chosenEntry = null;
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
            jContentPane.add(getListPane(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getJPanel2(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * @return Returns the packageLabel.
     */
    public JLabel getPackageLabel() {
        return packageLabel;
    }

    /**
     * @return Returns the chosenEntry.
     */
    public ClassEntry getChosenEntry() {
        return chosenEntry;
    }

}
