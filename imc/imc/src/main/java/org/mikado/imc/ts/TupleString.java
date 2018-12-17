package org.mikado.imc.ts;

/**
 * Implementation of a string as a TupleItem
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class TupleString implements TupleItem {

    /**
     * 
     */
    private static final long serialVersionUID = 1258252785854239806L;

    public String string;

    public TupleString() {
        string = null;
    }

    public TupleString(char[] s) {
        string = new String(s);
    }

    public TupleString(String s) {
        string = new String(s);
    }

    public TupleString(TupleString s) {
        string = (s.isFormal() ? null : new String(s.toString()));
    }

    public String toString() {
        if (string != null)
            return string;
        else
            return getClass().getName();
    }

    public int hashCode() {
        return string.hashCode();
    }

    public boolean isFormal() {
        return (string == null);
    }

    public void setFormal() {
        string = null;
    }

    public void setValue(Object o) throws TupleException {
        try {
            if (o != null) {
                String s = ((TupleString) o).string;
                if (s == null)
                    string = null;
                else
                    string = new String(s);
            }
        } catch (ClassCastException e) {
            throw new TupleException(e);
        }
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        
        if (isFormal()) {
            if (o instanceof TupleString) {
                TupleString new_name = (TupleString) o;
                if (new_name.isFormal())
                    return true;
            }
        }
        
        return string.equals(o.toString());
    }

    public boolean equalsIgnoreCase(Object o) {
        if (o instanceof String)
            return string.equalsIgnoreCase((String) o);
        else
            return string.equalsIgnoreCase(((TupleString) o).string);
    }

    public int compareTo(String s) {
        if (string == null)
            return -1;

        if (s == null)
            return +1;

        return string.compareTo(s);
    }

    public int compareTo(TupleString s) {
        if (s == null)
            return +1;

        return compareTo(s.string);
    }

    public String concat(String s) {
        if (string == null)
            string = new String();

        return string.concat(s);
    }

    public String concat(TupleString s) {
        return concat(s.string);
    }

    public Object duplicate() {
        return new TupleString(this);
    }

    public void setValue(String o) throws TupleException {
        string = new String(o);
    }

    public int length() {
        return (string == null ? 0 : string.length());
    }
}
