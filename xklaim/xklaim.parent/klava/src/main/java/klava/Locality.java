// base class for localities

package klava;

/**
 * The base class for a LogicalLocality and a PhysicalLocality
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
abstract public class Locality implements java.io.Serializable, TupleItem,
        Comparable<Locality> {
    protected Object locality;

    public Locality() {
        locality = null;
    }

    public Locality(Object l) {
        locality = l;
    }

    public Locality(Locality l) {
        locality = l.locality;
    }

    public Locality(KString s) {
        if (s != null)
            locality = s.toString();
    }

    public abstract String toString();

    public boolean isFormal() {
        return (locality == null);
    }

    public boolean equals(Object o) {
        return (o != null) && locality.equals(((Locality) o).locality);
    }

    public int hashCode() {
        return locality.hashCode();
    }

    public void setValue(Object o) {
        try {
            if (o != null) {
                if (((Locality) o).locality != null)
                    locality = new String((String) ((Locality) o).locality);
                else
                    locality = null;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public int compareTo(Locality l) {
        return locality.toString().compareTo(l.locality.toString());
    }
}
