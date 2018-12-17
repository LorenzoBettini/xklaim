/*
 * Created on Feb 21, 2006
 */
package klava.examples.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;

import klava.Tuple;
import klava.TupleSpaceVector;
import klava.gui.TupleListPanel;

import org.mikado.imc.events.EventManager;


/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleSpaceFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel jPanel = null;
    private JButton addButton = null;
    private JButton removeButton = null;

    TupleListPanel tupleListPanel;    
    
    int tupleNum = 0;
    
    TupleSpaceVector tupleSpace = new TupleSpaceVector();
    private JButton removeAllButton = null;
    
    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.add(getJButton(), null);
            jPanel.add(getJButton1(), null);
            jPanel.add(getJButton2(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if (addButton == null) {
            addButton = new JButton();
            addButton.setText("Add Tuple");
            addButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Tuple tuple = new Tuple(new String("string " + ++tupleNum));
                    tupleSpace.add(tuple);
                }
            });
        }
        return addButton;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton1() {
        if (removeButton == null) {
            removeButton = new JButton();
            removeButton.setText("Remove Tuple(s)");
            removeButton.setToolTipText("select some tuples and click here to remove them");
            removeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Object[] tuples = tupleListPanel.getSelectedValues();
                    for (int i = 0; i < tuples.length; ++i) {
                        Tuple tuple = (Tuple)tuples[i];
                        tupleSpace.in_nb(tuple);
                        System.out.println("removed: " + tuple);
                    }
                }
            });
        }
        return removeButton;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
        if (removeAllButton == null) {
            removeAllButton = new JButton();
            removeAllButton.setText("Remove All");
            removeAllButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    tupleSpace.removeAllTuples();
                }
            });
        }
        return removeAllButton;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TupleSpaceFrame tupleSpaceFrame = new TupleSpaceFrame();
        tupleSpaceFrame.pack();
        tupleSpaceFrame.setVisible(true);
    }

    /**
     * This is the default constructor
     */
    public TupleSpaceFrame() {
        super();
        
        EventManager eventManager = new EventManager();        
        
        tupleSpace.setEventManager(eventManager);
        
        tupleSpace.add(new Tuple("test tuple"));
        tupleSpace.add(new Tuple("test tuple 2"));
        tupleSpace.add(new Tuple("test tuple 3"));
        tupleListPanel = new TupleListPanel(tupleSpace);
        
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(420, 266);
        this.setContentPane(getJContentPane());
        this.setTitle("Tuple List Frame");
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
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(tupleListPanel, java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
