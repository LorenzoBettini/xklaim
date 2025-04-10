package klava;

// Customized Boolean

public class KBoolean implements TupleItem {

    private static final long serialVersionUID = 1895671327288035356L;

    public Boolean bool;

    public KBoolean() {
        bool = null;
    }

    public KBoolean(boolean b) {
        bool = b;
    }

    public KBoolean(Boolean b) {
        bool = b;
    }

    public KBoolean(String s) {
        bool = Boolean.parseBoolean(s);
    }

    public KBoolean(KBoolean b) {
        bool = (b.isFormal() ? null : b.booleanValue());
    }

    public String toString() {
        if (bool != null)
            return bool.toString();
        else
            return "!KBoolean";
    }

    public boolean booleanValue() {
        return (bool != null ? bool.booleanValue() : false);
    }

    // TupleItem methods
    public boolean isFormal() {
        return (bool == null);
    }

    public void setFormal() {
        bool = null;
    }

    public void setValue(Object o) {
        try {
            if (o != null) {
                Boolean b = ((KBoolean) o).bool;
                if (b == null)
                    bool = null;
                else
                    bool = b;
            }
        } catch (ClassCastException e) {
            System.err.println("KBoolean : " + e);
        }
    }

    public boolean equals(Object o) {
        return bool.equals(((KBoolean) o).bool);
    }

    public Object duplicate() {
        return new KBoolean(this);
    }

    public void setValue(String o) throws KlavaException {
        bool = Boolean.parseBoolean(o);
    }
}
