/*
 * Created on Feb 23, 2006
 */
package examples.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.gui.ProcessListPanel;
import org.mikado.imc.topology.ProcessContainer;
import javax.swing.JButton;

/**
 * Shows the processes that are currently running
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ProcessMonitor extends JFrame {

    /**
     * Simple test thread used in this example.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public class TestThread extends Thread {
        public void run() {
            System.out.println(getName() + ": running and sleeping...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(getName() + ": exiting...");
            processContainer.removeElement(this);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private ProcessListPanel processListPanel = null;

    protected ProcessContainer<Thread> processContainer = new ProcessContainer<Thread>();

    private JPanel jPanel = null;

    private JButton addProcessButton = null;

    /**
     * This method initializes processListPanel
     * 
     * @return org.mikado.imc.gui.ProcessListPanel
     */
    private ProcessListPanel getProcessListPanel() {
        if (processListPanel == null) {
            processListPanel = new ProcessListPanel("processes");
            processListPanel.add(getJPanel(), java.awt.BorderLayout.SOUTH);
        }
        return processListPanel;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.add(getJButton(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if (addProcessButton == null) {
            addProcessButton = new JButton();
            addProcessButton.setText("Add Process");
            addProcessButton.setToolTipText("Adds a thread that simply sleeps for 5 seconds before exiting");
            addProcessButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("adding process"); 
                    TestThread testThread = new TestThread();
                    testThread.start();
                    try {
                        processContainer.addElement(testThread);
                    } catch (IMCException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return addProcessButton;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ProcessMonitor processMonitor = new ProcessMonitor();
        processMonitor.pack();
        processMonitor.setVisible(true);
    }

    /**
     * This is the default constructor
     */
    public ProcessMonitor() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        processContainer.setEventManager(new EventManager());
        getProcessListPanel().setEventManager(processContainer.getEventManager());

        this.setSize(300, 200);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
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
            jContentPane.add(getProcessListPanel(),
                    java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

}
