/*
 * Created on Oct 21, 2005
 */
package klava;

/**
 * Translates a LogicalLocality into a PhysicalLocality
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public interface LogicalLocalityResolver {
    /**
     * Translates the passed LogicalLocality into a PhysicalLocality.
     * 
     * @param logicalLocality
     * @return
     * @throws KlavaException
     */
    PhysicalLocality resolve(LogicalLocality logicalLocality) throws KlavaException;
}
