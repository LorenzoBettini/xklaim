/*
 * Created on Jan 19, 2005
 *
 */
package examples.linda;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Implements a very simple tuple space, where tuples are made up
 * of one single field.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleSpace {
    /**
     * The actual tuple space.
     */
    protected Vector<Object> tuplespace = new Vector<Object>();

    /**
     * Adds a tuple into the tuple space.
     *
     * @param o The tuple to add.
     */
    public synchronized void add(Object o) {
        tuplespace.addElement(o);
    }

    /**
     * Reads a tuple from the tuple space.
     *
     * @param o The tuple to match with.
     *
     * @return The matched tuple or null if no tuple matches.
     */
    public synchronized Object get(Object o) {
        Enumeration<Object> en = tuplespace.elements();

        while (en.hasMoreElements()) {
            Object current = en.nextElement();

            if (current.equals(o)) {
                return current;
            }
        }

        return null;
    }

    /**
     * Removes a tuple from the tuple space.
     *
     * @param o The tuple to match with.
     *
     * @return The matched tuple or null if no tuple matches.
     */
    public synchronized Object remove(Object o) {
        Enumeration<Object> en = tuplespace.elements();

        while (en.hasMoreElements()) {
            Object current = en.nextElement();

            if (current.equals(o)) {
                tuplespace.remove(current);

                return current;
            }
        }

        return null;
    }
}
