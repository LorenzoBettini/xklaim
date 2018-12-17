package klava;

import java.util.Vector;
import java.util.Enumeration;

// Customized Vector

public class KVector implements TupleItem {

    /**
     * 
     */
    private static final long serialVersionUID = 2764095376758299036L;

    public Vector<Object> vector;

    public KVector() {
        vector = null;
    }

    public KVector(int initialCapacity) {
        vector = new Vector<Object>(initialCapacity);
    }

    public KVector(int initialCapacity, int capacityIncrement) {
        vector = new Vector<Object>(initialCapacity, capacityIncrement);
    }

    @SuppressWarnings("unchecked")
    public KVector(KVector v) {
        vector = (v.isFormal() ? null : (Vector<Object>) (v.vector.clone()));
    }

    public KVector(Enumeration en) {
        vector = new Vector<Object>();
        while (en.hasMoreElements())
            vector.addElement(en.nextElement());
    }

    public String toString() {
        if (vector != null)
            return vector.toString();
        else
            return "!KVector";
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

    public Enumeration elements() {
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
    public void setValue(Object o) {
        try {
            if (o != null && ((KVector) o).vector != null)
                vector = (Vector<Object>) ((KVector) o).vector.clone();
            else
                vector = null;
        } catch (ClassCastException e) {
            System.err.println("KVector : " + e);
        }
    }

    public boolean equals(Object o) {
        return (vector.equals(((KVector) o).vector));
    }

    public Object duplicate() {
        return new KVector(this);
    }

    public void setValue(String o) throws KlavaException {
        throw new KlavaException("cannot initialized a vector from a string");
    }
}
