/*
 * Created on Feb 7, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.Tuple;
import klava.topology.KlavaNode;

/**
 * A simple node used in tests
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NewlocNode extends KlavaNode {
    public NewlocNode() {
        /* insert its class name into the tuple space */
        out(new Tuple(new KString(getClass().getCanonicalName())));
    }
}
