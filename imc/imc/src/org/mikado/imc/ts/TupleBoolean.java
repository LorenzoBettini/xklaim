package org.mikado.imc.ts;

/**
 * Implementation of a boolean as a TupleItem
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class TupleBoolean implements TupleItem {

    private static final long serialVersionUID = 1895671327288035356L;

    public Boolean bool;

    public TupleBoolean() {
        bool = null;
    }

    public TupleBoolean(boolean b) {
        bool = new Boolean(b);
    }

    public TupleBoolean(Boolean b) {
        bool = new Boolean(b.booleanValue());
    }

    public TupleBoolean(String s) {
        bool = new Boolean(s);
    }

    public TupleBoolean(TupleBoolean b) {
        bool = (b.isFormal() ? null : new Boolean(b.booleanValue()));
    }

    public String toString() {
        if (bool != null)
            return bool.toString();
        else
            return getClass().getName();
    }

    public boolean booleanValue() {
        return (bool != null ? bool.booleanValue() : false);
    }

    public boolean isFormal() {
        return (bool == null);
    }

    public void setFormal() {
        bool = null;
    }

    public void setValue(Object o) throws TupleException {
        try {
            if (o != null) {
                Boolean b = ((TupleBoolean) o).bool;
                if (b == null)
                    bool = null;
                else
                    bool = new Boolean(b.booleanValue());
            }
        } catch (ClassCastException e) {
            throw new TupleException(e);
        }
    }

    public boolean equals(Object o) {
        return bool.equals(((TupleBoolean) o).bool);
    }

    public Object duplicate() {
        return new TupleBoolean(this);
    }

    public void setValue(String o) throws TupleException {
        bool = new Boolean(o);
    }
}
