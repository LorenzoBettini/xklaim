/*
 * Created on Nov 4, 2005
 */
package klava.proto;

import java.io.IOException;

import klava.KlavaException;
import klava.TupleSpace;
import klava.WaitingForResponse;
import klava.topology.KlavaProcess;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.topology.CollectableThread;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.ThreadContainer;

/**
 * Handles a TuplePacket
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class TupleOpManager {
    /**
     * The thread that is blocked waiting for a matching tuple.
     * 
     * @author Lorenzo Bettini
     */
    public class TupleThread extends CollectableThread {
        /**
         * The TuplePacket containing the requested operation.
         */
        TuplePacket tuplePacket;

        /**
         * The SessionId of the node we're acting for
         */
        SessionId ourSessionId;

        /**
         * @param packet
         */
        public TupleThread(TuplePacket packet, SessionId ourSessionId) {
            tuplePacket = packet;
            this.ourSessionId = ourSessionId;
        }

        /**
         * @see org.mikado.imc.topology.CollectableThread#execute()
         */
        public void execute() throws IMCException {
            try {
                findMatchingTuple(tuplePacket, true, true, ourSessionId);
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    /**
     * The thread that is blocked waiting to find a route to forward.
     * 
     * @author Lorenzo Bettini
     */
    public class ForwardTupleThread extends TupleThread {
        /**
         * @param packet
         * @param ourSessionId
         */
        public ForwardTupleThread(TuplePacket packet, SessionId ourSessionId) {
            super(packet, ourSessionId);
        }

        public void execute() throws IMCException {
            try {
                ProtocolStack route = RouteFinderState.findRoute(
                        tuplePacket.Dest.getSessionId(), sessionManager,
                        waitingForResponse);

                if (route != null) {
                    TupleOpState.writePacket(route, tuplePacket);
                } else {
                    /* if we're here we failed */
                    System.err.println(ourSessionId + ": cannot route to "
                            + tuplePacket.Dest);

                    cannotForward(tuplePacket, ourSessionId);
                }
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    /**
     * The thread that is blocked waiting to find a route to send an out
     * response.
     * 
     * @author Lorenzo Bettini
     */
    public class ResponseOutThread extends ForwardTupleThread {

        /**
         * @param packet
         * @param ourSessionId
         */
        public ResponseOutThread(TuplePacket packet, SessionId ourSessionId) {
            super(packet, ourSessionId);
        }

        public void execute() throws IMCException {
            try {
                ProtocolStack route = RouteFinderState.findRoute(
                        tuplePacket.Source.getSessionId(), sessionManager,
                        waitingForResponse);

                if (route != null) {
                    responseOut(tuplePacket, route);
                } else {
                    /* if we're here we failed */
                    System.err.println(ourSessionId
                            + ": cannot route for response to "
                            + tuplePacket.Source);
                }
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    /**
     * The thread that is blocked waiting to find a route to send an eval
     * response.
     * 
     * @author Lorenzo Bettini
     */
    public class ResponseEvalThread extends ForwardTupleThread {
        String error;

        /**
         * @param packet
         * @param ourSessionId
         */
        public ResponseEvalThread(TuplePacket packet, SessionId ourSessionId,
                String error) {
            super(packet, ourSessionId);
            this.error = error;
        }

        public void execute() throws IMCException {
            try {
                ProtocolStack route = RouteFinderState.findRoute(
                        tuplePacket.Source.getSessionId(), sessionManager,
                        waitingForResponse);

                if (route != null) {
                    responseEval(tuplePacket, route, error);
                } else {
                    /* if we're here we failed */
                    System.err.println(ourSessionId
                            + ": cannot route for response to "
                            + tuplePacket.Source);
                }
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    /**
     * The thread that is blocked waiting to find a route to send a response
     * concerning a retrieve tuple operation.
     * 
     * @author Lorenzo Bettini
     */
    public class ResponseTupleThread extends ForwardTupleThread {
        boolean result;

        /**
         * @param packet
         * @param ourSessionId
         */
        public ResponseTupleThread(TuplePacket packet, SessionId ourSessionId,
                boolean result) {
            super(packet, ourSessionId);
            this.result = result;
        }

        public void execute() throws IMCException {
            try {
                ProtocolStack route = RouteFinderState.findRoute(
                        tuplePacket.Source.getSessionId(), sessionManager,
                        waitingForResponse);

                if (route != null) {
                    responseTuple(tuplePacket, result, route);
                } else {
                    /* if we're here we failed */
                    System.err.println(ourSessionId
                            + ": cannot route for response to "
                            + tuplePacket.Source);

                    /*
                     * if it was a successful in, we must put the tuple back
                     */
                    if (result
                            && tuplePacket.operation.equals(TuplePacket.IN_S)) {
                        tupleSpace.out(tuplePacket.tuple);
                    }
                }
            } catch (Exception e) {
                throw new IMCException(e);
            }
        }
    }

    TupleSpace tupleSpace;

    /**
     * The routing table used to dispatch responses.
     */
    RoutingTable routingTable;

    /**
     * To forward tuple packets if unable to find a route.
     */
    protected SessionManager sessionManager;

    /**
     * The table that associates process names to responses.
     */
    protected WaitingForResponse<Response<String>> waitingForResponse;

    /**
     * Where the threads waiting for a matching tuple or for a route are stored.
     */
    ThreadContainer waitingThreads = new ThreadContainer();

    /**
     * To execute a process received, if null then we don't accept process from
     * remote sites.
     */
    protected ExecutionEngine executionEngine = null;

    /**
     * @param tupleSpace
     * @param routingTable
     * @param sessionManager
     * @param executionEngine
     *            To execute a process received, if null then we don't accept
     *            process from remote sites.
     * @param waitingForResponse
     */
    public TupleOpManager(TupleSpace tupleSpace, RoutingTable routingTable,
            SessionManager sessionManager, ExecutionEngine executionEngine,
            WaitingForResponse<Response<String>> waitingForResponse) {
        this.tupleSpace = tupleSpace;
        this.routingTable = routingTable;
        this.sessionManager = sessionManager;
        this.executionEngine = executionEngine;
        this.waitingForResponse = waitingForResponse;
    }

    /**
     * Handles the passed TuplePacket. If necessary, it simply forwards the
     * packet to the real destination.
     * 
     * @param tuplePacket
     *            The TuplePacket we have to handle
     * @param ourSessionId
     *            The SessionId we are working for. If it's not null we must
     *            check it and see if it's the same of the destination of the
     *            TuplePacket; if not, then we have to forward it.
     * @throws IOException
     * @throws ProtocolException
     * @throws InterruptedException
     */
    public void handle(TuplePacket tuplePacket, SessionId ourSessionId)
            throws ProtocolException, IOException, InterruptedException {
        if (checkForward(tuplePacket, ourSessionId))
            return; // already forwarded.

        try {
            if (tuplePacket.operation.equals(TuplePacket.OUT_S)) {
                tupleSpace.out(tuplePacket.tuple);

                ProtocolStack protocolStack = routingTable
                        .getProtocolStack(tuplePacket.Source.getSessionId());
                if (protocolStack != null) {
                    responseOut(tuplePacket, protocolStack);
                } else {
                    /*
                     * spawn a concurrent thread waiting to find the route for
                     * response
                     */
                    waitingThreads.addAndStart(new ResponseOutThread(
                            tuplePacket, ourSessionId));
                }
            } else if (tuplePacket.operation.equals(TuplePacket.IN_S)
                    || tuplePacket.operation.equals(TuplePacket.READ_S)) {
                if (!findMatchingTuple(tuplePacket, false,
                        !tuplePacket.blocking, ourSessionId)
                        && (tuplePacket.blocking || tuplePacket.timeout > 0)) {
                    /* spawn a concurrent thread waiting for the tuple */
                    waitingThreads.addAndStart(new TupleThread(tuplePacket,
                            ourSessionId));
                }
            } else if (tuplePacket.operation.equals(TuplePacket.TUPLEBACK_S)) {
                /* simply inserts the tuple and nothing else */
                tupleSpace.out(tuplePacket.tuple);
            } else if (tuplePacket.operation.equals(TuplePacket.EVAL_S)) {
                String error = null;

                if (executionEngine == null) {
                    /* sorry we don't accept remote processes */
                    error = ourSessionId
                            + ": execution engine for remote processes not set";
                } else {
                    try {
                        executionEngine
                                .runProcess((KlavaProcess) (tuplePacket.tuple
                                        .getItem(0)));
                    } catch (KlavaException e) {
                        error = ourSessionId + ": " + e.getMessage();
                    } catch (ClassCastException e) {
                        error = ourSessionId + ": " + e.getMessage();
                    }
                }

                /*
                 * use the original source for the destination (this may be
                 * different from the source of the stack due to forwarding)
                 */
                ProtocolStack protocolStack = routingTable
                        .getProtocolStack(tuplePacket.Source.getSessionId());
                if (protocolStack != null) {
                    responseEval(tuplePacket, protocolStack, error);
                } else {
                    /*
                     * spawn a concurrent thread waiting to find the route for
                     * response
                     */
                    waitingThreads.addAndStart(new ResponseEvalThread(
                            tuplePacket, ourSessionId, error));
                }
            } else {
                throw new ProtocolException("unknown operation: "
                        + tuplePacket.operation);
            }
        } catch (IMCException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @param tuplePacket
     * @param protocolStack
     * @param error
     * @throws ProtocolException
     * @throws IOException
     */
    private void responseEval(TuplePacket tuplePacket,
            ProtocolStack protocolStack, String error)
            throws ProtocolException, IOException {
        if (error != null) {
            /* there was an error, let's communicate it to the source */
            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseEval(marshaler, tuplePacket.Source
                    .getSessionId(), tuplePacket.processName, error);
            protocolStack.releaseMarshaler(marshaler);
        } else {
            /* everything went fine, let's communicate it to the source */
            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseEval(marshaler, tuplePacket.Source
                    .getSessionId(), tuplePacket.processName,
                    ResponseState.OK_S);
            protocolStack.releaseMarshaler(marshaler);
        }
    }

    /**
     * Sends a positive out response.
     * 
     * @param tuplePacket
     * @param protocolStack
     * @throws ProtocolException
     * @throws IOException
     */
    private void responseOut(TuplePacket tuplePacket,
            ProtocolStack protocolStack) throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();
        /*
         * use the original source for the destination (this may be different
         * from the source of the stack due to forwarding)
         */
        ResponseState.sendResponseOut(marshaler, tuplePacket.Source
                .getSessionId(), tuplePacket.processName, true);
        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * Checks whether this TuplePacket must be forwarded to another destination
     * (the actual destination is not me). If it forwards to another destination
     * returns true, false otherwise.
     * 
     * @param tuplePacket
     * @param ourSessionId
     * @return true if the packet is forwarded, false otherwise
     * @throws ProtocolException
     * @throws IOException
     */
    protected boolean checkForward(TuplePacket tuplePacket,
            SessionId ourSessionId) throws ProtocolException, IOException {
        /* see if it's a packet for ourselves or for someone else */
        if (ourSessionId != null
                && !ourSessionId.equals(tuplePacket.Dest.getSessionId())) {
            /* we must forward the packet without handling it */
            ProtocolStack forward = routingTable
                    .getProtocolStack(tuplePacket.Dest.getSessionId());
            if (forward != null) {
                /* OK we can forward it */
                TupleOpState.writePacket(forward, tuplePacket);
            } else {
                try {
                    waitingThreads.addAndStart(new ForwardTupleThread(
                            tuplePacket, ourSessionId));
                } catch (IMCException e) {
                    throw new ProtocolException(e);
                }
            }

            /*
             * in any case it's not our packet and there's nothing else we can
             * do
             */
            return true;
        } else {
            return false;
        }
    }

    /**
     * Notifies the original sender that we're unable to forward this
     * TuplePacket.
     * 
     * @param tuplePacket
     * @param ourSessionId
     * @throws ProtocolException
     * @throws IOException
     */
    protected void cannotForward(TuplePacket tuplePacket, SessionId ourSessionId)
            throws ProtocolException, IOException {
        /* cannot forward it, so notify the original sender */
        ProtocolStack source = routingTable.getProtocolStack(tuplePacket.Source
                .getSessionId());
        if (source == null) {
            /* this is unlikely, but... */
            throw new ProtocolException("lost connection with "
                    + tuplePacket.Source + "?");
        }

        if (tuplePacket.operation.equals(TuplePacket.OUT_S)) {
            ResponseState.sendResponseOut(source, tuplePacket.Source
                    .getSessionId(), tuplePacket.processName, false);
        } else if (tuplePacket.operation.equals(TuplePacket.IN_S)
                || tuplePacket.operation.equals(TuplePacket.READ_S)) {
            ResponseState.sendResponseTuple(source, tuplePacket.operation,
                    tuplePacket.tuple, ourSessionId, tuplePacket.Source
                            .getSessionId(), tuplePacket.processName, false);
        }
    }

    /**
     * Tries to find a matching tuple in the tuple space.
     * 
     * @param tuplePacket
     * @param block
     *            Whether to be blocked waiting for a matching tuple.
     * @param sendFailResult
     *            Whether to send the result in case of failure
     * @param ourSessionId
     *            The SessionId of the node we're acting for
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean findMatchingTuple(TuplePacket tuplePacket, boolean block,
            boolean sendFailResult, SessionId ourSessionId)
            throws ProtocolException, IOException, InterruptedException {
        boolean result = false;

        if (tuplePacket.operation.equals(TuplePacket.IN_S)) {
            if (block) {
                if (tuplePacket.timeout > 0) {
                    result = tupleSpace.in_t(tuplePacket.tuple,
                            tuplePacket.timeout);
                } else {
                    result = tupleSpace.in(tuplePacket.tuple);
                }
            } else {
                result = tupleSpace.in_nb(tuplePacket.tuple);
            }
        } else if (tuplePacket.operation.equals(TuplePacket.READ_S)) {
            if (block) {
                if (tuplePacket.timeout > 0) {
                    result = tupleSpace.read_t(tuplePacket.tuple,
                            tuplePacket.timeout);
                } else {
                    result = tupleSpace.read(tuplePacket.tuple);
                }
            } else {
                result = tupleSpace.read_nb(tuplePacket.tuple);
            }
        }

        if (!result && !sendFailResult)
            return false;

        ProtocolStack protocolStack = routingTable
                .getProtocolStack(tuplePacket.Source.getSessionId());
        if (protocolStack != null) {
            responseTuple(tuplePacket, result, protocolStack);
        } else {
            /*
             * spawn a concurrent thread waiting to find the route for response
             */
            try {
                waitingThreads.addAndStart(new ResponseTupleThread(tuplePacket,
                        ourSessionId, result));
            } catch (IMCException e) {
                throw new ProtocolException(e);
            }
        }

        return result;
    }

    /**
     * Sends a response concerning a retrieve tuple operation
     * 
     * @param tuplePacket
     * @param result
     * @param protocolStack
     * @throws ProtocolException
     * @throws IOException
     */
    private void responseTuple(TuplePacket tuplePacket, boolean result,
            ProtocolStack protocolStack) throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();

        Session session = protocolStack.getSession();
        if (session == null)
            throw new ProtocolException("null session");
        SessionId from = session.getLocalEnd();

        /*
         * use the original source for the destination (this may be different
         * from the source of the stack due to forwarding) and our id as the
         * source of this response
         */
        if (result) {
            ResponseState.sendResponseTuple(marshaler, tuplePacket.operation,
                    tuplePacket.tuple, from, tuplePacket.Source.getSessionId(),
                    tuplePacket.processName, result);
        } else {
            ResponseState.sendResponseTuple(marshaler,
                    TuplePacket.TUPLEABSENT_S, tuplePacket.tuple, from,
                    tuplePacket.Source.getSessionId(), tuplePacket.processName,
                    result);
        }

        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * @return Returns the waitingThreads.
     */
    final public ThreadContainer getWaitingThreads() {
        return waitingThreads;
    }

    /**
     * @param waitingThreads
     *            The waitingThreads to set.
     */
    final public void setWaitingThreads(ThreadContainer waitingThreads) {
        this.waitingThreads = waitingThreads;
    }

    /**
     * On closing we make sure to interrupt all the possible waiting threads.
     * 
     * @throws IMCException 
     */
    public void close() throws IMCException {
        waitingThreads.close();
    }
}
