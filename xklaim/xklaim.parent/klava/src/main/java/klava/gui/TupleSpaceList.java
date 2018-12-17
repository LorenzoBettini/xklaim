/*
 * Created on Mar 13, 2006
 */
package klava.gui;

import java.util.Enumeration;

import org.mikado.imc.events.EventManager;

import klava.KString;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;

/**
 * A graphical list that can be accessed through the TupleSpace interface.
 * 
 * Communication protocol:
 * 
 * <tt>out("COMMAND", "getSelectedItem")</tt> makes the tuple space insert a
 * tuple <tt>("COMMAND", "getSelectedItem", selected)</tt> where selected is a
 * KString containing the selected string.
 * 
 * <tt>out("COMMAND", "getSelectedItems")</tt> makes the tuple space insert a
 * tuple <tt>("COMMAND", "getSelectedItems", selected)</tt> where selected is
 * a TupleSpace containing the selected strings.
 * 
 * <tt>out("COMMAND", "removeAll")</tt> removes all the elements in the list.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class TupleSpaceList extends TupleListPanel implements TupleSpace {

    public static final KString removeAllString = new KString("removeAll");

    public static final KString getSelectedItemsString = new KString("getSelectedItems");

    public static final KString getSelecteItemString = new KString("getSelectedItem");

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The embedded tuple space representing the graphical list.
     */
    protected TupleSpace tupleSpace;

    /**
     * The embedded tuple space representing the command responses.
     */
    protected TupleSpace commandTupleSpace = new TupleSpaceVector();

    /**
     * The string representing a command request (stored in a tuple)
     */
    public static final KString cmdString = new KString("COMMAND");

    /**
     * 
     */
    public TupleSpaceList() {
        this.tupleSpace = new TupleSpaceVector();
        addTupleListPanel(this.tupleSpace);
    }

    /**
     * @param title
     */
    public TupleSpaceList(String title) {
        super(title);
        this.tupleSpace = new TupleSpaceVector();
        addTupleListPanel(this.tupleSpace);
    }

    /**
     * @param tupleSpace
     */
    public TupleSpaceList(TupleSpace tupleSpace) {
        super(tupleSpace);
        this.tupleSpace = tupleSpace;
    }

    /**
     * @param title
     * @param tupleSpace
     */
    public TupleSpaceList(String title, TupleSpace tupleSpace) {
        super(title, tupleSpace);
        this.tupleSpace = tupleSpace;
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
        if (isCommandTuple(t))
            return commandTupleSpace.in_nb(t);

        return tupleSpace.in_nb(t);
    }

    /**
     * @see klava.TupleSpace#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        if (isCommandTuple(t))
            return commandTupleSpace.in_t(t, TimeOut);

        return tupleSpace.in_t(t, TimeOut);
    }

    /**
     * @see klava.TupleSpace#in(klava.Tuple)
     */
    public boolean in(Tuple t) throws InterruptedException {
        if (isCommandTuple(t))
            return commandTupleSpace.in(t);

        return tupleSpace.in(t);
    }

    /**
     * @see klava.TupleSpace#length()
     */
    public int length() {
        return tupleSpace.length();
    }

    /**
     * @param t
     * @return true if the passed tuple contains the command string as the first
     *         item
     */
    protected boolean isCommandTuple(Tuple t) {
        if (t.length() > 1) { // request ?
            Object string = t.getItem(0);
            return (isString(string) && isCommand((KString) string));
        }

        return false;
    }

    /**
     * @see klava.TupleSpace#out(klava.Tuple)
     */
    public void out(Tuple t) {
        boolean done = false;

        /*
         * first check whether the passed tuple is a command request; in that
         * case, it does not actually insert the passed tuple in the list
         */
        if (isCommandTuple(t)) {
            Object command = t.getItem(1);
            if (isString(command)) {
                KString request = (KString) command;
                done = true; // maybe we're OK
                if (request.equals(getSelecteItemString)) {
                    String s = null;
                    Object selected = getSelectedValue();
                    if (selected != null)
                        s = Tuple.cleanString(getSelectedValue().toString());
                    Tuple newTuple = new Tuple(cmdString, request);
                    if (s == null)
                        newTuple.add(new KString(""));
                    else
                        newTuple.add(new KString(s));
                    commandTupleSpace.out(newTuple);
                } else if (request.equals(getSelectedItemsString)) {
                    Object values[] = getSelectedValues();
                    TupleSpaceVector ts = new TupleSpaceVector();
                    for (int i = 0; i < values.length; ++i)
                        ts.add(new Tuple(new KString(Tuple
                                .cleanString(values[i].toString()))));
                    commandTupleSpace.out(new Tuple(cmdString, request, ts));
                } else if (request.equals(removeAllString)) {
                    removeAllTuples();
                } else
                    done = false;
            }
        }

        /*
         * otherwise it is simply a Tuple to be inserted in the list
         */
        if (!done)
            tupleSpace.out(t);
    }

    /**
     * @param o
     * @return Whether the passed object is an instance of KString
     */
    protected boolean isString(Object o) {
        return (o instanceof KString);
    }

    /**
     * @param s
     * @return Whether the passed string is the command string
     */
    protected boolean isCommand(KString s) {
        return s.equalsIgnoreCase("COMMAND");
    }

    /**
     * @see klava.TupleSpace#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple t) {
        if (isCommandTuple(t))
            return commandTupleSpace.read_nb(t);

        return tupleSpace.read_nb(t);
    }

    /**
     * @see klava.TupleSpace#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        if (isCommandTuple(t))
            return commandTupleSpace.read_t(t, TimeOut);

        return tupleSpace.read_t(t, TimeOut);
    }

    /**
     * @see klava.TupleSpace#read(klava.Tuple)
     */
    public boolean read(Tuple t) throws InterruptedException {
        if (isCommandTuple(t))
            return commandTupleSpace.read(t);

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
        super.setEventManager(eventManager);
        tupleSpace.setEventManager(eventManager);
    }

    /**
     * @return Returns the tupleSpace associated to the graphical list.
     */
    public final TupleSpace getTupleSpace() {
        return tupleSpace;
    }

}
