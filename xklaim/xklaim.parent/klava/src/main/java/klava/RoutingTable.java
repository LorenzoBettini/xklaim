package klava;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.mikado.imc.events.EventGeneratorAdapter;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;

/**
 * Store routes (ProtocolStack) for physical localities
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
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
     * This class takes care of avoiding such a situation. However, we provide
     * the method checkForConsistency() that can be helpful in case you redefine
     * some behaviors of this class.
     */
    protected Hashtable<SessionId, ProtocolStack> stacks = new Hashtable<SessionId, ProtocolStack>();

    /**
     * Nodes to which messages for other nodes are forwarded
     */
    protected Hashtable<SessionId, TreeSet<SessionId>> proxies = new Hashtable<SessionId, TreeSet<SessionId>>();

    /**
     * The inversion of proxies
     */
    protected Hashtable<SessionId, SessionId> inverted_proxies = new Hashtable<SessionId, SessionId>();

    /**
     * Adds the association loc=protocolStack. It also checks for the
     * consistency condition. If it is not satisfied it returns <tt>false</tt>
     * and does not add the entry.
     * 
     * @see klava.RoutingTable#stacks
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
    synchronized protected boolean addRoute(SessionId destination,
            SessionId route, ProtocolStack protocolStack)
            throws ProtocolException {
        /* check the consistency condition */
        if (protocolStack.getSession() != null
                && destination.equals(protocolStack.getSession().getLocalEnd()))
            return false;

        stacks.put(destination, protocolStack);
        generateAddRouteEvent(route, destination);

        return true;
    }

    synchronized public void addRoute(SessionId loc, ProtocolStack protocolStack)
            throws ProtocolException {
        addRoute(loc, loc, protocolStack);
    }

    public synchronized boolean addRoute(SessionId subloc, SessionId suploc)
            throws ProtocolException {
        ProtocolStack protocolStack = getProtocolStack(suploc);

        if (protocolStack == null)
            return false;

        if (addRoute(subloc, suploc, protocolStack))
            addProxy(suploc, subloc);

        return true;
    }

    public synchronized void removeRoute(SessionId loc) {
        ProtocolStack proxy = (stacks.remove(loc));
        if (proxy == null)
            return;

        SessionId reverse = inverted_proxies.get(loc);
        if (reverse != null) {
            generateRemoveRouteEvent(reverse, loc);
            TreeSet<SessionId> nodes = proxies.get(reverse);
            if (nodes != null)
                nodes.remove(loc);
        } else {
            generateRemoveRouteEvent(loc, loc);
        }

        removeProxy(loc);
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

    synchronized protected void addProxy(SessionId proxy, SessionId loc) {
        TreeSet<SessionId> nodes = proxies.get(proxy);
        if (nodes == null) {
            nodes = new TreeSet<SessionId>();
            proxies.put(proxy, nodes);
        }

        nodes.add(loc);
        generateAddProxyEvent(proxy, nodes);

        inverted_proxies.put(loc, proxy);
    }

    // remove all the localities connected to this proxy
    synchronized protected void removeProxy(SessionId proxy) {
        TreeSet<SessionId> nodes = proxies.remove(proxy);
        if (nodes == null)
            return;

        generateRemoveProxyEvent(proxy, nodes);

        SessionId loc;
        Iterator<SessionId> subnodes = nodes.iterator();
        while (subnodes.hasNext()) {
            loc = subnodes.next();
            stacks.remove(loc);
            generateRemoveRouteEvent(proxy, loc);
            inverted_proxies.remove(loc);
        }
    }

    void generateRemoveRouteEvent(SessionId loc2, SessionId loc) {
        generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
                RouteEvent.ROUTE_REMOVED, loc2, loc));
    }

    void generateRemoveProxyEvent(SessionId proxy, TreeSet nodes) {
        //generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
        //        RouteEvent.PROXY_REMOVED, proxy, nodes));
    }

    void generateAddRouteEvent(SessionId loc2, SessionId loc) {
        generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
                RouteEvent.ROUTE_ADDED, loc2, loc));
    }

    void generateAddProxyEvent(SessionId proxy, TreeSet nodes) {
        //generate(RouteEvent.ROUTE_EVENT, new RouteEvent(this,
        //        RouteEvent.PROXY_ADDED, proxy, nodes));
    }

    void closeStacks() {
        Enumeration<ProtocolStack> protocolStacks = stacks.elements();
        ProtocolStack protocolStack;

        while (protocolStacks.hasMoreElements()) {
            protocolStack = protocolStacks.nextElement();

            try {
                protocolStack.close();
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }
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
     * @see klava.RoutingTable#stacks
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