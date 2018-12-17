package org.mikado.imc.ts;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Implementation of a vector as a TupleItem
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class TupleVector implements TupleItem {

    private static final long serialVersionUID = 2764095376758299036L;

    public Vector<Object> vector;

    public TupleVector() {
        vector = null;
    }

    public TupleVector(int initialCapacity) {
        vector = new Vector<Object>(initialCapacity);
    }

    public TupleVector(int initialCapacity, int capacityIncrement) {
        vector = new Vector<Object>(initialCapacity, capacityIncrement);
    }

    @SuppressWarnings("unchecked")
    public TupleVector(TupleVector v) {
        vector = (v.isFormal() ? null : (Vector<Object>) (v.vector.clone()));
    }

    public TupleVector(Enumeration<Object> en) {
        vector = new Vector<Object>();
        while (en.hasMoreElements())
            vector.addElement(en.nextElement());
    }

    public String toString() {
        if (vector != null)
            return vector.toString();
        else
            return getClass().getName();
    }

    public void addElement(Object obj) {
        if (vector == null)
            vector = new Vector<Object>();
        vector.addElement(obj);
    }

    public void remove(Object o) {
        if (vector != null)
            vector.remove(o);
    }

    public Enumeration<Object> elements() {
        if (vector == null)
            return null;

        return vector.elements();
    }

    public int size() {
        return vector.size();
    }

    public boolean empty() {
        return (vector == null || vector.size() == 0);
    }

    // TupleItem methods
    public boolean isFormal() {
        return (vector == null);
    }

    public void setFormal() {
        vector = null;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object o) throws TupleException {
        try {
            if (o != null && ((TupleVector) o).vector != null)
                vector = (Vector<Object>) ((TupleVector) o).vector.clone();
            else
                vector = null;
        } catch (ClassCastException e) {
            throw new TupleException(e);
        }
    }

    public boolean equals(Object o) {
        return (vector.equals(((TupleVector) o).vector));
    }

    public Object duplicate() {
        return new TupleVector(this);
    }

    public void setValue(String o) throws TupleException {
        throw new TupleException("cannot initialized a vector from a string");
    }
}
