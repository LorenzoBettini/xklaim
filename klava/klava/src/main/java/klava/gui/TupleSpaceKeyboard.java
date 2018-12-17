/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.gui.PanelWithTitle;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import klava.KString;
import klava.KlavaException;
import klava.KlavaTupleParsingException;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleParser;
import klava.TupleSpace;
import klava.TupleSpaceVector;

/**
 * A Text edit field accessible via TupleSpace interface.
 * 
 * If ENTER is pressed, then the text of the TextField is interpreted as a Tuple
 * (via TupleParser) and inserted in the tuple space.
 * 
 * If one searches for a Tuple of the shape ("getText", !s) then a tuple
 * ("getText", text) is inserted in the tuple space, where text is the current
 * text of the TextField (thus it always returns something, possibly an empty
 * string).
 * 
 * If one puts a Tuple of the shape ("setText", s) then the string s is inserted
 * in the TextField (replacing what was there before).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class TupleSpaceKeyboard extends PanelWithTitle implements TupleSpace {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;

    private JTextField jTextField = null;

    public static final KString getTextString = new KString("getText");

    public static final KString setTextString = new KString("setText");

    /**
     * The buffer of this text is stored in a tuple space
     */
    TupleSpace tupleSpace = new TupleSpaceVector();

    /**
     * 
     */
    public TupleSpaceKeyboard() {
        super();
        initialize();
    }

    /**
     * @param title
     */
    public TupleSpaceKeyboard(String title) {
        super(title);
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.add(getJPanel(), java.awt.BorderLayout.CENTER);

    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getJTextField(), gridBagConstraints);
        }
        return jPanel;
    }

    /**
     * This method initializes jTextField
     * 
     * @return javax.swing.JTextField
     */
    public JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        try {
                            out(parseTuple());
                        } catch (HeadlessException e1) {
                            e1.printStackTrace();
                        } catch (KlavaTupleParsingException e1) {
                            showException(e1);
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        getJTextField().setText("");
                        break;
                    }
                }
            });
        }
        return jTextField;
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
        if (!execInputCommand(t, true)) {
            /*
             * if the in succeds, also clear the text field, since this is a
             * removal operation
             */
            if (tupleSpace.in_nb(t))
                getJTextField().setText("");
            else
                return false;
        }

        return true;
    }

    /**
     * @see klava.TupleSpace#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        if (!execInputCommand(t, true)) {
            /*
             * if the in succeds, also clear the text field, since this is a
             * removal operation
             */
            if (tupleSpace.in_t(t, TimeOut))
                getJTextField().setText("");
            else
                return false;
        }

        return true;
    }

    /**
     * @see klava.TupleSpace#in(klava.Tuple)
     */
    public boolean in(Tuple t) throws InterruptedException {
        if (!execInputCommand(t, true)) {
            /*
             * if the in succeds, also clear the text field, since this is a
             * removal operation
             */
            if (tupleSpace.in(t))
                getJTextField().setText("");
            else
                return false;
        }

        return true;
    }

    /**
     * @see klava.TupleSpace#length()
     */
    public int length() {
        return tupleSpace.length();
    }

    /**
     * @see klava.TupleSpace#out(klava.Tuple)
     */
    public void out(Tuple t) {
        if (!execOutputCommand(t))
            tupleSpace.out(t);
    }

    /**
     * @see klava.TupleSpace#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple t) {
        return execInputCommand(t, false) || tupleSpace.read_nb(t);
    }

    /**
     * @see klava.TupleSpace#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        return execInputCommand(t, false) || tupleSpace.read_t(t, TimeOut);
    }

    /**
     * @see klava.TupleSpace#read(klava.Tuple)
     */
    public boolean read(Tuple t) throws InterruptedException {
        return execInputCommand(t, false) || tupleSpace.read(t);
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

    /**
     * @throws HeadlessException
     * @throws KlavaTupleParsingException
     */
    protected Tuple parseTuple() throws HeadlessException,
            KlavaTupleParsingException {
        return TupleParser.parseString(getJTextField().getText());
    }

    /**
     * Whether the passed tuple is a command tuple
     * 
     * @param tuple
     * @return Whether the passed tuple is a command tuple
     */
    public boolean isCommand(Tuple tuple) {
        return (tuple.length() > 1 && ((tuple.getItem(0).equals(getTextString) || tuple
                .getItem(0).equals(setTextString)) && tuple.length() == 2));
    }

    /**
     * Executes the input command represented by the tuple.
     * 
     * @param tuple
     * @param remove
     *            Whether the tuple was argument of an in
     * @return whether the tuple represented a command
     */
    protected boolean execInputCommand(Tuple tuple, boolean remove) {
        if (!isCommand(tuple))
            return false;

        String command = tuple.getItem(0).toString();

        try {
            if (command.equals(getTextString.toString())) {
                Object arg = tuple.getItem(1);
                if (arg instanceof TupleItem) {
                    ((TupleItem) arg).setValue(getJTextField().getText());

                    /* if it was an in, then clear the text */
                    if (remove)
                        getJTextField().setText("");
                }
            } else if (command.equals(setTextString.toString())) {
                getJTextField().setText(tuple.getItem(1).toString());
            }
        } catch (KlavaException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Executes the output command represented by the tuple.
     * 
     * @param tuple
     * @return whether the tuple represented a command
     */
    protected boolean execOutputCommand(Tuple tuple) {
        if (!isCommand(tuple))
            return false;

        String command = tuple.getItem(0).toString();

        if (command.equals(setTextString.toString())) {
            getJTextField().setText(tuple.getItem(1).toString());
        }

        return true;
    }
}
