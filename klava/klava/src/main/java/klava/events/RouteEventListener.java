/*
 * Created on Dec 5, 2005
 */
package klava.events;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import klava.proto.PropagateLocalityState;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.topology.SessionManager;

/**
 * Waits for RouteEvent and propagates the operation to all the nodes it is
 * connected to.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class RouteEventListener implements EventListener {
    SessionManager outgoingSessions;

    /**
     * @param outgoingSessions
     */
    public RouteEventListener(SessionManager outgoingSessions) {
        this.outgoingSessions = outgoingSessions;
    }

    /**
     * propagates the operation to all the nodes it is connected to.
     * 
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        RouteEvent routeEvent = (RouteEvent) event;

        /* to propagate added localities */
        Vector<SessionId> localities = null;

        /* the nodes to propagate to */
        Enumeration<ProtocolStack> stacks = outgoingSessions.getStacks();

        while (stacks.hasMoreElements()) {
            ProtocolStack protocolStack = stacks.nextElement();

            /*
             * we ignore these exceptions: if a connection fails, it is handled
             * somewhere else, not here.
             */
            try {
                /*
                 * we must avoid to send useless messages (in particular when
                 * the node logs to itself). The following check avoids this
                 */
                Session session = protocolStack.getSession();
                if (session != null) {
                    /*
                     * this means that we are telling someone that to reach a
                     * destination it must pass through itself: it's useless
                     * (and this happens, e.g., if the node connects to itself)
                     */
                    if (session.getRemoteEnd().equals(routeEvent.route))
                        continue;
                }

                if (routeEvent.type == RouteEvent.ROUTE_ADDED) {
                    localities = new Vector<SessionId>();
                    localities.addElement(routeEvent.destination);
                    PropagateLocalityState.propagateAddLocalities(
                            protocolStack, localities);
                } else if (routeEvent.type == RouteEvent.ROUTE_REMOVED) {
                    PropagateLocalityState.propagateRemoveLocalities(
                            protocolStack, routeEvent.destination);
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
