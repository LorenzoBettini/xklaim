/**
 * 
 */
package klava.proto;

import java.io.IOException;
import java.util.Enumeration;

import klava.WaitingForResponse;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.CollectableThread;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManager;
import org.mikado.imc.topology.ThreadContainer;

/**
 * Responds to route finding requests, and if not able tries to query all the
 * other nodes it is connected to.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class RouteFinderState extends RequestState {
    /**
     * @author bettini
     * 
     */
    public class RouteFinderThread extends CollectableThread {
        SessionId toFind;

        SessionId from;

        String process;

        public RouteFinderThread(SessionId toFind, SessionId from,
                String process) {
            this.toFind = toFind;
            this.from = from;
            this.process = process;
        }

        /**
         * Queries all the nodes it is connected to in order to find a route.
         * 
         * @see org.mikado.imc.topology.CollectableThread#execute()
         */
        @Override
        public void execute() throws IMCException {
            try {
                ProtocolStack route = findRoute(toFind, sessionManager,
                        waitingForResponse);

                /* if we're here we have a response */
                ProtocolStack destination = routingTable.getProtocolStack(from);

                if (destination == null) {
                    /* unlikely but must check and we cannot do much */
                    System.err.println("lost route with " + from);
                    return;
                }

                ResponseState.sendResponseOkError(destination, HASROUTE_S,
                        (route != null ? ResponseState.OK_S
                                : ResponseState.FAIL_S), from, process);
            } catch (ProtocolException e) {
                // we can't do much
                e.printStackTrace();
            } catch (IOException e) {
                // we can't do much
                e.printStackTrace();
            } catch (InterruptedException e) {
                // we can't do much
                e.printStackTrace();
            }
        }

    }

    /**
     * The routing table used to dispatch responses.
     */
    protected RoutingTable routingTable;

    /**
     * To forward tuple packets if unable to find a route.
     */
    protected SessionManager sessionManager;

    /**
     * The table that associates process names to responses.
     */
    protected WaitingForResponse<Response<String>> waitingForResponse;

    public static final String HASROUTE_S = "HASROUTE";

    /**
     * @param routingTable
     * @param sessionManager
     * @param waitingForResponse
     */
    public RouteFinderState(RoutingTable routingTable,
            SessionManager sessionManager,
            WaitingForResponse<Response<String>> waitingForResponse) {
        this.routingTable = routingTable;
        this.sessionManager = sessionManager;
        this.waitingForResponse = waitingForResponse;
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
     * Where the threads waiting for a matching tuple are stored.
     */
    ThreadContainer waitingThreads = new ThreadContainer();

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        /* this should read the FROM and PROCESS fields */
        super.enter(param, transmissionChannel);

        UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

        try {
            SessionId sessionId = SessionId.parseSessionId(unMarshaler
                    .readStringLine());

            if (routingTable.hasRoute(sessionId)) {
                ProtocolStack destination = routingTable.getProtocolStack(from);

                if (destination == null) {
                    /* unlikely but must check */
                    throw new ProtocolException("lost route with " + from);
                }

                /* OK send the response */
                ResponseState.sendResponseOkError(destination, HASROUTE_S,
                        ResponseState.OK_S, from, process);
            } else {
                /*
                 * must ask in turns to others but we'll use a thread for this
                 * otherwise we cannot serve other requests
                 */
                waitingThreads.addAndStart(new RouteFinderThread(sessionId,
                        from, process));
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        } catch (IMCException e) {
            throw new ProtocolException(e);
        }
    }

    public static void sendRouteRequest(ProtocolStack protocolStack,
            SessionId from, String processName, SessionId destination)
            throws ProtocolException, IOException {
        Marshaler marshaler = protocolStack.createMarshaler();

        sendRouteRequest(marshaler, from, processName, destination);

        protocolStack.releaseMarshaler(marshaler);
    }

    public static void sendRouteRequest(Marshaler marshaler, SessionId from,
            String processName, SessionId destination) throws IOException {
        marshaler.writeStringLine(HASROUTE_S);

        sendRequest(marshaler, from, processName);

        /* the destination we search for */
        marshaler.writeStringLine(destination.toString());
    }

    /**
     * Try to find a route for the specified SessionId, by using the connections
     * contained in the specified SessionManager. It uses waitingForResponse for
     * waiting for a response. It returns the ProtocolStack to communicate with
     * the node that has a route for the specified SessionId or null if a route
     * cannot be found.
     * 
     * @param toFind
     * @param sessionManager
     * @param waitingForResponse
     * @return The ProtocolStack to communicate with the node that has a route
     *         for the specified SessionId or null if a route cannot be found.
     * @throws ProtocolException
     * @throws IOException
     * @throws InterruptedException
     */
    public static ProtocolStack findRoute(SessionId toFind,
            SessionManager sessionManager,
            WaitingForResponse<Response<String>> waitingForResponse)
            throws ProtocolException, IOException, InterruptedException {
        /* query all the nodes we're connected to */
        Enumeration<ProtocolStack> stacks = sessionManager.getStacks();
        String mySelf = Thread.currentThread().getName();

        while (stacks.hasMoreElements()) {
            ProtocolStack protocolStack = stacks.nextElement();
            Response<String> response = new Response<String>();
            waitingForResponse.put(mySelf, response);

            /* we make the request and we are the source */
            sendRouteRequest(protocolStack, protocolStack.getSession()
                    .getLocalEnd(), mySelf, toFind);

            /* wait for response */
            response.waitForResponse();

            if (response.error == null
                    && response.responseContent.equals(ResponseState.OK_S)) {
                /* OK we found it! */
                return protocolStack;
            }
        }

        /* if we're here we didn't find it */
        return null;
    }

    /**
     * @return Returns the waitingThreads.
     */
    public final ThreadContainer getWaitingThreads() {
        return waitingThreads;
    }

    /**
     * @param waitingThreads
     *            The waitingThreads to set.
     */
    public final void setWaitingThreads(ThreadContainer waitingThreads) {
        this.waitingThreads = waitingThreads;
    }

}
