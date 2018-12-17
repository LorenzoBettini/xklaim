/*
 * Created on Mar 29, 2006
 */
package klava.examples.newsgatherer;

import org.mikado.imc.common.IMCException;

import klava.KlavaException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.examples.gui.NodeWithScreen;
import klava.topology.AcceptNodeCoordinator;

/**
 * A node of a distributed database
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class DatabaseNode extends NodeWithScreen {

    /**
     * @param screenTitle
     * @throws KlavaException
     */
    public DatabaseNode(String screenTitle) throws KlavaException {
        super(screenTitle);
    }

    /**
     * Constructs the node and connects to a server.
     * 
     * @param screenTitle
     * @param serverNodeLoc
     *            The locality of the server node of the distributed database
     *            (if null then it does not connect to the node)
     * @throws KlavaException
     */
    public DatabaseNode(String screenTitle, Locality serverNodeLoc)
            throws KlavaException {
        super(screenTitle);
        if (serverNodeLoc != null)
            login(serverNodeLoc);
    }

    /**
     * Constructs the node, connects to a server, and in turns accepts
     * connection requests from other nodes.
     * 
     * @param screenTitle
     * @param serverNodeLoc
     *            The locality of the server node of the distributed database
     * @param localAcceptLoc
     *            The physical locality where we accept incoming connections
     * @throws KlavaException
     * @throws IMCException
     */
    public DatabaseNode(String screenTitle, Locality serverNodeLoc,
            PhysicalLocality localAcceptLoc) throws KlavaException,
            IMCException {
        this(screenTitle, serverNodeLoc);
        addNodeCoordinator(new AcceptNodeCoordinator(localAcceptLoc, true));
    }

    /**
     * @param args
     *            if no argument is passed then simply start the node; if one
     *            argument is "accept" then start the node and connect to a
     *            server; if two arguments are passed then starts the node
     *            connects to a server (first argument) and waits for
     *            connections on a physical locality (second argument).
     * @throws KlavaException
     * @throws IMCException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        if (args.length == 0) {
            new DatabaseNode("db node");
        } else if (args.length == 1) {
            new DatabaseNode("db node", new PhysicalLocality(args[0]));
        } else if (args.length > 1) {
            new DatabaseNode("db node", new PhysicalLocality(args[0]),
                    new PhysicalLocality(args[1]));
        }
    }

}
