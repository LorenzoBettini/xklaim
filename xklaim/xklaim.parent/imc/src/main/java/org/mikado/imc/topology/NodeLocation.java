package org.mikado.imc.topology;

/**
 * Represents a node location 
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class NodeLocation {
    private static final long serialVersionUID = 4049640104974759733L;
    
    /**
     * The string representation for this location.
     */
    protected String repr;

	/**
     * Creates a new NodeLocation object starting from a
     * string representation
     *
     * @param repr The string representation
     */
    public NodeLocation(String repr) {
        this.repr = repr;
    }

    /**
     * Returns the string representation of this location.
     *
     * @return the string representation of this location
     */
    public String toString() {
        return repr;
    }
}
