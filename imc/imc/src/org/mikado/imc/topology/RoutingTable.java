package org.mikado.imc.topology;

import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import org.mikado.imc.events.EventGeneratorAdapter;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;

/**
 * Store routes (ProtocolStack) for physical localities
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class RoutingTable extends EventGeneratorAdapter {
    /**
     * The stack corresponding to a SessionId.
     * 
     * Consider the association:
     * 
     * <pre>
     * id = stack(local - remote)
     * </pre>
     * 
     * where local represents the SessionId of the local end of the associated
     * stack and remote the SessionId of the remote end (again of the associated
     * stack).
     * 
     * If you have:
     * 
     * <pre>
     * id = stack(local - id)
     * </pre>
     * 
     * it means that you can communicate directly to id (you have a direct
     * Session to it).
     * 
     * If you have:
     * 
     * <pre>
     * id = stack(local - id2)
     * </pre>
     * 
     * it means that you have to pass through id2 to communicate with id (i.e.,
     * you have to use the session that you have towards id2).
     * 
     * If you had:
     * 
     * <pre>
     * id = stack(id - id2)
     * </pre>
     * 
     * you'd have an inconsistency situation, since you have a loop: in order to
     * communicate with yourself you have to pass through another node.
     * 
     * Consistency about proxies:
     * 
     * If you have
     * 
     * <pre>
     * id = stack(local - id2)
     * </pre>
     * 
     * Then id2 must be known as the proxy for id.
     * 
     * Moreover, if you have that id1 is a proxy for id2, then you must also
     * have an entry
     * 
     * <pre>
     * id1 = stack(local - id2)
     * </pre>
     * 
     * This class takes care of keeping consistency about the routing table.
     * However, we provide the method checkForConsistency() that can be helpful
     * in case you redefine some behaviors of this class.
     */
    protected Hashtable<SessionId, ProtocolStack> stacks = new Hashtable<SessionId, ProtocolStack>();

    /**
     * Nodes to which messages for other nodes are forwarded
     */
    protected Hashtable<SessionId, TreeSet<SessionId>> proxies = new Hashtable<SessionId, TreeSet<SessionId>>();

    /**
     * Copy constructor. Makes a shallow copy.
     * 
     * @param routingTable
     */
    @SuppressWarnings("unchecked")
    public RoutingTable(RoutingTable routingTable) {
        synchronized (routingTable) {
            stacks = (Hashtable<SessionId, ProtocolStack>) routingTable.stacks
                    .clone();
            proxies = (Hashtable<SessionId, TreeSet<SessionId>>) routingTable.proxies
                    .clone();
        }
    }

    public RoutingTable() {
    }

    /**
     * Resets the routing table (without issuing any event).
     */
    public synchronized void reset() {
        stacks = new Hashtable<SessionId, ProtocolStack>();
        proxies = new Hashtable<SessionId, TreeSet<SessionId>>();
    }

    /**
     * Adds the association loc=protocolStack. It also checks for the
     * consistency condition. If it is not satisfied it returns <tt>false</tt>
     * and does not add the entry.
     * 
     * @see org.mikado.imc.topology.RoutingTable#stacks
     * 
     * @param destination
     * @param route
     *            The route to follow to reach the destination in case it is the
     *            same as destination it means that we are directly connected to
     *            destination.
     * @param protocolStack
     * @return <tt>true</tt> if the consistency condition is satisfied.
     * @throws ProtocolException
     */
    synchronized public boolean addRoute(SessionId destination,
            SessionId route, ProtocolStack protocolStack)
            throws ProtocolException {
        /* check the consistency condition */
        if (protocolStack.getSession() != null
                && (destination
                        .equals(protocolStack.getSession().getLocalEnd()))
                || !route.equals(protocolStack.getSession().getRemoteEnd()))
            return false;

        stacks.put(destination, protocolStack);
        addProxy(route, destination);
        generateAddRouteEvent(destination, route);

        return true;
    }

    /**
     * Adds the association destination=protocolStack. It does not check for
     * consistency, since there's no danger here: this is called only when
     * establishing or accepting a new connection.
     * 
     * @param destination
     * @param protocolStack
     * @throws ProtocolException
     */
    synchronized public void addRoute(SessionId destination,
            ProtocolStack protocolStack) throws ProtocolException {
        if (protocolStack.getSession() == null)
            throw new ProtocolException("null session in ProtocolStack");

        stacks.put(destination, protocolStack);
        generateAddRouteEvent(destination, protocolStack.getSession()
                .getLocalEnd());
    }

    /**
     * Removes the route for the specified SessionId. It also removes the
     * association in the proxy table.
     * 
     * @param sessionId
     * @throws ProtocolException
     */
    public synchronized void removeRoute(SessionId sessionId)
            throws ProtocolException {
        ProtocolStack protocolStack = (stacks.remove(sessionId));
        if (protocolStack == null)
            return;

        SessionId proxy = protocolStack.getSession().getRemoteEnd();
        if (proxy != null) {
            generateRemoveRouteEvent(sessionId, proxy);
            TreeSet<SessionId> nodes = proxies.get(proxy);
            if (nodes != null) {
                nodes.remove(sessionId);
                if (nodes.size() == 0)
                    removeProxy(proxy);
            }
        } else {
            generateRemoveRouteEvent(sessionId, sessionId);
        }

        /* remove it also as a proxy if it was a proxy */
        removeProxy(sessionId);
    }

    synchronized public boolean hasRoute(SessionId loc) {
        return stacks.containsKey(loc);
    }

    synchronized public ProtocolStack getProtocolStack(SessionId l) {
        return stacks.get(l);
    }

    synchronized public Enumeration<SessionId> getLocalities() {
        return stacks.keys();
    }

    /**
     * The SessionId of the proxy with which we can reach the remote SessionId.
     * Or null if we cannot.
     * 
     * @param remote
     * @return The SessionId of the proxy with which we can reach the remote
     *         SessionId.
     * @throws ProtocolException
     */
    synchronized public SessionId getProxy(SessionId remote)
            throws ProtocolException {
        /* first check in the inverted proxies */
        ProtocolStack protocolStack = getProtocolStack(remote);

        /* this must not happen if the table is consistent */
        if (protocolStack == null)
            return null;

        if (protocolStack.getSession() == null)
            throw new ProtocolException("null session in ProtocolStack for "
                    + remote);

        return protocolStack.getSession().getRemoteEnd();
    }

    /**
     * Adds the proxy for the specified destination.
     * 
     * @param proxy
     * @param destination
     */
    synchronized protected void addProxy(SessionId proxy, SessionId destination) {
        TreeSet<SessionId> nodes = proxies.get(proxy);
        if (nodes == null) {
            nodes = new TreeSet<SessionId>();
            proxies.put(proxy, nodes);
        }

        nodes.add(destination);
    }

    /**
     * Removes the proxy and all the ids related to this proxy.
     * 
     * @param proxy
     */
    synchronized protected void removeProxy(SessionId proxy) {
        TreeSet<SessionId> nodes = proxies.remove(proxy);
        if (nodes == null)
            return;

        SessionId loc;
        Iterator<SessionId> subnodes = nodes.iterator();
        while (subnodes.hasNext()) {
            loc = subnodes.next();
            stacks.remove(loc);
            generateRemoveRouteEvent(loc, proxy);
        }
    }

    /**
     * Generates a RouteEvent about removing a route.
     * 
     * @param destination
     * @param route
     */
    void generateRemoveRouteEvent(SessionId destination, SessionId route) {
        generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
                RouteEvent.ROUTE_REMOVED, destination, route));
    }

    /**
     * Generates a RouteEvent about adding a route.
     * 
     * @param destination
     * @param route
     */
    void generateAddRouteEvent(SessionId destination, SessionId route) {
        generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
                RouteEvent.ROUTE_ADDED, destination, route));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "stacks: " + stacks + "\nproxies: " + proxies;
    }

    /**
     * Check the consistency condition.
     * 
     * @see org.mikado.imc.topology.RoutingTable#stacks
     * 
     * @return Whether the consistency condition is satisfied.
     * @throws ProtocolException
     */
    public synchronized boolean checkForConsistency() throws ProtocolException {
        Enumeration<SessionId> sessionIds = stacks.keys();

        while (sessionIds.hasMoreElements()) {
            SessionId sessionId = sessionIds.nextElement();
            ProtocolStack protocolStack = stacks.get(sessionId);

            if (sessionId.equals(protocolStack.getSession().getLocalEnd())) {
                System.err.println("non consistent entry: " + sessionId + "="
                        + protocolStack.getSession());

                return false;
            }
        }

        return true;
    }
}