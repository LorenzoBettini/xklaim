package klava;

// Customized String

public class KString implements TupleItem {

    /**
     * 
     */
    private static final long serialVersionUID = 1258252785854239806L;

    public String string;

    public KString() {
        string = null;
    }

    public KString(char[] s) {
        string = new String(s);
    }

    public KString(String s) {
        string = new String(s);
    }

    public KString(KString s) {
        string = (s.isFormal() ? null : new String(s.toString()));
    }

    public String toString() {
        if (string != null)
            return string;
        else
            return "!KString";
    }

    public int hashCode() {
        return string.hashCode();
    }

    /**
     * @see klava.TupleItem#isFormal()
     */
    public boolean isFormal() {
        return (string == null);
    }

    /**
     * 
     */
    public void setFormal() {
        string = null;
    }

    /**
     * @see klava.TupleItem#setValue(java.lang.Object)
     */
    public void setValue(Object o) {
        try {
            if (o != null) {
                String s = ((KString) o).string;
                if (s == null)
                    string = null;
                else
                    string = new String(s);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        
        if (isFormal()) {
            if (o instanceof KString) {
                KString new_name = (KString) o;
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
            return string.equalsIgnoreCase(((KString) o).string);
    }

    public int compareTo(String s) {
        if (string == null)
            return -1;

        if (s == null)
            return +1;

        return string.compareTo(s);
    }

    public int compareTo(KString s) {
        if (s == null)
            return +1;

        return compareTo(s.string);
    }

    public String concat(String s) {
        if (string == null)
            string = new String();

        return string.concat(s);
    }

    public String concat(KString s) {
        return concat(s.string);
    }

    public Object duplicate() {
        return new KString(this);
    }

    public void setValue(String o) throws KlavaException {
        string = new String(o);
    }

    /**
     * @see java.lang.String#length()
     */
    public int length() {
        return (string == null ? 0 : string.length());
    }
}
