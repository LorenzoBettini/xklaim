/**
 * 
 */
package org.mikado.imc.events;

import java.util.TreeSet;

import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.topology.Node;

/**
 * An Event concerning the modification of a route (e.g., addition, removal,
 * etc.)
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 * 
 */
public class RouteEvent extends Event {
    /**
     * Identifier of the RouteEvents.
     */
    public final static String ROUTE_EVENT = "ROUTE";

    /** Added route */
    public final static int ROUTE_ADDED = 1;

    /** Removed route */
    public final static int ROUTE_REMOVED = 2;

    /** Added proxy */
    public final static int PROXY_ADDED = 3;

    /** Removed proxy */
    public final static int PROXY_REMOVED = 4;

    /**
     * The type of this event.
     */
    public int type;

    /**
     * It represents the SessionId of the final destination (remote end).
     * 
     */
    public SessionId destination;

    /**
     * Only for ROUTE_ADDED and ROUTE_REMOVED.
     * 
     * It represents the node that we have to go through to reach the final
     * destination.
     */
    public SessionId route;

    /**
     * The nodes for a given proxy. Only for PROXY_ADDED and PROXY_REMOVED
     */
    public TreeSet<Node> nodes;

    /**
     * Construct a RouteEvent of type ROUTE_ADDED or ROUTE_REMOVED
     * 
     * @param source
     * @param type
     * @param destination
     * @param route
     */
    public RouteEvent(Object source, int type, SessionId destination,
            SessionId route) {
        super(source);
        this.type = type;
        this.destination = destination;
        this.route = route;
    }

    /**
     * Construct a RouteEvent of type PROXY_ADDED or PROXY_REMOVED
     * 
     * @param source
     * @param type
     * @param destination
     * @param nodes
     */
    public RouteEvent(Object source, int type, SessionId destination,
            TreeSet<Node> nodes) {
        super(source);
        this.type = type;
        this.destination = destination;
        this.nodes = nodes;
    }

    /**
     * Return a string representation of this RouteEvent.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        switch (type) {
        case ROUTE_ADDED:
            return "Add route " + route + " for " + destination;
        case ROUTE_REMOVED:
            return "Remove route " + route + " for " + destination;
        case PROXY_ADDED:
            return "Add proxy " + route + " for " + nodes;
        case PROXY_REMOVED:
            return "Remove proxy " + route + " for " + nodes;
        }

        return super.toString();
    }
}
