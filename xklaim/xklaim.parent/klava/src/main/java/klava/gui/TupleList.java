/*
 * Created on Feb 21, 2006
 */
package klava.gui;

import java.util.Enumeration;

import klava.Tuple;
import klava.TupleSpace;
import klava.events.TupleEvent;
import klava.events.TupleEvent.TupleEventType;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.gui.DefaultListPanel;


/**
 * A Panel containing a JList that shows the tuples in a tuple space.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleList extends DefaultListPanel implements EventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct a TupleList starting from an existing tuple space
     * (it thus initializes the list with the tuples currently in the
     * tuple space).
     * 
     * @param tupleSpace
     */
    public TupleList(TupleSpace tupleSpace) {
        super();
        Enumeration<Tuple> tuples = tupleSpace.getTupleEnumeration();
        while (tuples.hasMoreElements()) {
            addTuple(tuples.nextElement());
        }
    }

    /**
     * appends the tuple at the end of the list
     * 
     * @param tuple
     */
    public synchronized void addTuple(Tuple tuple) {
        addElement(tuple);
    }
    
    /**
     * removes the tuple from the list
     * 
     * @param tuple
     */
    public synchronized void removeTuple(Tuple tuple) {
        String tupleToSearch = tuple.toString();
        Enumeration<?> tuples = elements();
        int index = 0;
        while (tuples.hasMoreElements()) {
            String repr = tuples.nextElement().toString();
            if (tupleToSearch.equals(repr)) {
                removeElementAt(index);
                break;
            }
            ++index;
        }
    }

    /**
     * Adds or remove a tuple according to the received TupleEvent.
     * 
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        if (event instanceof TupleEvent) {
            TupleEvent tupleEvent = (TupleEvent) event;
            
            if (tupleEvent.eventType == TupleEventType.ADDED) {
                addTuple(tupleEvent.tuple);
            } else if (tupleEvent.eventType == TupleEventType.REMOVED) {
                removeTuple(tupleEvent.tuple);
            } else { /* remove all */
                removeAllElements();
            }
        }
        /* otherwise simply ignore it */
    }
}
