/*
 * Created on Nov 3, 2005
 */
package klava.topology;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

import klava.Environment;
import klava.EnvironmentLogicalLocalityResolver;
import klava.KlavaConnectionException;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.KlavaNoDirectCommunicationsException;
import klava.KlavaPhysicalLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.LogicalLocalityResolver;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpace;
import klava.TupleSpaceVector;
import klava.WaitingForResponse;
import klava.events.LoginSubscribeEvent;
import klava.events.RouteEventListener;
import klava.proto.ExecutionEngine;
import klava.proto.AcceptRegisterState;
import klava.proto.LoginSubscribeState;
import klava.proto.MessageProtocolFactory;
import klava.proto.Response;
import klava.proto.ResponseState;
import klava.proto.TupleOpManagerFactory;
import klava.proto.TupleOpState;
import klava.proto.TuplePacket;
import klava.proto.TupleResponse;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.RouteEvent;
import org.mikado.imc.events.WaitForEventListener;
import org.mikado.imc.events.WaitForEventQueueListener;
import org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactory;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.pipe.LocalSessionStarter;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.NodeCoordinatorProxy;
import org.mikado.imc.topology.NodeLocation;
import org.mikado.imc.topology.NodeProcessProxy;
import org.mikado.imc.topology.RoutingTable;

/**
 * A generic klava Node
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.11 $
 */
public class KlavaNode extends Node {

    /**
     * The tuple space of this node.
     */
    protected TupleSpace tupleSpace = new TupleSpaceVector();

    /**
     * The RoutingTable
     */
    protected RoutingTable routingTable = new RoutingTable();

    /**
     * The table that associates process names to generic responses.
     */
    protected WaitingForResponse<Response<String>> waitingForOkResponse = new WaitingForResponse<Response<String>>();

    /**
     * The table that associates process names to tuple responses.
     */
    protected WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();

    /**
     * The table that associates process names to locality responses.
     */
    protected WaitingForResponse<Response<PhysicalLocality>> waitingForLocality = new WaitingForResponse<Response<PhysicalLocality>>();

    /**
     * The factory of managers of a received TuplePacket.
     */
    protected TupleOpManagerFactory tupleOpManagerFactory = null;

    /**
     * The factory used to create a Message Protocol.
     */
    protected MessageProtocolFactory messageProtocolFactory = null;

    /**
     * The Environment used to keep track of locality mappings
     */
    protected Environment environment = new Environment();

    /**
     * The resolver of LogicalLocalities.
     */
    protected LogicalLocalityResolver logicalLocalityResolver;

    /**
     * This is used to perform closures of tuples and processes.
     */
    protected ClosureMaker closureMaker;

    /**
     * The listener for logout events.
     */
    protected WaitForEventListener waitForLogoutEvents;

    /**
     * The listener for unsubscribe events.
     */
    protected WaitForEventListener waitForUnsusbscribeEvents;

    /**
     * If set, this is used as the default PhysicalLocality for this node, i.e.,
     * for translating self, or for accepting new connections (when the
     * PhysicalLocality is not specified).
     */
    protected PhysicalLocality mainPhysicalLocality = null;

    /**
     * This is used to dispatch and retrieve mobile code.
     */
    protected MigratingCodeFactory migratingCodeFactory;

    /**
     * The ExecutionEngine for processes received from remote sites
     */
    protected ExecutionEngine executionEngine;

    /**
     * These are the nodes that are created by this very node, that will die
     * together with this node.
     */
    protected Vector<KlavaNode> newNodes = new Vector<KlavaNode>();

    /**
     * The logical locality self representing this very node.
     */
    public final static LogicalLocality self = new LogicalLocality("self");

    public KlavaNode() {
        initNode();
    }

    /**
     * Initializes the node with a custom TupleSpace
     * 
     * @param tupleSpace
     */
    public KlavaNode(TupleSpace tupleSpace) {
        this.tupleSpace = tupleSpace;
        initNode();
    }

    /**
     * Performs initializations for this KlavaNode
     */
    protected void initNode() {
        executionEngine = createExecutionEngine();
        tupleOpManagerFactory = new TupleOpManagerFactory(tupleSpace,
                routingTable, waitingForOkResponse, executionEngine,
                getSessionManagers());

        logicalLocalityResolver = new EnvironmentLogicalLocalityResolver(
                environment, sessionManagers.outgoingSessionManager,
                waitingForLocality);
        routingTable.setEventManager(getEventManager());
        environment.setEventManager(getEventManager());
        tupleSpace.setEventManager(getEventManager());
        messageProtocolFactory = createMessageProtocolFactory();

        closureMaker = createClosureMaker();

        /*
         * sets the listener of RouteEvents, in order to propagate a locality
         * (added or removed) to all the nodes this node is connected to (only
         * the ones it is logged/subscribed)
         */
        getEventManager().addListener(RouteEvent.ROUTE_EVENT,
                new RouteEventListener(sessionManagers.outgoingSessionManager));

        /*
         * we use a queue for 20 events which should be enough not to lose
         * events and not to use too much memory. TODO: allow to specify the
         * size
         */
        waitForLogoutEvents = new WaitForEventQueueListener(20);
        waitForUnsusbscribeEvents = new WaitForEventQueueListener(20);

        getEventManager().addListener(LoginSubscribeEvent.LOGOUT_EVENT,
                waitForLogoutEvents);
        getEventManager().addListener(LoginSubscribeEvent.UNSUBSCRIBE_EVENT,
                waitForUnsusbscribeEvents);
    }

    /**
     * Factory method that creates and initializes a MessageProtocolFactory
     * 
     * @return the created MessageProtocolFactory
     */
    protected MessageProtocolFactory createMessageProtocolFactory() {
        return new MessageProtocolFactory(waitingForOkResponse,
                waitingForTuple, waitingForLocality, tupleOpManagerFactory,
                routingTable, environment, getEventManager(), sessionManagers,
                logicalLocalityResolver, createMigratingCodeFactory());
    }

    /**
     * Factory method that creates the MigratingCodeFactory. By default it
     * creates a JavaByteCodeMigratingCodeFactory.
     * 
     * @return the created MigratingCodeFactory
     */
    protected MigratingCodeFactory createMigratingCodeFactory() {
        return new JavaByteCodeMigratingCodeFactory();
    }

    /**
     * Factory method that creates the ClosureMaker.
     * 
     * @return the created ClosureMaker
     */
    protected ClosureMaker createClosureMaker() {
        return new ClosureMaker(logicalLocalityResolver, environment);
    }

    /**
     * Factory method that creates the ExecutionEngine. By default it creates a
     * NodeExecutionEngine.
     * 
     * @return the created ExecutionEngine
     */
    protected ExecutionEngine createExecutionEngine() {
        return new NodeExecutionEngine(this);
    }

    /**
     * By default a node accepts remote processes for executing them locally.
     * This methods permits specifying whether we want to refuse them.
     * 
     * @param refuse
     */
    public void refuseRemoteProcesses(boolean refuse) {
        executionEngine.acceptRemoteProcesses(!refuse);
    }

    /**
     * Accepts a login request. It listens for requests on the local
     * PhysicalLocality and if it accepts a login request, it stores into remote
     * PhysicalLocality the PhysicalLocality of the remote end.
     * 
     * @param local
     *            PhysicalLocality used for listening for login requests
     * @param remote
     *            PhysicalLocality of the remote end that will be set if login
     *            succeeds
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean accept(PhysicalLocality local, PhysicalLocality remote)
            throws KlavaException {
        /* the accept protocol state */
        AcceptRegisterState acceptRegisterState = new AcceptRegisterState(
                routingTable, remote, getMessageProtocolFactory());

        return handleIncoming(local, acceptRegisterState);
    }

    /**
     * Accepts a login request. It listens for requests on the main
     * PhysicalLocality (if this is not set, it throws an exception) and if it
     * accepts a login request, it stores into remote PhysicalLocality the
     * PhysicalLocality of the remote end.
     * 
     * @param remote
     *            PhysicalLocality of the remote end that will be set if login
     *            succeeds
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean accept(PhysicalLocality remote) throws KlavaException {
        if (mainPhysicalLocality == null)
            throw new KlavaPhysicalLocalityException(
                    "main physical locality not set");

        /* the accept protocol state */
        AcceptRegisterState acceptRegisterState = new AcceptRegisterState(
                routingTable, remote, getMessageProtocolFactory());

        return handleIncoming(mainPhysicalLocality, acceptRegisterState);
    }

    /**
     * Accepts a subscribe request. It listens for requests on the local
     * PhysicalLocality and if it accepts a subscribe request, it stores into
     * remote PhysicalLocality the PhysicalLocality of the remote end and into
     * logical LogicalLocality the LogicalLocality of the remote end.
     * 
     * @param local
     *            PhysicalLocality used for listening for login requests
     * @param remote
     *            PhysicalLocality of the remote end that will be set if
     *            subscribe succeeds
     * @param logical
     *            LogicalLocality of the remote end that will be set if
     *            subscribe succeeds
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean register(PhysicalLocality local, PhysicalLocality remote,
            LogicalLocality logical) throws KlavaException {
        /* the register protocol state */
        AcceptRegisterState acceptRegisterState = new AcceptRegisterState(
                routingTable, environment, remote, logical,
                getMessageProtocolFactory());

        return handleIncoming(local, acceptRegisterState);
    }

    /**
     * Accepts a subscribe request. It listens for requests on on the main
     * PhysicalLocality (if this is not set, it throws an exception) and if it
     * accepts a subscribe request, it stores into remote PhysicalLocality the
     * PhysicalLocality of the remote end and into logical LogicalLocality the
     * LogicalLocality of the remote end.
     * 
     * @param remote
     *            PhysicalLocality of the remote end that will be set if
     *            subscribe succeeds
     * @param logical
     *            LogicalLocality of the remote end that will be set if
     *            subscribe succeeds
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean register(PhysicalLocality remote, LogicalLocality logical)
            throws KlavaException {
        if (mainPhysicalLocality == null)
            throw new KlavaPhysicalLocalityException(
                    "main physical locality not set");

        /* the register protocol state */
        AcceptRegisterState acceptRegisterState = new AcceptRegisterState(
                routingTable, environment, remote, logical,
                getMessageProtocolFactory());

        return handleIncoming(mainPhysicalLocality, acceptRegisterState);
    }

    /**
     * Handles an incoming communication request, by using the passed
     * ProtocolState.
     * 
     * Upon returning it is not known whether it succeeded or not, thus who
     * calls this method must have a way to check this.
     * 
     * @param local
     *            PhysicalLocality used for listening for incoming requests
     * @param protocolState
     *            The ProtocolState used to handle the incoming communication
     *            request
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    protected boolean handleIncoming(PhysicalLocality local,
            AcceptRegisterState protocolState) throws KlavaException {
        ProtocolStack protocolStack = null;
        try {
            /* wait for a physical connection (Session) */
            protocolStack = accept(local.getSessionId());

            /*
             * set the event manager so that events will be routed through this
             * node event manager
             */
            protocolState.setEventManager(getEventManager());

            /* start the incoming communication protocol */
            Protocol protocol = new Protocol(protocolState);
            protocol.setProtocolStack(protocolStack);
            protocol.start();

            /*
             * something went wrong so we must make sure to close the stack
             */
            if (!protocolState.isSuccess()) {
                /* be sure to update also the session manager */
                sessionManagers.incomingSessionManager
                        .removeSession(protocolStack.getSession());
                protocol.close();
            }

            return protocolState.isSuccess();
        } catch (ProtocolException e) {
            try {
                if (protocolStack != null)
                    protocolStack.close();
            } catch (ProtocolException e1) {
                /* ignore */
            }
            throw new KlavaConnectionException(e);
        }
    }

    /**
     * Performs a communication request, by using the passed ProtocolState.
     * 
     * Upon returning it is not known whether it succeeded or not, thus who
     * calls this method must have a way to check this.
     * 
     * @param remote
     *            Locality to issue the communication request
     * @param protocolState
     *            The ProtocolState used to handle the outgoing communication
     *            request
     * @return The result of the operation
     * @throws KlavaException
     */
    protected boolean handleOutgoing(Locality remote,
            LoginSubscribeState protocolState) throws KlavaException {
        ProtocolStack protocolStack = null;
        try {
            /* first translate a possible LogicalLocality */
            PhysicalLocality destination = getPhysical(remote);

            /* establish a physical connection (Session) */
            protocolStack = connect(destination.getSessionId());

            /*
             * set the event manager so that events will be routed through this
             * node event manager
             */
            protocolState.setEventManager(getEventManager());

            /* start the incoming communication protocol */
            Protocol protocol = new Protocol(protocolState);
            protocol.setProtocolStack(protocolStack);
            protocol.start();

            if (!protocolState.isSuccess()) {
                /* print the error on the screen */
                System.err.println(remote + ": " + protocolState.getError());

                /* be sure to update also the session manager */
                sessionManagers.outgoingSessionManager
                        .removeSession(protocolStack.getSession());
            }

            return protocolState.isSuccess();
        } catch (ProtocolException e) {
            try {
                if (protocolStack != null)
                    protocolStack.close();
            } catch (ProtocolException e1) {
                /* ignore */
            }
            throw new KlavaConnectionException(e);
        }
    }

    /**
     * Logins to a remote locality.
     * 
     * @param remote
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean login(Locality remote) throws KlavaException {
        /* the login protocol state */
        LoginSubscribeState loginSubscribeState = new LoginSubscribeState(
                routingTable, messageProtocolFactory);

        return handleOutgoing(remote, loginSubscribeState);
    }

    /**
     * Logout from a remote locality.
     * 
     * @param remote
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean logout(Locality remote) throws KlavaException {
        return performDisconnect(remote, null);
    }

    /**
     * Unsubscribe from a remote locality by specifying the LogicalLocality with
     * which this node had previously subscribed.
     * 
     * @param remote
     * @param logical
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean unsubscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        return performDisconnect(remote, logical);
    }

    /**
     * Disconnect from a remote node, i.e., logout or unsubscribe
     * 
     * @param remote
     * @param logical
     *            if not null, performs an unsubscribe, otherwise a logout.
     * @return
     * @throws KlavaException
     */
    private boolean performDisconnect(Locality remote, LogicalLocality logical)
            throws KlavaException {
        /*
         * first retrieves the ProtocolStack associated to the destination
         * locality
         */
        ProtocolStack protocolStack = routingTable
                .getProtocolStack(getPhysical(remote).getSessionId());

        if (protocolStack == null) {
            /*
             * this might be the case when we disconnect from a locality to
             * which we're not connected or from which we've already
             * disconnected.
             */
            throw new KlavaNoDirectCommunicationsException(remote.toString());
        }

        /* actually sends the operation message */
        Marshaler marshaler;
        try {
            marshaler = protocolStack.createMarshaler();

            if (logical == null) {
                marshaler.writeStringLine(AcceptRegisterState.LOGOUT_S);
            } else {
                marshaler.writeStringLine(AcceptRegisterState.UNSUBSCRIBE_S);
                marshaler.writeStringLine(logical.toString());
            }

            protocolStack.releaseMarshaler(marshaler);

            /* also physically close the connection */
            disconnect(protocolStack);

            return true;
        } catch (ProtocolException e) {
            throw new KlavaException(e);
        } catch (IOException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Subscribes to a remote locality with the specified LogicalLocality.
     * 
     * @param remote
     * @param logical
     *            The LogicalLocality with which this node subscribes to the
     *            remote node.
     * @return Whether the operation succeeded
     * @throws KlavaException
     */
    public boolean subscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        /* the login protocol state */
        LoginSubscribeState loginSubscribeState = new LoginSubscribeState(
                routingTable, messageProtocolFactory, logical);

        return handleOutgoing(remote, loginSubscribeState);
    }

    /**
     * Wait for a remote node to disconnect from this one.
     * 
     * @param physicalLocality
     *            This will be set with the PhysicalLocality of the disconnected
     *            node.
     * @throws KlavaException
     */
    public void disconnected(PhysicalLocality physicalLocality)
            throws KlavaException {
        try {
            LoginSubscribeEvent loginSubscribeEvent = (LoginSubscribeEvent) waitForLogoutEvents
                    .waitForEvent();
            physicalLocality.setValue(loginSubscribeEvent.session
                    .getRemoteEnd().toString());
        } catch (ClassCastException e) {
            throw new KlavaException(e);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Wait for a remote node to disconnect (with an unsubscribe) from this one.
     * 
     * @param physicalLocality
     *            This will be set with the PhysicalLocality of the disconnected
     *            node.
     * @param logicalLocality
     *            This will be set with the LogicalLocality of the disconnected
     *            node.
     * @throws KlavaException
     */
    public void disconnected(PhysicalLocality physicalLocality,
            LogicalLocality logicalLocality) throws KlavaException {
        try {
            LoginSubscribeEvent loginSubscribeEvent = (LoginSubscribeEvent) waitForUnsusbscribeEvents
                    .waitForEvent();
            physicalLocality.setValue(loginSubscribeEvent.session
                    .getRemoteEnd().toString());
            logicalLocality.setValue(loginSubscribeEvent.logicalLocality);
        } catch (ClassCastException e) {
            throw new KlavaException(e);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs an EVAL operation at this node.
     * 
     * @param klavaProcess
     * @throws KlavaException
     */
    public void eval(KlavaProcess klavaProcess) throws KlavaException {
        try {
            addNodeProcess(klavaProcess);
        } catch (IMCException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs an EVAL operation at this node.
     * 
     * @param klavaProcess
     * @throws KlavaException
     */
    public void eval(KlavaNodeCoordinator klavaNodeCoordinator)
            throws KlavaException {
        try {
            addNodeCoordinator(klavaNodeCoordinator);
        } catch (IMCException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs an EVAL operation at the specified destination.
     * 
     * @param klavaProcess
     * @param destination
     * @throws KlavaException
     */
    public void eval(KlavaProcess klavaProcess, Locality destination)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            eval(klavaProcess);
            return;
        }

        /* the response we will wait for */
        Response<String> response = new Response<String>();

        /* we build a tuple made of a single element, the process to send */
        tupleOperation(TuplePacket.EVAL_S, new Tuple(klavaProcess),
                realDestination, true, waitingForOkResponse, response, -1);
        /*
         * if we're here the sending succeeded. But we still have to check the
         * response contents
         */
        if (response.error != null)
            throw new KlavaException(response.error);
    }

    /**
     * Performs an OUT operation at the local tuple space.
     * 
     * @param tuple
     */
    public void out(Tuple tuple) {
        tupleSpace.out(tuple);
    }

    /**
     * Performs an OUT operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void out(Tuple tuple, Locality destination) throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            out(tuple);
            return;
        }

        /* the response we will wait for */
        Response<String> response = new Response<String>();

        tupleOperation(TuplePacket.OUT_S, tuple, realDestination, true,
                waitingForOkResponse, response, -1);
        /*
         * if we're here the sending succeeded. But we still have to check the
         * response contents
         */
        if (response.error != null)
            throw new KlavaException(response.error);
    }

    /**
     * Performs an IN operation at the local tuple space.
     * 
     * @param tuple
     * @throws KlavaException
     */
    public void in(Tuple tuple) throws KlavaException {
        try {
            tupleSpace.in(tuple);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs a non blocking IN operation at the local tuple space.
     * 
     * @param tuple
     * @return Whether a matching tuple is found
     */
    public boolean in_nb(Tuple tuple) {
        return tupleSpace.in_nb(tuple);
    }

    /**
     * Performs an IN operation at the local tuple space with the specified
     * timeout.
     * 
     * @param tuple
     * @param timeout
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    public boolean in_t(Tuple tuple, long timeout) throws KlavaException {
        try {
            return tupleSpace.in_t(tuple, timeout);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs an IN operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void in(Tuple tuple, Locality destination) throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            in(tuple);
            return;
        }

        operationInRead(TuplePacket.IN_S, tuple, realDestination, true, -1);
    }

    /**
     * Performs a non-blocking IN operation of the specified Tuple at the
     * destination locality.
     * 
     * @param tuple
     * @param destination
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    public boolean in_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            return in_nb(tuple);
        }
        return operationInRead(TuplePacket.IN_S, tuple, realDestination, false,
                -1);
    }

    /**
     * Performs a timeout IN operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @param timeout
     *            Must be > 0
     * @return true if the operation completed successfully or false if it
     *         completed for a timeout
     * @throws KlavaException
     */
    public boolean in_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            return in_t(tuple, timeout);
        }

        return operationInRead(TuplePacket.IN_S, tuple, realDestination, true,
                timeout);
    }

    /**
     * Performs an READ operation at the local tuple space.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void read(Tuple tuple) throws KlavaException {
        try {
            tupleSpace.read(tuple);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs a non blocking READ operation at the local tuple space.
     * 
     * @param tuple
     * @param destination
     * @return Whether a matching tuple is found
     */
    public boolean read_nb(Tuple tuple) {
        return tupleSpace.read_nb(tuple);
    }

    /**
     * Performs a READ operation at the local tuple space with the specified
     * timeout.
     * 
     * @param tuple
     * @param timeout
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    public boolean read_t(Tuple tuple, long timeout) throws KlavaException {
        try {
            return tupleSpace.read_t(tuple, timeout);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Performs an READ operation of the specified Tuple at the destination
     * locality.
     * 
     * @param tuple
     * @param destination
     * @throws KlavaException
     */
    public void read(Tuple tuple, Locality destination) throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            read(tuple);
            return;
        }

        operationInRead(TuplePacket.READ_S, tuple, realDestination, true, -1);
    }

    /**
     * Performs a non-blocking READ operation of the specified Tuple at the
     * destination locality.
     * 
     * @param tuple
     * @param destination
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    public boolean read_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            return read_nb(tuple);
        }

        return operationInRead(TuplePacket.READ_S, tuple, realDestination,
                false, -1);
    }

    /**
     * Performs a timeout READ operation of the specified Tuple at the
     * destination locality.
     * 
     * @param tuple
     * @param destination
     * @param timeout
     *            Must be > 0
     * @return true if the operation completed successfully or false if it
     *         completed for a timeout
     * @throws KlavaException
     */
    public boolean read_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        PhysicalLocality realDestination = new PhysicalLocality();
        if (checkLocalDestination(destination, realDestination)) {
            /* this is a local operation */
            return read_t(tuple, timeout);
        }

        return operationInRead(TuplePacket.READ_S, tuple, realDestination,
                true, timeout);
    }

    /**
     * Performs an IN or READ operation of the specified Tuple at the
     * destination locality.
     * 
     * @param operation
     *            The operation identifier
     * @param tuple
     * @param destination
     * @param blocking
     *            Whether the operation should be a blocking one
     * @param timeout
     *            The timeout for the operation
     * @return Whether a matching tuple is found
     * @throws KlavaException
     */
    private boolean operationInRead(String operation, Tuple tuple,
            PhysicalLocality destination, boolean blocking, long timeout)
            throws KlavaException {
        /* the response we will wait for */
        TupleResponse response = new TupleResponse();

        tupleOperation(operation, tuple, destination, blocking,
                waitingForTuple, response, timeout);
        /*
         * if we're here the operation succeeded but let's see whether the
         * response is a positive response or not, in case of a non blocking
         * operation
         */
        if ((blocking && timeout < 0) || (response.error == null)) {
            // first we save the original template
            tuple.setOriginalTemplate();

            // then we copy the result
            tuple.copy_tuple(response.responseContent);
            return true;
        }

        return false;
    }

    /**
     * Tries to translate destLocality into a PhysicalLocality; then it checks
     * whether the PhysicalLocality corresponds to this very node. If it does,
     * returns true, otherwise false. The translated locality is put into the
     * out parameter destination (which then must not be null). In case the
     * locality is self, then it returns true, and the out locality destination
     * is not modified.
     * 
     * @param destLocality
     *            The locality to translate into a PhysicalLocality
     * @param destination
     *            out parameter where the translated PhysicalLocality is set
     * @return true if the PhysicalLocality corresponds to a local address of
     *         this node.
     * @throws KlavaException
     */
    protected boolean checkLocalDestination(Locality destLocality,
            PhysicalLocality destination) throws KlavaException {
        if (destLocality == self)
            return true;

        /* first translate a possible LogicalLocality */
        destination.setValue(getPhysical(destLocality));

        if (isLocal(new NodeLocation(destination.toString())))
            return true;

        return false;
    }

    /**
     * Executes a tuple operation on a specific destination locality. Once the
     * operation message is sent, the method blocks waiting for a response. The
     * response instance must be passed to the method, together with the data
     * structure used to associate a process name to a response instance,
     * waitingForResponse. The process name is obtained from the current thread
     * name.
     * 
     * Notice that the blocking parameter only means that we are willing to wait
     * if a matching tuple is not present, i.e., we are always blocked until we
     * don't get a response from the destination site, even if blocking is
     * false.
     * 
     * @param <R>
     * @param <ResponseType>
     *            The type of the Response
     * @param operation
     *            The operation identifier
     * @param tuple
     *            The tuple for this operation
     * @param destination
     *            The PhysicalLocality of the destination
     * @param blocking
     *            Whether the operation should be a blocking one
     * @param waitingForResponse
     *            The structure where Responses are waited for and notified
     * @param response
     *            The Response we wait for
     * @param timeout
     *            The timeout for the operation
     * @throws KlavaException
     */
    protected <R, ResponseType extends Response<R>> void tupleOperation(
            String operation, Tuple tuple, PhysicalLocality destination,
            boolean blocking,
            WaitingForResponse<ResponseType> waitingForResponse,
            ResponseType response, long timeout) throws KlavaException {
        /*
         * then retrieves the ProtocolStack associated to the destination
         * locality
         */
        ProtocolStack protocolStack = routingTable.getProtocolStack(destination
                .getSessionId());

        if (protocolStack != null) {
            tupleOperation(protocolStack, operation, tuple, blocking,
                    waitingForResponse, response, timeout, destination);
        } else {
            /*
             * try to query all the nodes we are connected to, to see if we can
             * deliver this message
             */
            Enumeration<ProtocolStack> stacks = sessionManagers.outgoingSessionManager
                    .getStacks();

            while (stacks.hasMoreElements()) {
                ProtocolStack protocolStack1 = stacks.nextElement();

                try {
                    tupleOperation(protocolStack1, operation, tuple, blocking,
                            waitingForResponse, response, timeout, destination);

                    if (response.error == null) {
                        return; // OK we're done
                    } else if (response.error.equals(ResponseState.FAIL_S +
                            ResponseState.SPECIFICATION_SEPARATOR +
                            TuplePacket.TUPLEABSENT_S)) {
                        // in this case we found a way to reach the locality,
                        // but the tuple is not there, so we're done anyway
                        return;
                    } else {
                        response.reset(); // reset the error state
                    }
                } catch (KlavaException e) {
                    /* we go on with the next stack */
                    e.printStackTrace();
                }
            }

            /* if we're here we failed */
            throw new KlavaPhysicalLocalityException("no route to " +
                    destination);
        }
    }

    /**
     * Executes a tuple operation on a specific destination locality, by using a
     * specific ProtocolStack.
     * 
     * @param <R>
     * @param <ResponseType>
     * @param protocolStack
     * @param operation
     * @param tuple
     * @param blocking
     * @param waitingForResponse
     * @param response
     * @param timeout
     * @param destination
     * @throws ProtocolException
     * @throws KlavaException
     */
    public <R, ResponseType extends Response<R>> void tupleOperation(
            ProtocolStack protocolStack, String operation, Tuple tuple,
            boolean blocking,
            WaitingForResponse<ResponseType> waitingForResponse,
            ResponseType response, long timeout, PhysicalLocality destination)
            throws KlavaException {
        try {
            /* Retrieves the source locality from the ProtocolStack */
            PhysicalLocality source = new PhysicalLocality(protocolStack
                    .getSession().getLocalEnd());
            TuplePacket tuplePacket = new TuplePacket(destination, source,
                    operation, tuple);
            tuplePacket.blocking = blocking;
            tuplePacket.timeout = timeout;

            /* this is needed to retrieve the response */
            tuplePacket.processName = Thread.currentThread().getName();

            /* binds the passed response to the thread name */
            waitingForResponse.put(tuplePacket.processName, response);

            /* actually sends the operation message */
            Marshaler marshaler = protocolStack.createMarshaler();

            TupleOpState tupleOpState = new TupleOpState();
            tupleOpState.setTuplePacket(tuplePacket);
            tupleOpState.setProtocolStack(protocolStack);
            tupleOpState.setDoRead(false);
            tupleOpState.enter(null, new TransmissionChannel(marshaler));

            protocolStack.releaseMarshaler(marshaler);

            /* now wait for response */
            response.waitForResponse();
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        } catch (ProtocolException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * Translates a Locality into a PhysicalLocality. If the passed locality is
     * a LogicalLocality tries to use the environment to translate it otherwise
     * if it is a PhysicalLocality simply returns it.
     * 
     * If the locality is logical and it cannot be resolved it throws a
     * KlavaLogicalLocalityException. Otherwise the returned PhysicalLocality is
     * not null.
     * 
     * If the passed locality is self, returns mainPhysicalLocality if set,
     * otherwise returns the first established session's local end, if no
     * session is established it throws a KlavaLogicalLocalityException.
     * 
     * @param locality
     * @return The PhysicalLocality corresponding to the Locality
     * @throws KlavaException
     */
    public PhysicalLocality getPhysical(Locality locality)
            throws KlavaException {
        if (locality instanceof PhysicalLocality) {
            return (PhysicalLocality) locality;
        }

        if (!(locality instanceof LogicalLocality))
            throw new KlavaLogicalLocalityException("not a logical locality: " +
                    locality);

        LogicalLocality logicalLocality = (LogicalLocality) locality;

        if (logicalLocality.equals(self)) {
            if (mainPhysicalLocality != null)
                return mainPhysicalLocality;

            try {
                Session oneSession = getOneSession();
                if (oneSession == null)
                    throw new KlavaLogicalLocalityException(
                            "no physical locality for self");

                return new PhysicalLocality(oneSession.getLocalEnd());
            } catch (ProtocolException e) {
                throw new KlavaException(e);
            }

        }

        return logicalLocalityResolver.resolve(logicalLocality);
    }

    /**
     * Returns one established session. It is not specified which one, or null
     * if no session is established.
     * 
     * @return one established session
     * @throws ProtocolException
     */
    protected Session getOneSession() throws ProtocolException {
        /* it first tries with incoming sessions */
        Enumeration<ProtocolStack> stacks = sessionManagers.incomingSessionManager
                .getStacks();

        if (stacks.hasMoreElements())
            return stacks.nextElement().getSession();

        /* then tries with outgoing sessions */
        stacks = sessionManagers.outgoingSessionManager.getStacks();

        if (stacks.hasMoreElements())
            return stacks.nextElement().getSession();

        /* sorry, no session */
        return null;
    }

    /**
     * Creates a brand new node and returns its locality
     * 
     * @return the locality of the brand new node.
     * @throws KlavaException
     */
    public PhysicalLocality newloc() throws KlavaException {
        /* pass a null coordinator to the more general version */
        return newloc((KlavaNodeCoordinator) null);
    }

    /**
     * Creates a brand new node and returns its locality (it also installs the
     * passed node coodinator on the created node).
     * 
     * @param nodeCoordinator
     * 
     * @return the locality of the brand new node.
     * @throws KlavaException
     */
    public PhysicalLocality newloc(KlavaNodeCoordinator nodeCoordinator)
            throws KlavaException {
        return newloc(null, nodeCoordinator);
    }

    /**
     * Creates a brand new node and returns its locality (it also installs the
     * passed node coodinator on the created node).
     * 
     * @param className
     *            The class name of the node that will be created for the new
     *            locality
     * @param nodeCoordinator
     * 
     * @return the locality of the brand new node.
     * @throws KlavaException
     */
    public PhysicalLocality newloc(String className,
            KlavaNodeCoordinator nodeCoordinator) throws KlavaException {
        /*
         * this will create a local session (communication through pipes). If
         * the main locality is set, then use it as a prefix to create the
         * session identifier
         */

        try {
            /* create a brand new session id for the local pipe */
            SessionId sessionId = LocalSessionStarter
                    .createNewSessionId((getNodeName() != null ? getNodeName()
                            : ""));

            /* and wait for a one-time accept on that session id */
            PhysicalLocality physicalLocality = new PhysicalLocality(sessionId);
            AcceptNodeCoordinator acceptNodeCoordinator = new AcceptNodeCoordinator(
                    physicalLocality);

            addNodeCoordinator(acceptNodeCoordinator);

            /*
             * we call yield to give a better chance to the accept coordinator
             * to start listening for requests, so that the next login has more
             * chances to find the accept thread already listening
             */
            Thread.yield();

            KlavaNode node = null;

            if (className == null) {
                /* make the client node connect to the brand new locality */
                node = new ClientNode(physicalLocality);
            } else {
                /* create an instance of node through the specified class name */
                node = (KlavaNode) (Class.forName(className).getDeclaredConstructor().newInstance());

                /* and connect it to this creator node */
                node.login(physicalLocality);
            }

            if (nodeCoordinator != null)
                node.addNodeCoordinator(nodeCoordinator);

            acceptNodeCoordinator.join();

            PhysicalLocality newLoc = acceptNodeCoordinator.getRemote();

            if (newLoc == null)
                throw new KlavaConnectionException("failed on newloc");

            return newLoc;
        } catch (Exception e) {
            throw new KlavaException(e);
        }

    }

    /**
     * Logs the passed node into this Node returning its new Locality (i.e., the
     * locality with which the passed node has been logged into this node)
     * 
     * @param node
     *            The node to logs into this node.
     * 
     * @return the locality of the logged node.
     * @throws KlavaException
     */
    public PhysicalLocality newloc(KlavaNode node) throws KlavaException {
        /*
         * this will create a local session (communication through pipes). If
         * the main locality is set, then use it as a prefix to create the
         * session identifier
         */

        try {
            /* create a brand new session id for the local pipe */
            SessionId sessionId = LocalSessionStarter
                    .createNewSessionId((getNodeName() != null ? getNodeName()
                            : ""));

            /* and wait for a one-time accept on that session id */
            PhysicalLocality physicalLocality = new PhysicalLocality(sessionId);
            AcceptNodeCoordinator acceptNodeCoordinator = new AcceptNodeCoordinator(
                    physicalLocality);

            addNodeCoordinator(acceptNodeCoordinator);

            /*
             * we call yield to give a better chance to the accept coordinator
             * to start listening for requests, so that the next login has more
             * chances to find the accept thread already listening
             */
            Thread.yield();

            /* and connect it to this creator node */
            node.login(physicalLocality);

            acceptNodeCoordinator.join();

            PhysicalLocality newLoc = acceptNodeCoordinator.getRemote();

            if (newLoc == null)
                throw new KlavaConnectionException("failed on newloc");

            return newLoc;
        } catch (ProtocolException e) {
            throw new KlavaException(e);
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        } catch (IMCException e) {
            throw new KlavaException(e);
        } catch (ClassCastException e) {
            throw new KlavaException(e);
        }

    }

    /**
     * Tries to add a mapping LogicalLocality - PhysicalLocality into the
     * Environment. If it suceeds it returns true, false otherwise.
     * 
     * @param logicalLocality
     * @param physicalLocality
     * @return If it suceeds it returns true, false otherwise.
     */
    public boolean addToEnvironment(LogicalLocality logicalLocality,
            PhysicalLocality physicalLocality) {
        return environment.try_add(logicalLocality, physicalLocality);
    }

    /**
     * @see klava.Environment#remove(klava.LogicalLocality)
     */
    public PhysicalLocality removeLogical(LogicalLocality l) {
        return environment.remove(l);
    }

    /**
     * @see klava.Environment#removePhysical(klava.PhysicalLocality)
     */
    public HashSet<LogicalLocality> removePhysical(
            PhysicalLocality physicalLocality) {
        return environment.removePhysical(physicalLocality);
    }

    /**
     * Returns the protocol stack associated to the specified PhysicalLocality.
     * It searches in the RoutingTable.
     * 
     * @param physicalLocality
     * 
     * @return
     */
    public ProtocolStack getNodeStack(PhysicalLocality physicalLocality) {
        return routingTable.getProtocolStack(physicalLocality.getSessionId());
    }

    /**
     * Redefines this method in order to create a KlavaNodeProcessProxy
     * 
     * @see org.mikado.imc.topology.Node#createNodeProcessProxy()
     */
    @Override
    protected NodeProcessProxy createNodeProcessProxy() {
        return new KlavaNodeProcessProxy(this);
    }

    /**
     * Redefines this method in order to create a KlavaNodeCoordinatorProxy
     * 
     * @see org.mikado.imc.topology.Node#createNodeCoordinatorProxy()
     */
    @Override
    protected NodeCoordinatorProxy createNodeCoordinatorProxy() {
        return new KlavaNodeCoordinatorProxy(this);
    }

    /**
     * This extends close of the parent class by closing also the nodes that
     * this node had created.
     * 
     * @see org.mikado.imc.topology.Node#close()
     */
    @Override
    public void close() throws IMCException {
        try {
            super.close();
        } finally {
            Enumeration<KlavaNode> myNodes = newNodes.elements();
            while (myNodes.hasMoreElements()) {
                try {
                    myNodes.nextElement().close();
                } catch (IMCException e) {
                    /* we have to go on anyway and close all the nodes */
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Closes all the sessions and session starters that involve the passed
     * PhysicalLocality.
     * 
     * @param physicalLocality
     */
    public void closeSessions(PhysicalLocality physicalLocality) {
        sessionManagers.closeSessions(physicalLocality.getSessionId());
    }

    /**
     * @return Returns the messageProtocolFactory.
     */
    public final MessageProtocolFactory getMessageProtocolFactory() {
        return messageProtocolFactory;
    }

    /**
     * @param messageProtocolFactory
     *            The messageProtocolFactory to set.
     */
    public final void setMessageProtocolFactory(
            MessageProtocolFactory messageProtocolFactory) {
        this.messageProtocolFactory = messageProtocolFactory;
    }

    /**
     * @return Returns the tupleSpace.
     */
    public final TupleSpace getTupleSpace() {
        return tupleSpace;
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
     * @return Returns the waitingForLocality.
     */
    public WaitingForResponse<Response<PhysicalLocality>> getWaitingForLocality() {
        return waitingForLocality;
    }

    /**
     * @param waitingForLocality
     *            The waitingForLocality to set.
     */
    public void setWaitingForLocality(
            WaitingForResponse<Response<PhysicalLocality>> waitingForLocality) {
        this.waitingForLocality = waitingForLocality;
    }

    /**
     * @return Returns the waitingForOkResponse.
     */
    public WaitingForResponse<Response<String>> getWaitingForOkResponse() {
        return waitingForOkResponse;
    }

    /**
     * @param waitingForOkResponse
     *            The waitingForOkResponse to set.
     */
    public void setWaitingForOkResponse(
            WaitingForResponse<Response<String>> waitingForOkResponse) {
        this.waitingForOkResponse = waitingForOkResponse;
    }

    /**
     * @return Returns the waitingForTuple.
     */
    public WaitingForResponse<TupleResponse> getWaitingForTuple() {
        return waitingForTuple;
    }

    /**
     * @param waitingForTuple
     *            The waitingForTuple to set.
     */
    public void setWaitingForTuple(
            WaitingForResponse<TupleResponse> waitingForTuple) {
        this.waitingForTuple = waitingForTuple;
    }

    /**
     * @return Returns the closureMaker.
     */
    public ClosureMaker getClosureMaker() {
        return closureMaker;
    }

    /**
     * @param closureMaker
     *            The closureMaker to set.
     */
    public void setClosureMaker(ClosureMaker closureMaker) {
        this.closureMaker = closureMaker;
    }

    /**
     * @see klava.topology.ClosureMaker#makeClosure(klava.Tuple,
     *      klava.PhysicalLocality)
     */
    public void makeClosure(Tuple tuple, PhysicalLocality forSelf)
            throws KlavaException {
        closureMaker.makeClosure(tuple, forSelf);
    }

    /**
     * @see klava.topology.ClosureMaker#makeClosure(klava.TupleItem,
     *      klava.PhysicalLocality)
     */
    public TupleItem makeClosure(TupleItem tupleItem, PhysicalLocality forSelf)
            throws KlavaException {
        return closureMaker.makeClosure(tupleItem, forSelf);
    }

    /**
     * @return Returns the mainPhysicalLocality.
     */
    public PhysicalLocality getMainPhysicalLocality() {
        return mainPhysicalLocality;
    }

    /**
     * @param mainPhysicalLocality
     *            The mainPhysicalLocality to set.
     */
    public void setMainPhysicalLocality(PhysicalLocality mainPhysicalLocality) {
        this.mainPhysicalLocality = mainPhysicalLocality;
    }

    /**
     * @return Returns the migratingCodeFactory.
     */
    public MigratingCodeFactory getMigratingCodeFactory() {
        return migratingCodeFactory;
    }

    /**
     * @param migratingCodeFactory
     *            The migratingCodeFactory to set.
     */
    public void setMigratingCodeFactory(
            MigratingCodeFactory migratingCodeFactory) {
        this.migratingCodeFactory = migratingCodeFactory;
    }

}
