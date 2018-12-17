/*
 * Created on Mar 29, 2006
 */
package klava.examples.newsgatherer;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.examples.gui.NodeWithScreen;

/**
 * A client node for NewsGatherer
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NewsClientNode extends NodeWithScreen {

    /**
     * @param nodeName
     * @param serverLoc The server locality to log to
     * @throws KlavaException
     */
    public NewsClientNode(String nodeName, Locality serverLoc)
            throws KlavaException {
        super(nodeName);
        subscribe(serverLoc, new LogicalLocality(nodeName));
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws KlavaMalformedPhyLocalityException
     */
    public static void main(String[] args)
            throws KlavaMalformedPhyLocalityException, KlavaException {
        new NewsClientNode("news client node", new PhysicalLocality(args[0]));
    }

}
