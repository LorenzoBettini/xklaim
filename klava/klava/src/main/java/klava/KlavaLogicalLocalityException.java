/*
 KlavaLogicalLocalityException
 LogicalLocality cannot be translated
 */

package klava;

/**
 * Represents a failure in translating a LogicalLocality into a PhysicalLocality
 * 
 * @author Lorenzo Bettini
 */
public class KlavaLogicalLocalityException extends KlavaException {
    /**
     * 
     */
    private static final long serialVersionUID = 2646981674297921630L;

    public KlavaLogicalLocalityException() {
        super();
    }

    public KlavaLogicalLocalityException(String s) {
        super(s);
    }

    public KlavaLogicalLocalityException(Locality locality) {
        super(locality.toString());
    }
}
