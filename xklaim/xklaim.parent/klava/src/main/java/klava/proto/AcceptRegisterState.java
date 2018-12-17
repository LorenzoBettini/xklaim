/*
 * Created on Jul 25, 2005
 *
 */
package klava.proto;

import java.io.IOException;

import klava.Environment;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.events.LoginSubscribeEvent;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;
import org.mikado.imc.topology.ProtocolThread;
import org.mikado.imc.topology.RoutingTable;


/**
 * The ProtocolState that handles a login/logout or subscribe/unsubscribe
 * request.
 * 
 * TODO rename it to AcceptRegisterState when everything is updated
 * 
 * @author bettini
 * 
 */
public class AcceptRegisterState extends ProtocolStateSimple {
    /**
     * The RoutingTable used to keep tracks of routes.
     */
    protected RoutingTable routingTable = null;

    /**
     * The Environment used to keep track of locality mappings
     */
    protected Environment environment = null;

    /**
     * The PhysicalLocality to set when the login is success.
     */
    protected PhysicalLocality physicalLocality;

    /**
     * The LogicalLocality to set when the login is success.
     */
    protected LogicalLocality logicalLocality;

    /**
     * Whether the request was handled successfully.
     */
    protected boolean success = false;

    /**
     * Whether we wait for a logout/unsubscribe
     */
    protected boolean logoutUnsubscribe = false;

    /**
     * The ProtocolFactory to create the message protocol.
     */
    protected MessageProtocolFactory messageProtocolFactory;

    public final static String LOGIN_S = "LOGIN";

    public final static String LOGOUT_S = "LOGOUT";

    public final static String SUBSCRIBE_S = "SUBSCRIBE";

    public final static String UNSUBSCRIBE_S = "UNSUBSCRIBE";

    /**
     * @param routingTable
     * @param environment
     * @param physicalLocality
     * @param logicalLocality
     * @param messageProtocolFactory
     *            The factory to create the message protocol
     */
    public AcceptRegisterState(RoutingTable routingTable,
            Environment environment, PhysicalLocality physicalLocality,
            LogicalLocality logicalLocality,
            MessageProtocolFactory messageProtocolFactory) {
        this.routingTable = routingTable;
        this.environment = environment;
        this.physicalLocality = physicalLocality;
        this.logicalLocality = logicalLocality;
        this.messageProtocolFactory = messageProtocolFactory;
    }

    /**
     * @param routingTable
     * @param physicalLocality
     * @param messageProtocolFactory
     *            The factory to create the message protocol
     */
    public AcceptRegisterState(RoutingTable routingTable,
            PhysicalLocality physicalLocality,
            MessageProtocolFactory messageProtocolFactory) {
        this.routingTable = routingTable;
        this.physicalLocality = physicalLocality;
        this.messageProtocolFactory = messageProtocolFactory;
    }

    /**
     * @param next_state
     */
    public AcceptRegisterState(String next_state) {
        super(next_state);
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        PhysicalLocality readPhysicalLocality = null;
        String error = "";

        try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            String next_token = null;
            if (param != null)
                next_token = param.toString();
            else
                next_token = unMarshaler.readStringLine();

            if (logoutUnsubscribe) {
                if (!next_token.equals(LOGOUT_S)
                        && !next_token.equals(UNSUBSCRIBE_S)) {
                    error = WrongStringProtocolException.format(LOGOUT_S
                            + " or " + UNSUBSCRIBE_S, next_token);
                }
            } else {
                if (!next_token.equals(LOGIN_S)
                        && !next_token.equals(SUBSCRIBE_S)) {
                    error = WrongStringProtocolException.format(LOGIN_S
                            + " or " + SUBSCRIBE_S, next_token);
                }
            }

            /* if there was an error, communicate and exit */
            if (error.length() > 0) {
                Marshaler marshaler = getMarshaler(transmissionChannel);
                marshaler.writeStringLine(error);
                releaseMarshaler(marshaler);
                setNextState(Protocol.END);
                return;
            }

            if ((next_token.equals(LOGIN_S) || next_token.equals(LOGOUT_S))
                    && logicalLocality != null) {
                Marshaler marshaler = getMarshaler(transmissionChannel);
                marshaler
                        .writeStringLine("use SUBSCRIBE protocol, not LOGIN protocol");
                releaseMarshaler(marshaler);
                setNextState(Protocol.END);
                return;
            }

            /* construct remote physical locality */
            readPhysicalLocality = new PhysicalLocality(getProtocolStack()
                    .getSession().getRemoteEnd());

            synchronized (routingTable) {
                LogicalLocality log_loc = null;

                /* now check whether the physical locality can be handled */
                if (logoutUnsubscribe) {
                    success = routingTable.hasRoute(readPhysicalLocality
                            .getSessionId());

                    if (!success)
                        System.err.println("Physical Locality "
                                + readPhysicalLocality + " not present!");
                } else {
                    success = !routingTable.hasRoute(readPhysicalLocality
                            .getSessionId());

                    if (!success) {
                        error = "Physical Locality " + readPhysicalLocality
                                + " already present!";
                        System.err.println(error);
                    }
                }

                if (logicalLocality != null) {
                    /* read logical locality */
                    log_loc = new LogicalLocality(unMarshaler.readStringLine());

                    if (logoutUnsubscribe) {
                        success = (environment.remove(log_loc) != null);

                        if (!success)
                            System.err.println("Logical Locality " + log_loc
                                    + " not present!");
                    } else {
                        success = environment.try_add(log_loc,
                                readPhysicalLocality);

                        if (!success) {
                            error = "Logical Locality " + log_loc
                                    + " already present!";
                            System.err.println(error);
                        }
                    }
                }

                if (success) {
                    if (logoutUnsubscribe) {
                        routingTable.removeRoute(readPhysicalLocality
                                .getSessionId());
                    } else {
                        routingTable.addRoute(readPhysicalLocality
                                .getSessionId(), getProtocolStack());
                    }

                    LoginSubscribeEvent loginSubscribeEvent = null;
                    if (logicalLocality == null) {
                        loginSubscribeEvent = new LoginSubscribeEvent(this,
                                getSession());
                        loginSubscribeEvent.removed = logoutUnsubscribe;
                        generate(
                                (logoutUnsubscribe ? LoginSubscribeEvent.LOGOUT_EVENT
                                        : LoginSubscribeEvent.LOGIN_EVENT),
                                loginSubscribeEvent);
                    } else {
                        loginSubscribeEvent = new LoginSubscribeEvent(this,
                                getSession(), log_loc);
                        loginSubscribeEvent.removed = logoutUnsubscribe;
                        generate(
                                (logoutUnsubscribe ? LoginSubscribeEvent.UNSUBSCRIBE_EVENT
                                        : LoginSubscribeEvent.SUBSCRIBE_EVENT),
                                loginSubscribeEvent);
                    }

                    physicalLocality.setValue(readPhysicalLocality);
                    if (logicalLocality != null)
                        logicalLocality.setValue(log_loc);

                }
            }

            /* write something back only if it's an accept or register */
            if (!logoutUnsubscribe) {
                Marshaler marshaler = getMarshaler(transmissionChannel);
                if (success) {
                    marshaler.writeStringLine("OK");
                    marshaler.writeStringLine("" + readPhysicalLocality);
                } else {
                    marshaler.writeStringLine("FAIL");
                    marshaler.writeStringLine(error);
                }
                releaseMarshaler(marshaler);
            }

            if (!logoutUnsubscribe && success) {
                /*
                 * spawns a parallel thread to deal with the new established
                 * communication
                 */
                Protocol protocol = messageProtocolFactory.createProtocol();

                /* set the protocol layer for messages */
                protocolStack.insertFirstLayer(new MessageProtocolLayer());

                protocol.setProtocolStack(protocolStack);
                ProtocolThread protocolThread = new ProtocolThread(protocol);

                protocolThread.start();
            }

        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        if (!success)
            setNextState(Protocol.END);
    }

    /**
     * @return Returns the success.
     */
    public final boolean isSuccess() {
        return success;
    }

    /**
     * @return Returns the logoutUnsubscribe.
     */
    public final boolean isLogoutUnsubscribe() {
        return logoutUnsubscribe;
    }

    /**
     * @param logoutUnsubscribe
     *            The logoutUnsubscribe to set.
     */
    public final void setLogoutUnsubscribe(boolean logoutUnsubscribe) {
        this.logoutUnsubscribe = logoutUnsubscribe;
    }

}
