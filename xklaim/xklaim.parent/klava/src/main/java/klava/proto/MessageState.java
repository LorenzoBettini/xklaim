/*
 * Created on Nov 3, 2005
 */
package klava.proto;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import klava.Environment;
import klava.LogicalLocality;
import klava.LogicalLocalityResolver;
import klava.PhysicalLocality;
import klava.WaitingForResponse;
import klava.events.LoginSubscribeEvent;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManagers;

/**
 * The main message state. After reading the string that identifies a message,
 * it enters the corresponding state.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class MessageState extends ProtocolStateSimple {

    /**
     * The actual state that enters a specific state depending on the
     * identifying string.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class MessageStateSwitch extends ProtocolSwitchState {

        /**
         * 
         */
        public MessageStateSwitch() {
            super();
            init();
        }

        /**
         * @param next_state
         */
        public MessageStateSwitch(String next_state) {
            super(next_state);
            init();
        }

        void init() {
            /* A TupleOpState used for reading tuple operation requests */
            addRequestState(TupleOpState.OPERATION_S, new TupleOpState(
                    getTupleOpManager()));

            /* To deal with responses */
            ResponseState responseState = new ResponseState();
            responseState.setWaitingForResponse(waitingForResponse);
            responseState.setWaitingForTuple(waitingForTuple);
            responseState.setWaitingForLocality(waitingForLocality);
            responseState.setRoutingTable(routingTable);
            responseState
                    .setSessionManager(sessionManagers.outgoingSessionManager);
            addRequestState(ResponseState.RESPONSE_S, responseState);

            /* to deal with locality requests */
            LocalityResolverState localityResolverState = new LocalityResolverState(
                    logicalLocalityResolver);
            addRequestState(LocalityResolverState.RESOLVELOC_S,
                    localityResolverState);

            /* to deal with locality propagation */
            PropagateLocalityState propagateLocalityState = new PropagateLocalityState(
                    routingTable);
            addRequestState(PropagateLocalityState.PROPAGATE_S,
                    propagateLocalityState);

            /* To deal with logout and unsubscribe */
            AcceptRegisterState unsubscribeState = new AcceptRegisterState(
                    routingTable, environment, new PhysicalLocality(),
                    new LogicalLocality(), null);
            unsubscribeState.setLogoutUnsubscribe(true);
            /*
             * notice that we don't care about the physical and logical
             * locality: events will be generated concerning a logout and
             * unsubscribe
             */
            AcceptRegisterState logoutState = new AcceptRegisterState(
                    routingTable, new PhysicalLocality(), null);
            logoutState.setLogoutUnsubscribe(true);

            addRequestState(AcceptRegisterState.LOGOUT_S, logoutState);
            addRequestState(AcceptRegisterState.UNSUBSCRIBE_S, unsubscribeState);

            /* for route finding */
            addRequestState(RouteFinderState.HASROUTE_S, new RouteFinderState(
                    routingTable, sessionManagers.outgoingSessionManager,
                    waitingForResponse));
        }

    }

    public static final String MESSAGE_BEGIN = "MESSAGE";

    public static final String MESSAGE_END = "END MESSAGE";

    /**
     * The manager of a received TuplePacket.
     */
    TupleOpManager tupleOpManager = null;

    /**
     * The table that associates process names to generic responses.
     */
    WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();

    /**
     * The table that associates process names to tuple responses.
     */
    WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();

    /**
     * The table that associates process names to locality responses.
     */
    WaitingForResponse<Response<PhysicalLocality>> waitingForLocality = new WaitingForResponse<Response<PhysicalLocality>>();

    /**
     * The RoutingTable
     */
    RoutingTable routingTable = null;

    /**
     * The Environment used to keep track of locality mappings
     */
    Environment environment = null;

    /**
     * The SessionManager containing the established sessions, i.e., those
     * created by a login.
     */
    SessionManagers sessionManagers = new SessionManagers();

    /**
     * The resolver of LogicalLocalities.
     */
    LogicalLocalityResolver logicalLocalityResolver;

    /**
     * This is used to dispatch and retrieve mobile code.
     */
    MigratingCodeFactory migratingCodeFactory;

    /**
     * The switch for each message type
     */
    MessageStateSwitch messageStateSwitch;

    /**
     * 
     */
    public MessageState() {
        super();
    }

    /**
     * @param routingTable
     * @param environment
     * @param sessionManagers
     * @param logicalLocalityResolver
     * @param migratingCodeFactory
     *            used to dispatch and retrieve mobile code.
     * @param processContainer
     */
    public MessageState(RoutingTable routingTable, Environment environment,
            SessionManagers sessionManagers,
            LogicalLocalityResolver logicalLocalityResolver,
            MigratingCodeFactory migratingCodeFactory) {
        this.routingTable = routingTable;
        this.environment = environment;
        this.sessionManagers = sessionManagers;
        this.logicalLocalityResolver = logicalLocalityResolver;
        this.migratingCodeFactory = migratingCodeFactory;

        TupleOpState.setMigratingCodeFactory(migratingCodeFactory);
    }

    /**
     * Reads the MESSAGE begin string, enters the MessageStateSwitch and then
     * reads the END MESSAGE string
     * 
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

            /* create it only the first time */
            if (messageStateSwitch == null) {
                messageStateSwitch = new MessageStateSwitch();
                messageStateSwitch.setProtocolStack(getProtocolStack());
                messageStateSwitch.setEventManager(getEventManager());
            }

            messageStateSwitch
                    .enter(null, new TransmissionChannel(unMarshaler));
        } catch (ProtocolException e) {
            printSession();
            e.printStackTrace();
            lostConnection();
            throw e;
            /* something bad happened so we end the protocol */
        }
    }

    /**
     * Remove the connection information both from the RoutingTable and from the
     * Environment (so that they are kept consistent) and also generates the
     * events concerning this lost connection.
     */
    protected void lostConnection() {
        ProtocolStack protocolStack = getProtocolStack();
        try {
            /* something happened before the actual communication */
            if (protocolStack == null || protocolStack.getSession() == null)
                return;

            /* remove the Session */
            sessionManagers.removeSession(protocolStack.getSession());

            /* remove the id of the lost session from the RoutingTable */
            SessionId sessionId = protocolStack.getSession().getRemoteEnd();
            routingTable.removeRoute(sessionId);

            /* generate the corresponding event */
            generate(LoginSubscribeEvent.LOGOUT_EVENT, new LoginSubscribeEvent(
                    this, protocolStack.getSession()));

            /* now update the Environment */
            PhysicalLocality physicalLocality = new PhysicalLocality(sessionId);
            /*
             * get the possible LogicalLocalities associated with this
             * PhysicalLocality
             */
            HashSet<LogicalLocality> logicalLocalities = environment
                    .toLogical(physicalLocality);
            if (logicalLocalities != null) {
                LogicalLocality logicalLocality;
                Iterator<LogicalLocality> iterator = logicalLocalities
                        .iterator();
                while (iterator.hasNext()) {
                    /* and remove it one by one */
                    logicalLocality = iterator.next();
                    environment.remove(logicalLocality);
                    /* also generate a synthetized unsubscribe event */
                    generate(LoginSubscribeEvent.UNSUBSCRIBE_EVENT,
                            new LoginSubscribeEvent(this, protocolStack
                                    .getSession(), logicalLocality));
                }
            }
        } catch (ProtocolException e) {
            return;
        }
    }

    /**
     * Communicates to the other end that there was an error in protocol: a
     * string was expected but another one was received. All the information is
     * contained in the passed WrongStringProtocolException.
     * 
     * @param wrongStringProtocolException
     * @param transmissionChannel
     * @throws ProtocolException
     * @throws IOException
     */
    public void communicateWrongString(
            WrongStringProtocolException wrongStringProtocolException,
            TransmissionChannel transmissionChannel) throws ProtocolException,
            IOException {
        Marshaler marshaler = getMarshaler(transmissionChannel);

        marshaler.writeStringLine(wrongStringProtocolException.toString());

        releaseMarshaler(marshaler);
    }

    /**
     * Prints the session into the standard error. This is useful for printing
     * errors and to see who's printing an error.
     */
    protected void printSession() {
        try {
            System.err.print(getProtocolStack().getSession().toString() + ": ");
        } catch (NullPointerException e) {
            /* we print nothing in this case */
        } catch (ProtocolException e) {
            /* we print nothing in this case */
        }
    }

    /**
     * On closing we make sure to delegate closed to the MessageStateSwitch.
     * 
     * @see org.mikado.imc.protocols.ProtocolSwitchState#closed()
     */
    @Override
    public void closed() throws ProtocolException {
        super.closed();
        try {
            if (messageStateSwitch != null)
                messageStateSwitch.closed();
        } catch (IMCException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @return Returns the tupleOpManager.
     */
    public final TupleOpManager getTupleOpManager() {
        return tupleOpManager;
    }

    /**
     * @param tupleOpManager
     *            The tupleOpManager to set.
     */
    public final void setTupleOpManager(TupleOpManager tupleOpManager) {
        this.tupleOpManager = tupleOpManager;
    }

    /**
     * @return Returns the waitingForResponse.
     */
    public final WaitingForResponse<Response<String>> getWaitingForResponse() {
        return waitingForResponse;
    }

    /**
     * @param waitingForResponse
     *            The waitingForResponse to set.
     */
    public final void setWaitingForResponse(
            WaitingForResponse<Response<String>> waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }

    /**
     * @return Returns the waitingForTuple.
     */
    public final WaitingForResponse<TupleResponse> getWaitingForTuple() {
        return waitingForTuple;
    }

    /**
     * @param waitingForTuple
     *            The waitingForTuple to set.
     */
    public final void setWaitingForTuple(
            WaitingForResponse<TupleResponse> waitingForTuple) {
        this.waitingForTuple = waitingForTuple;
    }

    /**
     * @return Returns the waitingForLocality.
     */
    public final WaitingForResponse<Response<PhysicalLocality>> getWaitingForLocality() {
        return waitingForLocality;
    }

    /**
     * @param waitingForLocality
     *            The waitingForLocality to set.
     */
    public final void setWaitingForLocality(
            WaitingForResponse<Response<PhysicalLocality>> waitingForLocality) {
        this.waitingForLocality = waitingForLocality;
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

    /**
     * @return Returns the environment.
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @param environment
     *            The environment to set.
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
