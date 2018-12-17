/*
 * Created on Apr 6, 2006
 */
package org.mikado.imc.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.mikado.imc.common.ClassCollector;
import org.mikado.imc.common.ClassEntry;
import org.mikado.imc.common.ClassFilter;
import org.mikado.imc.topology.NodeProcess;

/**
 * A specialized ClassChooserDialog that collects all the classes that are
 * subclass of a given super class and allows the user to choose one.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ClassChooserFilteredDialog extends ClassChooserDialog {
    /**
     * Updates the progress bar while filtering
     * 
     * @author bettini
     * @version $Revision: 1.2 $
     */
    public class ClassFilterProgress extends ClassFilter {
        int counter = 0;

        /**
         * @param classFilter
         * @throws ClassNotFoundException
         */
        public ClassFilterProgress(String classFilter)
                throws ClassNotFoundException {
            super(classFilter);
        }

        /**
         * @see org.mikado.imc.common.ClassFilter#filterEntry(java.util.Vector,
         *      org.mikado.imc.common.ClassEntry)
         */
        @Override
        protected void filterEntry(Vector<ClassEntry> filtered,
                ClassEntry classEntry) {
            super.filterEntry(filtered, classEntry);
            getJProgressBar().setValue(++counter);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel superClassPane = null;

    private JButton resetButton = null;

    private TitledBorder titledBorder = null;

    private String superClass = null;

    private JProgressBar jProgressBar = null;

    private JComboBox furtherSuperClassFilter = null;

    public ClassChooserFilteredDialog(String superClass, Frame owner,
            String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
        this.superClass = superClass;
        initialize();
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getSuperClassPane() {
        if (superClassPane == null) {
            superClassPane = new JPanel();
            superClassPane.setLayout(new BoxLayout(getSuperClassPane(),
                    BoxLayout.X_AXIS));
            superClassPane.setBorder(getTitledBorder());
            superClassPane.add(getFurtherSuperClassFilter(), null);
            superClassPane.add(Box.createRigidArea(new Dimension(10, 0)));
            superClassPane.add(getResetButton(), null);
        }
        return superClassPane;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getResetButton() {
        if (resetButton == null) {
            resetButton = new JButton();
            resetButton.setText("Reset");
            resetButton.setToolTipText("Go back to the initial list");
            resetButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startReloading();
                }
            });
        }
        return resetButton;
    }

    /**
     * This method initializes jProgressBar
     * 
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getJProgressBar() {
        if (jProgressBar == null) {
            jProgressBar = new JProgressBar();
            jProgressBar.setIndeterminate(false);
            jProgressBar.setStringPainted(true);
            jProgressBar.setAlignmentX(LEFT_ALIGNMENT);
        }
        return jProgressBar;
    }

    /**
     * This method initializes jComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getFurtherSuperClassFilter() {
        if (furtherSuperClassFilter == null) {
            furtherSuperClassFilter = new JComboBox();
            furtherSuperClassFilter.setRenderer(new ClassEntryRenderer());
            furtherSuperClassFilter
                    .setToolTipText("Choose a superclass to further filter the class list");
            furtherSuperClassFilter
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            startFurtherFiltering();
                        }
                    });
        }
        return furtherSuperClassFilter;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassChooserDialog classChooser = new ClassChooserFilteredDialog(
                NodeProcess.class.getName(), null, "Choose a NodeProcess", true);
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
        getContentPane().add(getSuperClassPane(), java.awt.BorderLayout.NORTH);
        setDefaultLookAndFeelDecorated(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                if (getClassList().getModel().getSize() == 0) {
                    /* let's populate the list */
                    startReloading();
                }
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getListPane().add(getJProgressBar());
    }

    protected void startReloading() {
        new Thread() {
            public void run() {
                reloadClasses();
            }
        }.start();
    }

    protected void reloadClasses() {
        resetButton.setEnabled(false);
        getFurtherSuperClassFilter().setEnabled(false);

        jProgressBar.setIndeterminate(true);

        ClassCollector classCollector = new ClassCollector();
        getPackageLabel()
                .setText("classpath: " + classCollector.getClassPath());
        Vector<ClassEntry> allEntries = classCollector.getClassEntries();
        try {
            ClassFilter classFilter = new ClassFilterProgress(superClass);
            jProgressBar.setIndeterminate(false);
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(allEntries.size());
            Vector<ClassEntry> filtered = classFilter.filter(allEntries);
            setJList(filtered);
            getFurtherSuperClassFilter().setModel(
                    new DefaultComboBoxModel(filtered));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        resetButton.setEnabled(true);
        getFurtherSuperClassFilter().setEnabled(true);
    }

    protected void startFurtherFiltering() {
        new Thread() {
            public void run() {
                furtherFilterClasses();
            }
        }.start();
    }

    protected void furtherFilterClasses() {
        resetButton.setEnabled(false);
        getFurtherSuperClassFilter().setEnabled(false);

        jProgressBar.setIndeterminate(true);

        ClassCollector classCollector = new ClassCollector();
        Vector<ClassEntry> allEntries = classCollector.getClassEntries();
        try {
            ClassFilter classFilter = new ClassFilterProgress(
                    ((ClassEntry) getFurtherSuperClassFilter()
                            .getSelectedItem()).getFullyQualifiedClassName());
            jProgressBar.setIndeterminate(false);
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(allEntries.size());
            Vector<ClassEntry> filtered = classFilter.filter(allEntries);
            setJList(filtered);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        resetButton.setEnabled(true);
        getFurtherSuperClassFilter().setEnabled(true);
    }

    /**
     * @return Returns the titledBorder.
     */
    public TitledBorder getTitledBorder() {
        if (titledBorder == null) {
            titledBorder = javax.swing.BorderFactory
                    .createTitledBorder(
                            javax.swing.BorderFactory
                                    .createBevelBorder(javax.swing.border.BevelBorder.RAISED),
                            "superclass",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            new java.awt.Color(51, 51, 51));
        }

        return titledBorder;
    }

}
