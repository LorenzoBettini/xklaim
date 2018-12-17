package klava;

public class LogicalLocality extends Locality {

    /**
     * 
     */
    private static final long serialVersionUID = 6770234450511505387L;

    public LogicalLocality() {
        super();
    }

    public LogicalLocality(String l) {
        super(l);
    }

    public LogicalLocality(LogicalLocality l) {
        super(l);
    }

    public LogicalLocality(Locality l) {
        super(l);
    }

    public LogicalLocality(KString s) {
        super(s);
    }

    public Object duplicate() {
        return new LogicalLocality(this);
    }

    public String toString() {
        if (locality != null)
            return (String) locality;
        else
            return "!LogicalLocality";
    }

    public void setValue(String o) throws KlavaException {
        locality = new String(o);
    }

    /**
     * @see klava.Locality#hashCode()
     */
    @Override
    public int hashCode() {
        return locality.hashCode();
    }
}
