/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import java.util.Enumeration;

import org.mikado.imc.gui.PanelWithTitle;
import javax.swing.JTextArea;

import klava.KString;
import klava.Tuple;
import klava.TupleSpace;
import javax.swing.JScrollPane;
import javax.swing.JButton;

/**
 * A graphical text area where one can write using tuples (inserted via out).
 * 
 * out is the only TupleSpace operation that makes sense.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class TupleSpaceScreen extends PanelWithTitle implements TupleSpace {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JTextArea jTextArea = null;

    private JScrollPane jScrollPane = null;

    private JButton jButton = null;

    /**
     * @param title
     */
    public TupleSpaceScreen(String title) {
        super(title);
        initialize();
    }
    
    /**
     * 
     */
    public TupleSpaceScreen() {
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        
        /* adds it by using the method of TupleSpaceWithPanel */
        addComponent(getJButton());
    }

    /**
     * This method initializes jTextArea
     * 
     * @return javax.swing.JTextArea
     */
    public JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
        }
        return jTextArea;
    }

    /**
     * Returns null
     * 
     * @see klava.TupleSpace#getTupleEnumeration()
     */
    public Enumeration<Tuple> getTupleEnumeration() {
        return null;
    }

    /**
     * Returns false.
     * 
     * @see klava.TupleSpace#in_nb(klava.Tuple)
     */
    public boolean in_nb(Tuple t) {
        return false;
    }

    /**
     * Returns false
     * 
     * @see klava.TupleSpace#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        return false;
    }

    /**
     * Returns false
     * 
     * @see klava.TupleSpace#in(klava.Tuple)
     */
    public boolean in(Tuple t) throws InterruptedException {
        return false;
    }

    /**
     * Returns 0
     * 
     * @see klava.TupleSpace#length()
     */
    public int length() {
        return 0;
    }

    /**
     * Appends the string representation of the passed tuple in the text area
     * appending a "\n".
     * 
     * If the passed tuple only contains a string (or a KString) prints only the
     * string (not as
     * 
     * @see klava.TupleSpace#out(klava.Tuple)
     */
    public void out(Tuple t) {
        if (t.length() == 1) {
            Object e = t.getItem(0);

            if ((e instanceof String) || (e instanceof KString)) {
                /* simply appends the string and returns */
                getJTextArea().append(e.toString());
                return;
            }
        }

        /*
         * by default simply prints the string for of the tuple into the text
         * area with a "\n"
         */
        getJTextArea().append(t.toString() + "\n");
    }

    /**
     * Returns false
     * 
     * @see klava.TupleSpace#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple t) {
        return false;
    }

    /**
     * Returns false
     * 
     * @see klava.TupleSpace#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        return false;
    }

    /**
     * Returns false
     * 
     * @see klava.TupleSpace#read(klava.Tuple)
     */
    public boolean read(Tuple t) throws InterruptedException {
        return false;
    }

    /**
     * Clears the text area
     * 
     * @see klava.TupleSpace#removeAllTuples()
     */
    public void removeAllTuples() {
        jTextArea.setText("");
    }

    /**
     * Does nothing
     * 
     * @see klava.TupleSpace#removeTuple(int)
     */
    public void removeTuple(int i) {

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
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("clear");
            jButton.setToolTipText("clears the text area");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextArea().setText("");
                }
            });
        }
        return jButton;
    }

}
