package klava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Customized Integer

public class KInteger implements TupleItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(KInteger.class);

    private static final long serialVersionUID = -2472657424836506350L;

    public Integer integer;

    public KInteger() {
        integer = null;
    }

    public KInteger(int i) {
        integer = i;
    }

    public KInteger(Integer i) {
        integer = i;
    }

    public KInteger(KInteger i) {
        integer = (i.isFormal() ? null : i.intValue());
    }

    public String toString() {
        if (integer != null)
            return integer.toString();
        else
            return "!KInteger";
    }

    public int intValue() {
        return (integer != null ? integer.intValue() : 0);
    }

    /**
     * @see klava.TupleItem#isFormal()
     */
    public boolean isFormal() {
        return (integer == null);
    }

    public void setFormal() {
        integer = null;
    }

    /**
     * @see klava.TupleItem#setValue(java.lang.Object)
     */
    public void setValue(Object o) {
        try {
            if (o != null) {
                Integer i = ((KInteger) o).integer;
                if (i == null) // formal
                    integer = null;
                else
                    integer = ((KInteger) o).integer;
            }
        } catch (ClassCastException e) {
            LOGGER.error("KInteger type error: {}", e.toString());
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        return integer.equals(((KInteger) o).integer);
    }

    public Object duplicate() {
        return new KInteger(this);
    }

    public void setValue(String o) throws KlavaException {
        integer = Integer.parseInt(o);
    }
}
