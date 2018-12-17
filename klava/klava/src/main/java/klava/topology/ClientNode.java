/*
 * Created on Jan 24, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;

/**
 * A specialized klava node that connects to a server (Net).
 * 
 * If the LogicalLocality is specified, then the client connects with a
 * subscribe, otherwise with a simple login.
 * 
 * @author Lorenzo Bettini
 */
public class ClientNode extends KlavaNode {
    /**
     * The locality of the server to connect to
     */
    protected PhysicalLocality server;

    /**
     * @param server
     *            The locality of the server to connect to
     * @throws KlavaException
     */
    public ClientNode(PhysicalLocality server) throws KlavaException {
        this.server = server;

        if (!login(server))
            throw new KlavaException("failed to log to " + server);

        System.out.println("logged to " + server);
    }

    /**
     * @param server
     *            The locality of the server to connect to
     * @param logicalLocality
     *            The logical locality with which we subscribe to the server
     * @throws KlavaException
     */
    public ClientNode(PhysicalLocality server, LogicalLocality logicalLocality)
            throws KlavaException {
        this.server = server;

        if (!subscribe(server, logicalLocality))
            throw new KlavaException("failed to subscribe to " + server
                    + " as " + logicalLocality);

        System.out
                .println("subscribed to " + server + " as " + logicalLocality);
    }

    /**
     * @param args
     * @throws KlavaException
     */
    public static void main(String[] args) throws KlavaException {
        /* the default one */
        PhysicalLocality server = new PhysicalLocality("localhost", 9999);

        if (args.length == 0) {
            System.out
                    .println("syntax: <locality of the server> [logical locality]");
            System.out.println("using default: " + server);
        } else if (args.length > 2) {
            System.out
                    .println("syntax: <locality of the server> [logical locality]");
            System.exit(1);
        } else if (args.length > 0) {
            server = new PhysicalLocality(args[0]);
        }

        if (args.length == 2) {
            new ClientNode(server, new LogicalLocality(args[1]));
        } else {
            new ClientNode(server);
        }
    }

}
