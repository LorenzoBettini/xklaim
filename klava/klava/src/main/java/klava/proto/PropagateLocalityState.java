/*
 * Created on Oct 17, 2005
 */
package klava.proto;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import klava.PhysicalLocality;
import klava.events.LocalityEvent;

import org.mikado.imc.events.EventGenerator;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.WrongStringProtocolException;
import org.mikado.imc.topology.RoutingTable;


/**
 * Handles propagation of locality operation, e.g., add and remove
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class PropagateLocalityState extends ProtocolStateSimple implements
        EventGenerator {

    /**
     * To routing table used to update localities routes
     */
    protected RoutingTable routingTable = new RoutingTable();

    /**
     * The constant string for a propagation
     */
    public static final String PROPAGATE_S = "PROPAGATE";

    /**
     * The constant string for adding a locality
     */
    public static final String ADDLOCALITY_S = "ADDLOCALITY";

    /**
     * The constant string for removing a locality
     */
    public static final String REMOVELOCALITY_S = "REMOVELOCALITY";

    /**
     * @param routingTable
     */
    public PropagateLocalityState(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    /**
     * The PROPAGATE string is assumed to be passed in the first param, or it is
     * read from the channel.
     * 
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        try {
            if (param == null)
                param = transmissionChannel.readStringLine();

            if (!param.equals(PROPAGATE_S))
                throw new WrongStringProtocolException(PROPAGATE_S, param
                        .toString());

            String op = transmissionChannel.readStringLine();

            if (op.equals(ADDLOCALITY_S)) {
                try {
                    int len = Integer.parseInt(transmissionChannel
                            .readStringLine());

                    /* this will be used to generate the event */
                    Vector<PhysicalLocality> physicalLocalities = new Vector<PhysicalLocality>();

                    synchronized (routingTable) {
                        for (int i = 1; i <= len; ++i) {
                            SessionId sessionId = SessionId
                                    .parseSessionId(transmissionChannel
                                            .readStringLine());

                            /*
                             * we update the routing table and insert it in the
                             * event only if we don't have a route for a
                             * SessionId
                             */
                            if (!routingTable.hasRoute(sessionId)) {
                                /* we use as the proxy for the destination the one
                                 * we received this message from */
                                routingTable.addRoute(sessionId,
                                        getProtocolStack().getSession()
                                                .getRemoteEnd(),
                                        getProtocolStack());

                                physicalLocalities.add(new PhysicalLocality(
                                        sessionId));
                            }
                        }
                    }

                    // generate the event
                    generate(LocalityEvent.ADDLOCALITY_EVENT,
                            new LocalityEvent(this, physicalLocalities, true));
                } catch (NumberFormatException e) {
                    throw new ProtocolException(e);
                }
            } else if (op.equals(REMOVELOCALITY_S)) {
                SessionId toRemove = SessionId
                        .parseSessionId(transmissionChannel.readStringLine());

                synchronized (routingTable) {
                    /*
                     * we update the routing table and insert it in the event
                     * only if we have a route for a SessionId
                     */
                    if (routingTable.hasRoute(toRemove)) {
                        routingTable.removeRoute(toRemove);

                        Vector<PhysicalLocality> physicalLocalities = new Vector<PhysicalLocality>();
                        physicalLocalities.add(new PhysicalLocality(toRemove));

                        // generate the event
                        generate(LocalityEvent.REMOVELOCALITY_EVENT,
                                new LocalityEvent(this, physicalLocalities,
                                        false));
                    }
                }
            } else {
                throw new WrongStringProtocolException(ADDLOCALITY_S + " or "
                        + REMOVELOCALITY_S, op);
            }

        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Propagates the added localities using the passed ProtocolStack.
     * 
     * Packet:
     * 
     * <tt>
     * PROPAGATE_S
     * ADDLOCALITY_S
     * number of localities
     * localities
     * </tt>
     * 
     * @param protocolStack
     *            The stack for propagating localities
     * @param sessionIds
     *            The localities to propagate
     * @throws ProtocolException
     * @throws IOException
     */
    static public void propagateAddLocalities(ProtocolStack protocolStack,
            Vector<SessionId> sessionIds)
            throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();

        marshaler.writeStringLine(PROPAGATE_S);
        marshaler.writeStringLine(ADDLOCALITY_S);

        marshaler.writeStringLine("" + sessionIds.size());

        Iterator<SessionId> iterator = sessionIds.iterator();
        while (iterator.hasNext()) {
            marshaler.writeStringLine(iterator.next().toString());
        }

        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * Propagates the removed locality using the passed ProtocolStack.
     * 
     * Packet:
     * 
     * <tt>
     * PROPAGATE_S
     * REMOVELOCALITY_S
     * locality to remove
     * </tt>
     * 
     * @param protocolStack
     * @param sessionId
     * @throws ProtocolException
     * @throws IOException
     */
    static public void propagateRemoveLocalities(ProtocolStack protocolStack,
            SessionId sessionId) throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();

        marshaler.writeStringLine(PROPAGATE_S);
        marshaler.writeStringLine(REMOVELOCALITY_S);

        marshaler.writeStringLine(sessionId.toString());

        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * @return Returns the routingTable.
     */
    public final RoutingTable getRoutingTable() {
        return routingTable;
    }

    /**
     * @param routingTable
     *            The routingTable to set.
     */
    public final void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }
}
