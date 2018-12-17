/*
 * Created on Oct 5, 2005
 */
package klava.proto;

import java.io.IOException;

import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.WaitingForResponse;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;
import org.mikado.imc.topology.CollectableThread;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.ThreadContainer;

/**
 * Deals with responses to operations.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class ResponseState extends ProtocolSwitchState {

    /**
     * In case of a forwarding of response OK/ERROR, this thread waits to find a
     * route.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class ResponseOkErrorThread extends CollectableThread {
        String op;

        String result;

        SessionId destination;

        String processName;

        /**
         * @param op
         * @param result
         * @param destination
         * @param processName
         */
        public ResponseOkErrorThread(String op, String result,
                SessionId destination, String processName) {
            this.op = op;
            this.result = result;
            this.destination = destination;
            this.processName = processName;
        }

        /**
         * @see org.mikado.imc.topology.CollectableThread#execute()
         */
        public void execute() throws IMCException {
            try {
                /* first we must find the route */
                ProtocolStack route = RouteFinderState.findRoute(destination,
                        sessionManager, waitingForResponse);

                if (route == null) {
                    System.err.println("cannot route response " + result
                            + " to " + destination);

                    /* we can't do much more here */
                    return;
                }

                /* we forward the response and exit */
                sendResponseOkError(route, op, result, destination, processName);
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    /**
     * Handles a response involving a Locality
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class ResponseLocalityState extends ProtocolStateSimple {

        /**
         * 
         */
        public ResponseLocalityState() {
            super();
        }

        /**
         * @param next_state
         */
        public ResponseLocalityState(String next_state) {
            super(next_state);
        }

        /**
         * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            try {
                UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

                String process = readProcessName(unMarshaler);

                PhysicalLocality physicalLocality = new PhysicalLocality();
                Response<PhysicalLocality> response = null;
                try {
                    String result = unMarshaler.readStringLine();

                    if (result.equals(OK_S))
                        physicalLocality = new PhysicalLocality(unMarshaler
                                .readStringLine());

                    response = waitingForLocality.remove(process);
                    if (response != null) {
                        // otherwise we simply discard the locality
                        synchronized (response) {
                            if (!result.equals(OK_S))
                                response.error = result;
                            response.responseContent = physicalLocality;
                            response.notify();
                        }
                    }
                } catch (KlavaMalformedPhyLocalityException e) {
                    if (response != null) {
                        synchronized (response) {
                            response.error = e.getMessage();
                            response.notify();
                        }
                    }
                }
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }

    }

    /**
     * Handles a response involving a tuple
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class ResponseTupleState extends ProtocolStateSimple {

        /**
         * In case of a forwarding of a tuple response, this thread waits to
         * find a route.
         * 
         * @author Lorenzo Bettini
         * @version $Revision: 1.5 $
         */
        public class ResponseTupleThread extends CollectableThread {
            String op;

            boolean result;

            SessionId from;

            SessionId destination;

            String processName;

            Tuple tuple;

            /**
             * @param op
             * @param result
             * @param from
             * @param destination
             * @param processName
             * @param tuple
             */
            public ResponseTupleThread(String op, boolean result,
                    SessionId from, SessionId destination, String processName,
                    Tuple tuple) {
                this.op = op;
                this.result = result;
                this.from = from;
                this.destination = destination;
                this.processName = processName;
                this.tuple = tuple;
            }

            public void execute() throws IMCException {
                try {
                    /* first we must find the route */
                    ProtocolStack route = RouteFinderState.findRoute(
                            destination, sessionManager, waitingForResponse);

                    if (route == null) {
                        System.err.println("cannot route tuple response "
                                + tuple + " to " + destination);

                        /* if it was an IN response we must put it back */
                        if (op.equals(TuplePacket.IN_S)) {
                            putTupleBack(new TransmissionChannel(
                                    getProtocolStack().createMarshaler()),
                                    tuple, from, getProtocolStack()
                                            .getSession().getLocalEnd());
                        }

                        return;
                    }

                    /*
                     * we forward the response (leaving source and destination
                     * intact) and exit
                     */
                    sendResponseTuple(route, op, tuple, from, destination,
                            processName, result);
                } catch (Exception e) {
                    throw new IMCException(e);
                }
            }
        }

        /**
         * 
         */
        public ResponseTupleState() {
            super();
        }

        /**
         * @param next_state
         */
        public ResponseTupleState(String next_state) {
            super(next_state);
        }

        /**
         * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            try {
                UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

                String process = readProcessName(unMarshaler);
                SessionId destination = readDestination(unMarshaler);
                SessionId from = readFrom(unMarshaler);
                Tuple tuple = null;
                String res = unMarshaler.readStringLine();
                boolean fail = false;

                if (res.equals(OK_S)) {
                    TupleState tupleState = new TupleState();
                    tupleState.setDoRead(true);
                    tupleState.setProtocolStack(getProtocolStack());
                    tupleState.enter(null, transmissionChannel);
                    tuple = tupleState.getTuple();
                } else if (res.equals(FAIL_S)) {
                    fail = true;
                } else {
                    throw new WrongStringProtocolException(OK_S + " or "
                            + FAIL_S, res);
                }

                if (isToForward(destination)) {
                    ProtocolStack forward = routingTable
                            .getProtocolStack(destination);
                    if (forward != null) {
                        /* we forward the response and exit */
                        sendResponseTuple(forward, param.toString(), tuple,
                                from, destination, process, !fail);

                        return;
                    } else {
                        /* spawn a thread to take care of finding the route */
                        waitingThreads.addAndStart(new ResponseTupleThread(
                                param.toString(), !fail, from, destination,
                                process, tuple));
                        return;
                    }
                }

                TupleResponse tupleResponse = waitingForTuple.remove(process);
                if (tupleResponse != null) {
                    if (fail)
                        tupleResponse.error = res + SPECIFICATION_SEPARATOR + param;
                    else
                        tupleResponse.responseContent = tuple;

                    synchronized (tupleResponse) {
                        tupleResponse.notify();
                    }
                } else {
                    /*
                     * in case of an IN we must put the tuple back if the
                     * requesting process is not there anymore
                     */
                    if (param.toString().equals(TuplePacket.IN_S)) {
                        ProtocolStack protocolStack = getProtocolStack();
                        if (protocolStack == null)
                            throw new ProtocolException(
                                    "missing protocol stack");

                        Session session = protocolStack.getSession();
                        if (session == null)
                            throw new ProtocolException("missing session");

                        putTupleBack(transmissionChannel, tuple, session
                                .getLocalEnd(), session.getRemoteEnd());
                    }
                }
            } catch (IOException e) {
                throw new ProtocolException(e);
            } catch (IMCException e) {
                throw new ProtocolException(e);
            }
        }

        /**
         * Sends a tuple (that was received due to an IN response) back to the
         * sender, since we do not need it here.
         * 
         * @param transmissionChannel
         * @param tuple
         * @param from
         *            from whom we had received this tuple
         * @param to
         * @throws ProtocolException
         * @throws IOException
         */
        private void putTupleBack(TransmissionChannel transmissionChannel,
                Tuple tuple, SessionId from, SessionId to)
                throws ProtocolException, IOException {
            TuplePacket tuplePacket;
            try {
                tuplePacket = new TuplePacket(new PhysicalLocality(from
                        .toString()), new PhysicalLocality(to.toString()),
                        TuplePacket.TUPLEBACK_S, tuple);

                TupleOpState tupleOpState = new TupleOpState();
                tupleOpState.setDoRead(false);
                tupleOpState.setTuplePacket(tuplePacket);

                TransmissionChannel transmissionChannel2 = new TransmissionChannel(
                        getMarshaler(transmissionChannel));

                tupleOpState.writePacket(transmissionChannel2);

                releaseMarshaler(transmissionChannel2.marshaler);
            } catch (KlavaMalformedPhyLocalityException e) {
                e.printStackTrace();
                /* there's nothing we can do about it. */
            }
        }

    }

    /**
     * Handles a response involving an OK or an error
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.5 $
     */
    public class ResponseOkErrorState extends ProtocolStateSimple {

        /**
         * 
         */
        public ResponseOkErrorState() {
            super();
        }

        /**
         * @param next_state
         */
        public ResponseOkErrorState(String next_state) {
            super(next_state);
        }

        /**
         * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            try {
                /* param contains the operation involved in the response */
                UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
                String process = readProcessName(unMarshaler);
                SessionId destination = readDestination(unMarshaler);

                String res = unMarshaler.readStringLine();

                if (isToForward(destination)) {
                    ProtocolStack forward = routingTable
                            .getProtocolStack(destination);
                    if (forward != null) {
                        /* we forward the response and exit */
                        sendResponseOkError(forward, param.toString(), res,
                                destination, process);
                        return;
                    } else {
                        /* spawn a thread to take care of finding the route */
                        waitingThreads.addAndStart(new ResponseOkErrorThread(
                                param.toString(), res, destination, process));
                        return;
                    }
                }

                Response<String> response = waitingForResponse.remove(process);
                if (response != null) {
                    synchronized (response) {
                        if (res.equals(OK_S)) {
                            response.responseContent = res;
                            response.error = null;
                        } else {
                            response.responseContent = null;
                            response.error = res;
                        }

                        response.notify();
                    }
                } // otherwise we simply discard it
            } catch (IOException e) {
                throw new ProtocolException(e);
            } catch (IMCException e) {
                throw new ProtocolException(e);
            }
        }
    }

    public static final String RESPONSE_S = "RESPONSE";

    public static final String RESPONSELOCALITY_S = "RESPONSELOCALITY";

    public static final String FROM_S = "FROM";

    public static final String TO_S = "TO";

    public static final String OK_S = "OK";

    public static final String FAIL_S = "FAIL";
    
    /** the separator string for specifying further information, e.g.,
     * ERROR CODE + SPECIFICATION_SEPARATOR + OTHER INFO; this usually corresponds
     * to ": " */
    public static final String SPECIFICATION_SEPARATOR = ": ";

    /**
     * The table that associates process names to tuple responses.
     */
    WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();

    /**
     * The table that associates process names to locality responses.
     */
    WaitingForResponse<Response<PhysicalLocality>> waitingForLocality = new WaitingForResponse<Response<PhysicalLocality>>();

    /**
     * The table that associates process names to generic responses.
     */
    WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();

    /**
     * The routing table that, if set, is used to forward responses.
     */
    RoutingTable routingTable = null;

    /**
     * To forward responses if not destined to this site.
     */
    protected SessionManager sessionManager;

    /**
     * Where the threads waiting for a route are stored.
     */
    ThreadContainer waitingThreads = new ThreadContainer();

    /**
     * 
     */
    public ResponseState() {
        super();
        init();
    }

    /**
     * Initializes this ProtocolSwitchState table.
     */
    protected void init() {
        addRequestState(TuplePacket.OUT_S, new ResponseOkErrorState());
        addRequestState(TuplePacket.EVAL_S, new ResponseOkErrorState());
        addRequestState(TuplePacket.IN_S, new ResponseTupleState());
        addRequestState(TuplePacket.READ_S, new ResponseTupleState());
        addRequestState(RESPONSELOCALITY_S, new ResponseLocalityState());
        addRequestState(RouteFinderState.HASROUTE_S, new ResponseOkErrorState());
        addRequestState(TuplePacket.TUPLEABSENT_S, new ResponseTupleState());
    }

    /**
     * On closing we make sure to interrupt all the possible waiting threads.
     * 
     * @see org.mikado.imc.protocols.ProtocolSwitchState#closed()
     */
    @Override
    public void closed() throws ProtocolException {
        super.closed();
        try {
            waitingThreads.close();
        } catch (IMCException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Retrieves the process name from the TransmissionChannel
     * 
     * @param unMarshaler
     * @return
     * @throws IOException
     * @throws ProtocolException
     */
    String readProcessName(UnMarshaler unMarshaler) throws IOException,
            ProtocolException {
        String process = unMarshaler.readStringLine();

        if (!process.equals(TupleOpState.PROCESS_S)) {
            throw new WrongStringProtocolException(TupleOpState.PROCESS_S,
                    process);
        }

        return unMarshaler.readStringLine();
    }

    /**
     * Reads the destination.
     * 
     * @param unMarshaler
     * @return
     * @throws IOException
     * @throws ProtocolException
     */
    static SessionId readDestination(UnMarshaler unMarshaler)
            throws IOException, ProtocolException {
        String to = unMarshaler.readStringLine();
        if (!to.equals(TO_S))
            throw new WrongStringProtocolException(TO_S, to);

        return SessionId.parseSessionId(unMarshaler.readStringLine());
    }

    /**
     * Reads the source.
     * 
     * @param unMarshaler
     * @return
     * @throws IOException
     * @throws ProtocolException
     */
    static SessionId readFrom(UnMarshaler unMarshaler) throws IOException,
            ProtocolException {
        String from = unMarshaler.readStringLine();
        if (!from.equals(FROM_S))
            throw new WrongStringProtocolException(FROM_S, from);

        return SessionId.parseSessionId(unMarshaler.readStringLine());
    }

    /**
     * Checks whether the specified destination coincides with our SessionId or
     * if we must forward to it.
     * 
     * @param destination
     * @return
     * @throws ProtocolException
     */
    boolean isToForward(SessionId destination) throws ProtocolException {
        ProtocolStack protocolStack = getProtocolStack();
        if (protocolStack == null)
            throw new ProtocolException("null stack");

        Session session = protocolStack.getSession();
        if (session == null)
            throw new ProtocolException("null session");

        return (!destination.equals(session.getLocalEnd()));
    }

    /**
     * Sends a the header for a generic response.
     * 
     * @param marshaler
     * @param op
     *            The operation for which we send the response
     * @param processName
     *            The process name involved in this response
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseHeader(Marshaler marshaler, String op,
            String processName) throws ProtocolException, IOException {
        marshaler.writeStringLine(RESPONSE_S);
        marshaler.writeStringLine(op);
        marshaler.writeStringLine(TupleOpState.PROCESS_S);
        marshaler.writeStringLine(processName);
    }

    /**
     * Sends a response for an out operation.
     * 
     * @param marshaler
     * @param destination
     *            The identifier of the destination
     * @param processName
     *            The name of the process that is waiting for this response.
     * @param result
     *            Wether the operation succeeded.
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseOut(Marshaler marshaler,
            SessionId destination, String processName, boolean result)
            throws ProtocolException, IOException {
        sendResponseOkError(marshaler, TuplePacket.OUT_S, (result ? OK_S
                : FAIL_S), destination, processName);
    }

    /**
     * Sends a response for an out operation.
     * 
     * @param protocolStack
     * @param destination
     * @param processName
     * @param result
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseOut(ProtocolStack protocolStack,
            SessionId destination, String processName, boolean result)
            throws ProtocolException, IOException {
        sendResponseOkError(protocolStack, TuplePacket.OUT_S, (result ? OK_S
                : FAIL_S), destination, processName);
    }

    /**
     * Sends a response for an eval operation.
     * 
     * @param marshaler
     * @param destination
     *            The identifier of the destination
     * @param processName
     *            The name of the process that is waiting for this response.
     * @param result
     *            The string describing the result (it can contain a specific
     *            error description)
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseEval(Marshaler marshaler,
            SessionId destination, String processName, String result)
            throws ProtocolException, IOException {
        sendResponseOkError(marshaler, TuplePacket.EVAL_S, result, destination,
                processName);
    }

    /**
     * Sends a response for an eval operation.
     * 
     * @param protocolStack
     * @param destination
     *            The identifier of the destination
     * @param processName
     *            The name of the process that is waiting for this response.
     * @param result
     *            The string describing the result (it can contain a specific
     *            error description)
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseEval(ProtocolStack protocolStack,
            SessionId destination, String processName, String result)
            throws ProtocolException, IOException {
        sendResponseOkError(protocolStack, TuplePacket.EVAL_S, result,
                destination, processName);
    }

    /**
     * Sends a response involving an OK or an error
     * 
     * @param marshaler
     * @param op
     *            The operation for which we send the response
     * @param result
     *            Whether OK or an error
     * @param destination
     *            The identifier of the destination
     * @param processName
     *            The name of the process that is waiting for this response.
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseOkError(Marshaler marshaler, String op,
            String result, SessionId destination, String processName)
            throws ProtocolException, IOException {
        sendResponseHeader(marshaler, op, processName);
        marshaler.writeStringLine(TO_S);
        marshaler.writeStringLine(destination.toString());
        marshaler.writeStringLine(result);
    }

    /**
     * Sends a response involving an OK or an error
     * 
     * @param protocolStack
     * @param op
     * @param result
     * @param destination
     * @param processName
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseOkError(ProtocolStack protocolStack,
            String op, String result, SessionId destination, String processName)
            throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();
        sendResponseOkError(marshaler, op, result, destination, processName);
        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * Sends a response involving a tuple. If it is a failure response then the
     * tuple is not written.
     * 
     * @param marshaler
     * @param op
     *            The operation for which we send the response
     * @param tuple
     *            the tuple to send
     * @param from
     *            The identifier of the sender
     * @param destination
     *            The identifier of the destination
     * @param processName
     * @param resultOk
     *            whether it is an OK response or a FAILURE response
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseTuple(Marshaler marshaler, String op,
            Tuple tuple, SessionId from, SessionId destination,
            String processName, boolean resultOk) throws ProtocolException,
            IOException {
        sendResponseHeader(marshaler, op, processName);

        marshaler.writeStringLine(TO_S);
        marshaler.writeStringLine(destination.toString());

        marshaler.writeStringLine(FROM_S);
        marshaler.writeStringLine(from.toString());

        if (resultOk) {
            marshaler.writeStringLine(OK_S);
            TupleState tupleState = new TupleState();
            tupleState.setDoRead(false);
            tupleState.setTuple(tuple);
            tupleState.enter(null, new TransmissionChannel(marshaler));
        } else {
            marshaler.writeStringLine(FAIL_S);
        }
    }

    /**
     * Sends a response involving a tuple. If it is a failure response then the
     * tuple is not written.
     * 
     * @param protocolStack
     * @param op
     * @param tuple
     * @param from
     * @param destination
     * @param processName
     * @param resultOk
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseTuple(ProtocolStack protocolStack,
            String op, Tuple tuple, SessionId from, SessionId destination,
            String processName, boolean resultOk) throws ProtocolException,
            IOException {
        Marshaler marshaler = protocolStack.createMarshaler();
        sendResponseTuple(marshaler, op, tuple, from, destination, processName,
                resultOk);
        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * Sends a response involving a locality. If the result is false, the
     * physical locality is not actually sent.
     * 
     * @param marshaler
     * @param physicalLocality
     *            the locality to send
     * @param processName
     * @param resultOk
     *            whether it is an OK response or a FAILURE response
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResponseLocality(Marshaler marshaler,
            PhysicalLocality physicalLocality, String processName,
            boolean resultOk) throws ProtocolException, IOException {
        sendResponseHeader(marshaler, RESPONSELOCALITY_S, processName);
        marshaler.writeStringLine((resultOk ? OK_S : FAIL_S));

        if (resultOk)
            marshaler.writeStringLine(physicalLocality.toString());
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
     * @return Returns the sessionManager.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * @param sessionManager
     *            The sessionManager to set.
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
