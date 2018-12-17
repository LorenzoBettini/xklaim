/*
 * Created on Jan 12, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.TupleItem;

/**
 * A specialized TupleItem representing a process inserted in a tuple space.
 * 
 * @author Lorenzo Bettini
 */
public class KlavaProcessVar implements TupleItem {
    /**
     * The wrapped KlavaProcess; if null, this item is to be considered formal.
     */
    public KlavaProcess klavaProcess = null;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a forma KlavaProcessVar
     */
    public KlavaProcessVar() {
    }

    /**
     * Copy constructor
     * 
     * @param klavaProcessVar
     */
    public KlavaProcessVar(KlavaProcessVar klavaProcessVar) {
        klavaProcess = klavaProcessVar.klavaProcess;
    }

    /**
     * @see klava.TupleItem#isFormal()
     */
    public boolean isFormal() {
        return (klavaProcess == null);
    }

    /**
     * @see klava.TupleItem#setValue(java.lang.Object)
     */
    public void setValue(Object o) {
        if (o instanceof KlavaProcess)
            klavaProcess = (KlavaProcess) o;
        else if (o instanceof KlavaProcessVar)
            klavaProcess = ((KlavaProcessVar) o).klavaProcess;

        /* otherwise we leave it formal */
        /* TODO no error raising? */
    }

    /**
     * @see klava.TupleItem#setValue(java.lang.String)
     */
    public void setValue(String o) throws KlavaException {
        throw new KlavaException(
                "cannot initialized a process var from a string");
    }

    /**
     * @see klava.TupleItem#duplicate()
     */
    public Object duplicate() {
        return new KlavaProcessVar(this);
    }

    /**
     * Always return false, since two KlavaProcessVar can never be equal, since
     * we cannot establish equality on processes
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
