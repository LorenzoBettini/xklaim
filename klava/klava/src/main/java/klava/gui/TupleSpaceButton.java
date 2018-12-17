/*
 * Created on Mar 15, 2006
 */
package klava.gui;

import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JButton;

import org.mikado.imc.events.EventManager;

import klava.KString;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;

/**
 * A graphical button that can be accessed through the TupleSpace interface.
 * 
 * A tuple ("CLICKED") is available each time the button is clicked.
 * 
 * Inserting any other tuple sets the text of the button.
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class TupleSpaceButton extends JPanel implements TupleSpace {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JButton jButton = null;
    
    /**
     * The tuple space used to interact with the button.
     */
    protected TupleSpace tupleSpace = new TupleSpaceVector();
    
    public static final KString clickedString = new KString("CLICKED");
    
    /**
     * The button text (default is "OK")
     */
    protected String buttonText = "0K";
    
    /**
     * This is the default constructor
     */
    public TupleSpaceButton() {
        super();
        initialize();
    }

    /**
     * This is the default constructor
     */
    public TupleSpaceButton(String label) {
        super();
        buttonText = label;
        initialize();
    }

    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(300, 200);
        this.add(getJButton(), null);
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    public JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText(buttonText);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    /* insert a ("CLICKED") tuple in the tuple space */
                    out(new Tuple(clickedString));
                }
            });
        }
        return jButton;
    }

    /**
     * @see org.mikado.imc.events.EventGenerator#getEventManager()
     */
    public EventManager getEventManager() {
        return tupleSpace.getEventManager();
    }

    /**
     * @see klava.TupleSpace#getTupleEnumeration()
     */
    public Enumeration<Tuple> getTupleEnumeration() {
        return tupleSpace.getTupleEnumeration();
    }

    /**
     * @see klava.TupleSpace#in_nb(klava.Tuple)
     */
    public boolean in_nb(Tuple t) {
        return tupleSpace.in_nb(t);
    }

    /**
     * @see klava.TupleSpace#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        return tupleSpace.in_t(t, TimeOut);
    }

    /**
     * @see klava.TupleSpace#in(klava.Tuple)
     */
    public boolean in(Tuple t) throws InterruptedException {
        return tupleSpace.in(t);
    }

    /**
     * @see klava.TupleSpace#length()
     */
    public int length() {
        return tupleSpace.length();
    }

    /**
     * If the inserted tuple is different from ("CLICKED") then
     * it is used to set the text of the button.
     * 
     * @see klava.TupleSpace#out(klava.Tuple)
     */
    public void out(Tuple t) {
        if (!(t.length() == 1 && t.getItem(0) != null &&
                t.getItem(0).toString().equalsIgnoreCase(clickedString.toString()))) {
            getJButton().setText(Tuple.cleanString(t.toString()));
            buttonText = getJButton().getText();
            return;
        }
        
        tupleSpace.out(t);
    }

    /**
     * @see klava.TupleSpace#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple t) {
        return tupleSpace.read_nb(t);
    }

    /**
     * @see klava.TupleSpace#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        return tupleSpace.read_t(t, TimeOut);
    }

    /**
     * @see klava.TupleSpace#read(klava.Tuple)
     */
    public boolean read(Tuple t) throws InterruptedException {
        return tupleSpace.read(t);
    }

    /**
     * @see klava.TupleSpace#removeAllTuples()
     */
    public void removeAllTuples() {
        tupleSpace.removeAllTuples();
    }

    /**
     * @see klava.TupleSpace#removeTuple(int)
     */
    public void removeTuple(int i) {
        tupleSpace.removeTuple(i);
    }

    /**
     * @see org.mikado.imc.events.EventGenerator#setEventManager(org.mikado.imc.events.EventManager)
     */
    public void setEventManager(EventManager eventManager) {
        tupleSpace.setEventManager(eventManager);
    }

}
