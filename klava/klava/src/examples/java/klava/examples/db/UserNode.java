/**
 *   Node.java  version 1.0  Feb 10, 2009
 */
package klava.examples.db;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.topology.ClientNode;

/**
 * Place class description here
 * 
 * @version 1.0
 * @author Fan Yang
 * 
 */
public class UserNode extends ClientNode {

    /**
     * @param server
     * @param logicalLocality
     * @throws KlavaException
     */
    public UserNode(PhysicalLocality server, LogicalLocality logicalLocality)
            throws KlavaException {
        super(server, logicalLocality);
        UserProcess user = new UserProcess();
        eval(user);
    }

}
