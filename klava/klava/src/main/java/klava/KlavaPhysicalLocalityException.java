/*
 KlavaPhysicalLocalityException
 Physical Locality not present in the net
 */

package klava;

/**
 * Exception representing a missing PhysicalLocality, i.e., a non reachable one.
 * 
 * @author Lorenzo Bettini
 */
public class KlavaPhysicalLocalityException extends KlavaException {
    /**
     * 
     */
    private static final long serialVersionUID = -4063578502188243458L;

    public KlavaPhysicalLocalityException() {
        super();
    }

    public KlavaPhysicalLocalityException(String s) {
        super(s);
    }
}
