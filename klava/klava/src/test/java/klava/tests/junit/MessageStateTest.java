/*
 * Created on Nov 3, 2005
 */
package klava.tests.junit;

import java.io.IOException;

import junit.framework.TestCase;
import klava.Environment;
import klava.KBoolean;
import klava.KInteger;
import klava.KString;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;
import klava.WaitingForResponse;
import klava.proto.MessageState;
import klava.proto.MessageProtocolLayer;
import klava.proto.Response;
import klava.proto.ResponseState;
import klava.proto.RouteFinderState;
import klava.proto.TupleOpManager;
import klava.proto.TupleOpState;
import klava.proto.TuplePacket;
import klava.proto.TupleResponse;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.topology.ProcessContainer;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManager;


/**
 * Tests for the MessageState class
 * 
 * @author Lorenzo Bettini
 */
public class MessageStateTest extends TestCase {
    ProtocolStack protocolStack;

    ProtocolLayerSharedBuffer protocolLayer;

    PhysicalLocality dest;

    PhysicalLocality source;

    Session sourceToDest;

    Session destToSource;

    protected void setUp() throws Exception {
        super.setUp();
        protocolLayer = new ProtocolLayerSharedBuffer();
        protocolStack = new ProtocolStack(protocolLayer);
        protocolStack.insertFirstLayer(new MessageProtocolLayer());
        /* we need to create a fake session */
        dest = new PhysicalLocality("127.0.0.1", 9999);
        source = new PhysicalLocality("127.0.0.1", 10000);
        sourceToDest = new Session(protocolLayer, source.getSessionId(), dest
                .getSessionId());
        /* the inverted session */
        destToSource = new Session(protocolLayer, dest.getSessionId(), source
                .getSessionId());
        /*
         * this is the case when the protocol stack is used for receiving
         * messages. If it's used for receiving response we must use
         * sourceToDest
         */
        protocolStack.setSession(destToSource);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Sends an OUT message and waits for the response.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws KlavaMalformedPhyLocalityException
     * @throws InterruptedException
     */
    public void testOut() throws ProtocolException, IOException,
            KlavaMalformedPhyLocalityException, InterruptedException {
        System.out.println("*** testOut ***");

        Tuple tuple = new Tuple(new KString("foo"), new Integer(10));

        TuplePacket tuplePacket = new TuplePacket(dest, source,
                TuplePacket.OUT_S, tuple);

        String processName = "waiting process";
        tuplePacket.processName = processName;

        Marshaler marshaler = protocolStack.createMarshaler();

        TupleOpState tupleOpState = new TupleOpState();
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState.setProtocolStack(protocolStack);
        tupleOpState.setDoRead(false);
        tupleOpState.enter(null, new TransmissionChannel(marshaler));

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("sent:\n" + tuplePacket);

        RoutingTable routingTable = new RoutingTable();
        /*
         * we bind both addresses (source and dest) to the same protocol stack
         * in this test
         */
        routingTable.addRoute(source.getSessionId(), protocolStack);
        routingTable.addRoute(dest.getSessionId(), protocolStack);

        TupleSpace tupleSpace = new TupleSpaceVector();

        WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();

        TupleOpManager tupleOpManager = new TupleOpManager(tupleSpace,
                routingTable, new SessionManager(), null, waitingForResponse);

        /* the response shared with the thread */
        Response<String> response = new Response<String>();

        /* the concurrent thread that'll wait for the response */
        WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                processName);
        waitingForResponseProcess.response = response;
        waitingForResponse.put(waitingForResponseProcess.getName(), response);
        waitingForResponseProcess.start();

        MessageState klavaMessageState = new MessageState();
        klavaMessageState.setProtocolStack(protocolStack);
        klavaMessageState.setTupleOpManager(tupleOpManager);
        /* the response state will share the same WaitingForResponse */
        klavaMessageState.setWaitingForResponse(waitingForResponse);
        klavaMessageState.setRoutingTable(routingTable);
        klavaMessageState.enter(null, null);

        /*
         * now we should read the response from the same stack so we must switch
         * the session
         */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        waitingForResponseProcess.join();

        assertTrue(response.error == null);
        assertTrue(response.responseContent != null);

        System.out.println("response: " + response.responseContent);

        assertEquals(ResponseState.OK_S, response.responseContent);
    }

    /**
     * Sends an HASROUTE message and waits for the response.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws KlavaMalformedPhyLocalityException
     * @throws InterruptedException
     */
    public void testRouteFinder() throws ProtocolException, IOException,
            KlavaMalformedPhyLocalityException, InterruptedException {
        System.out.println("*** testRouteFinder ***");

        String processName = "waiting process";
        SessionId toFind = new IpSessionId("localhost", 50000);

        Marshaler marshaler = protocolStack.createMarshaler();

        RouteFinderState.sendRouteRequest(marshaler, source.getSessionId(),
                processName, toFind);

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("searching for: " + toFind);

        RoutingTable routingTable = new RoutingTable();
        /*
         * we bind both addresses (source and dest) to the same protocol stack
         * in this test, and also add the same route for the sessionId to find
         */
        routingTable.addRoute(source.getSessionId(), protocolStack);
        routingTable.addRoute(dest.getSessionId(), protocolStack);
        routingTable.addRoute(toFind, protocolStack);

        /* the response shared with the thread */
        Response<String> response = new Response<String>();

        /* the concurrent thread that'll wait for the response */
        WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                processName);
        waitingForResponseProcess.response = response;
        WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();
        waitingForResponse.put(waitingForResponseProcess.getName(), response);
        waitingForResponseProcess.start();

        MessageState klavaMessageState = new MessageState();
        klavaMessageState.setProtocolStack(protocolStack);
        /* the response state will share the same WaitingForResponse */
        klavaMessageState.setWaitingForResponse(waitingForResponse);
        klavaMessageState.setRoutingTable(routingTable);
        klavaMessageState.enter(null, null);

        /*
         * now we should read the response from the same stack so we must switch
         * the session
         */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        waitingForResponseProcess.join();

        assertTrue(response.error == null);
        assertTrue(response.responseContent != null);

        System.out.println("response: " + response.responseContent);

        assertEquals(ResponseState.OK_S, response.responseContent);
    }

    /**
     * Sends an IN message and waits for the response.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws KlavaMalformedPhyLocalityException
     * @throws InterruptedException
     */
    public void testIn() throws ProtocolException, IOException,
            KlavaMalformedPhyLocalityException, InterruptedException {
        System.out.println("*** testIn ***");

        execTestTupleOperation(TuplePacket.IN_S);
    }

    /**
     * Sends an READ message and waits for the response.
     * 
     * @throws ProtocolException
     * @throws IOException
     * @throws KlavaMalformedPhyLocalityException
     * @throws InterruptedException
     */
    public void testRead() throws ProtocolException, IOException,
            KlavaMalformedPhyLocalityException, InterruptedException {
        System.out.println("*** testRead ***");

        execTestTupleOperation(TuplePacket.READ_S);
    }

    private void execTestTupleOperation(String operation)
            throws KlavaMalformedPhyLocalityException, ProtocolException,
            IOException, InterruptedException {
        Tuple tuple = new Tuple(new KString(), new Integer(10));
        TuplePacket tuplePacket = new TuplePacket(dest, source, operation,
                tuple);

        String processName = "waiting process";
        tuplePacket.processName = processName;

        Marshaler marshaler = protocolStack.createMarshaler();

        TupleOpState tupleOpState = new TupleOpState();
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState.setProtocolStack(protocolStack);
        tupleOpState.setDoRead(false);
        tupleOpState.enter(null, new TransmissionChannel(marshaler));

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("sent:\n" + tuplePacket);

        RoutingTable routingTable = new RoutingTable();
        /*
         * we bind both addresses (source and dest) to the same protocol stack
         * in this test
         */
        routingTable.addRoute(source.getSessionId(), protocolStack);
        routingTable.addRoute(dest.getSessionId(), protocolStack);

        TupleSpace tupleSpace = new TupleSpaceVector();
        Tuple outTuple = new Tuple(new KString("foo"), new Integer(10));
        tupleSpace.out(outTuple);

        TupleOpManager tupleOpManager = new TupleOpManager(tupleSpace,
                routingTable, new SessionManager(),
                null, new WaitingForResponse<Response<String>>());

        /* the response shared with the thread */
        TupleResponse tupleResponse = new TupleResponse();

        /* the concurrent thread that'll wait for the response */
        WaitingForResponseProcess<TupleResponse> waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                processName);
        waitingForTupleProcess.response = tupleResponse;
        WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();
        waitingForTuple.put(waitingForTupleProcess.getName(), tupleResponse);
        waitingForTupleProcess.start();

        MessageState klavaMessageState = new MessageState();
        klavaMessageState.setProtocolStack(protocolStack);
        klavaMessageState.setTupleOpManager(tupleOpManager);
        /* the response state will share the same WaitingForResponse */
        klavaMessageState.setWaitingForTuple(waitingForTuple);
        klavaMessageState.setRoutingTable(routingTable);
        klavaMessageState.setEnvironment(new Environment());
        klavaMessageState.enter(null, null);

        /* now we should read the response from the same stack */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        waitingForTupleProcess.join();

        assertTrue(tupleResponse.error == null);
        assertTrue(tupleResponse.responseContent != null);

        System.out.println("response: " + tupleResponse.responseContent);

        /* now searches for a non existing matching tuple */

        /* the response shared with the thread */
        tupleResponse = new TupleResponse();

        waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                processName);
        waitingForTupleProcess.response = tupleResponse;
        waitingForTuple.put(waitingForTupleProcess.getName(), tupleResponse);
        waitingForTupleProcess.start();

        tuple = new Tuple(new KBoolean(), new KInteger(10));
        /*
         * we want to check whether the retrieved tuple is exactly what we will
         * insert in the tuple space
         */
        tuple.setHandleRetrieved(true);

        tuplePacket = new TuplePacket(dest, source, operation, tuple);

        tuplePacket.processName = processName;

        marshaler = protocolStack.createMarshaler();

        tupleOpState = new TupleOpState();
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState.setProtocolStack(protocolStack);
        tupleOpState.setDoRead(false);
        tupleOpState.enter(null, new TransmissionChannel(marshaler));

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("sent:\n" + tuplePacket);

        /* read the request */
        protocolStack.setSession(destToSource);
        klavaMessageState.enter(null, null);

        /* now we should read the response from the same stack */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        waitingForTupleProcess.join();

        assertTrue(tupleResponse.error != null);
        assertTrue(tupleResponse.responseContent == null);

        System.out.println("error: " + tupleResponse.error);

        /*
         * now searches for a non existing matching tuple but willing to wait
         * for a matching tuple. This tuple will be inserted in the tuple space
         * after sending the request.
         */

        ProcessContainer waitingThreads = tupleOpManager.getWaitingThreads();

        /* If it is not found we want to wait. */
        tuplePacket.blocking = true;

        marshaler = protocolStack.createMarshaler();

        tupleOpState = new TupleOpState();
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState.setProtocolStack(protocolStack);
        tupleOpState.setDoRead(false);
        tupleOpState.enter(null, new TransmissionChannel(marshaler));

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("sent:\n" + tuplePacket);

        /* read the request */
        protocolStack.setSession(destToSource);
        klavaMessageState.enter(null, null);

        /*
         * now insert the tuple and check that the thread spawned by
         * tupleOpManager retrieves it
         */
        Tuple addedTuple = new Tuple(new KBoolean(true), new KInteger(10));
        tupleSpace.out(addedTuple);

        waitingThreads.join();

        /* the response shared with the thread */
        tupleResponse = new TupleResponse();

        waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                processName);
        waitingForTupleProcess.response = tupleResponse;
        waitingForTuple.put(waitingForTupleProcess.getName(), tupleResponse);
        waitingForTupleProcess.start();

        /* read the response */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        assertTrue(tupleResponse.error == null);
        assertTrue(tupleResponse.responseContent != null);

        System.out.println("response: " + tupleResponse.responseContent);

        /*
         * we want to check whether the retrieved tuple is exactly what we will
         * insert in the tuple space
         */
        assertTrue(tupleResponse.responseContent.alreadyRetrieved(addedTuple
                .getTupleId()));

        /*
         * now searches for a non existing matching tuple but willing to wait
         * only for an amount of time, i.e., timeout.
         */
        tuplePacket.tuple = new Tuple(new KString("non existent"));
        tuplePacket.blocking = false; // blocking must not be considered with
        // timeout
        tuplePacket.timeout = 1000; // 1 second

        marshaler = protocolStack.createMarshaler();

        tupleOpState = new TupleOpState();
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState.setProtocolStack(protocolStack);
        tupleOpState.setDoRead(false);
        tupleOpState.enter(null, new TransmissionChannel(marshaler));

        protocolStack.releaseMarshaler(marshaler);

        System.out.println("sent:\n" + tuplePacket);

        /* read the request */
        protocolStack.setSession(destToSource);
        klavaMessageState.enter(null, null);

        waitingThreads.join();

        /* the response shared with the thread */
        tupleResponse = new TupleResponse();

        waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                processName);
        waitingForTupleProcess.response = tupleResponse;
        waitingForTuple.put(waitingForTupleProcess.getName(), tupleResponse);
        waitingForTupleProcess.start();

        /* read the response */
        protocolStack.setSession(sourceToDest);
        klavaMessageState.enter(null, null);

        assertTrue(tupleResponse.error != null);
        assertTrue(tupleResponse.responseContent == null);

        System.out.println("error: " + tupleResponse.error);
    }
}
