package org.mikado.imc.ts;

/**
 * Implementation of an integer as a TupleItem
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class TupleInteger implements TupleItem {

    /**
     * 
     */
    private static final long serialVersionUID = -2472657424836506350L;

    public Integer integer;

    public TupleInteger() {
        integer = null;
    }

    public TupleInteger(int i) {
        integer = new Integer(i);
    }

    public TupleInteger(Integer i) {
        integer = new Integer(i.intValue());
    }

    public TupleInteger(TupleInteger i) {
        integer = (i.isFormal() ? null : new Integer(i.intValue()));
    }

    public String toString() {
        if (integer != null)
            return integer.toString();
        else
            return getClass().getName();
    }

    public int intValue() {
        return (integer != null ? integer.intValue() : 0);
    }

    public boolean isFormal() {
        return (integer == null);
    }

    public void setFormal() {
        integer = null;
    }

    public void setValue(Object o) throws TupleException {
        try {
            if (o != null) {
                Integer i = ((TupleInteger) o).integer;
                if (i == null) // formal
                    integer = null;
                else
                    integer = new Integer(((TupleInteger) o).integer.intValue());
            }
        } catch (ClassCastException e) {
            throw new TupleException(e);
        }
    }

    public boolean equals(Object o) {
        return integer.equals(((TupleInteger) o).integer);
    }

    public Object duplicate() {
        return new TupleInteger(this);
    }

    public void setValue(String o) throws TupleException {
        integer = new Integer(o);
    }
}
