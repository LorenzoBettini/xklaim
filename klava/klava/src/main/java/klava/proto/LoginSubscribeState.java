/*
 * Created on Jul 25, 2005
 *
 */
package klava.proto;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import klava.KlavaMalformedPhyLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.events.LoginSubscribeEvent;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.topology.ProtocolThread;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;


/**
 * The ProtocolState that tries to perform a login or subscribe
 * 
 * TODO rename it to LoginSubscribeState when everything is updated
 * 
 * @author bettini
 * 
 */
public class LoginSubscribeState extends ProtocolStateSimple {
    /**
     * The LogicalLocality with which we subscribe
     */
    protected LogicalLocality logicalLocality;

    /**
     * The PhysicalLocality with which we are logged at the remote site.
     */
    protected PhysicalLocality physicalLocality;

    /**
     * Whether the operation succeeded.
     */
    protected boolean success = false;

    /**
     * Contains the error description in case of failure
     */
    protected String error = "";

    /**
     * The RoutingTable used to keep tracks of routes.
     */
    protected RoutingTable routingTable = null;

    /**
     * The ProtocolFactory to create the message protocol.
     */
    protected MessageProtocolFactory messageProtocolFactory;

    /**
     * @param logicalLocality
     */
    public LoginSubscribeState(RoutingTable routingTable,
            MessageProtocolFactory messageProtocolFactory,
            LogicalLocality logicalLocality) {
        this(routingTable, messageProtocolFactory);
        this.logicalLocality = logicalLocality;
    }

    public LoginSubscribeState(RoutingTable routingTable,
            MessageProtocolFactory messageProtocolFactory) {
        this.routingTable = routingTable;
        this.messageProtocolFactory = messageProtocolFactory;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        boolean send_log_loc = logicalLocality != null;
        String proto_string;

        Marshaler marshaler = getMarshaler(transmissionChannel);

        if (send_log_loc) {
            proto_string = AcceptRegisterState.SUBSCRIBE_S;
        } else {
            proto_string = AcceptRegisterState.LOGIN_S;
        }

        try {
            marshaler.writeStringLine(proto_string);

            if (send_log_loc)
                marshaler.writeStringLine("" + logicalLocality);

            releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

            String result = unMarshaler.readStringLine();

            if (result.equals("OK")) {
                success = true;
            } else {
                success = false;
                error = unMarshaler.readStringLine();
            }

            /* updates the routing table with the route to the remote end */
            if (success) {
                try {
                    physicalLocality = new PhysicalLocality(unMarshaler
                            .readStringLine());
                } catch (KlavaMalformedPhyLocalityException e) {
                    throw new ProtocolException(e);
                }

                /* set the protocol layer for messages */
                protocolStack.insertFirstLayer(new MessageProtocolLayer());

                /*
                 * it is important to set the message layer in the stack as soon
                 * as possible, since the next actions, e.g., adding a route,
                 * can generate events that require to write something into the
                 * stack.
                 */

                routingTable.addRoute(getProtocolStack().getSession()
                        .getRemoteEnd(), getProtocolStack());

                /*
                 * spawns a parallel thread to deal with the new established
                 * communication
                 */
                Protocol protocol = messageProtocolFactory.createProtocol();

                protocol.setProtocolStack(protocolStack);
                ProtocolThread protocolThread = new ProtocolThread(protocol);

                protocolThread.start();

                /* generate the login/subscribe event */
                LoginSubscribeEvent loginSubscribeEvent = null;
                if (logicalLocality == null) {
                    loginSubscribeEvent = new LoginSubscribeEvent(this,
                            getSession());
                    generate((LoginSubscribeEvent.LOGIN_EVENT),
                            loginSubscribeEvent);
                } else {
                    loginSubscribeEvent = new LoginSubscribeEvent(this,
                            getSession(), logicalLocality);
                    generate((LoginSubscribeEvent.SUBSCRIBE_EVENT),
                            loginSubscribeEvent);
                }

                synchronized (routingTable) {
                    /* propagate the localities */
                    Enumeration<SessionId> enumeration = routingTable
                            .getLocalities();
                    Vector<SessionId> localities = new Vector<SessionId>();
                    while (enumeration.hasMoreElements())
                        localities.addElement(enumeration.nextElement());

                    PropagateLocalityState.propagateAddLocalities(
                            getProtocolStack(), localities);
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @return Returns the success.
     */
    public final boolean isSuccess() {
        return success;
    }

    /**
     * @return Returns the physicalLocality.
     */
    public final PhysicalLocality getPhysicalLocality() {
        return physicalLocality;
    }

    /**
     * @return Returns the error.
     */
    public String getError() {
        return error;
    }
}
