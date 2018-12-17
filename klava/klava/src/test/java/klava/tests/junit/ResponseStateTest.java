/*
 * Created on Oct 5, 2005
 */
package klava.tests.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.RoutingTable;

import junit.framework.TestCase;
import klava.Environment;
import klava.EnvironmentLogicalLocalityResolver;
import klava.KInteger;
import klava.KlavaMalformedPhyLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.WaitingForResponse;
import klava.proto.LocalityResolverState;
import klava.proto.Response;
import klava.proto.ResponseState;
import klava.proto.RouteFinderState;
import klava.proto.TupleOpState;
import klava.proto.TuplePacket;
import klava.proto.TupleResponse;

/**
 * Tests for the ResponseState class
 * 
 * @author Lorenzo Bettini
 * 
 */
public class ResponseStateTest extends TestCase {
    ProtocolStack protocolStack;

    ProtocolLayer protocolLayer;

    ResponseState responseState;

    Environment environment;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        protocolLayer = new ProtocolLayerSharedBuffer();
        protocolStack = new ProtocolStack(protocolLayer);
        /* we need a fake Session */
        protocolStack.setSession(new Session(protocolLayer, new IpSessionId(
                "localhost", 9999), new IpSessionId("localhost", 10000)));
        responseState = new ResponseState();
        responseState.setProtocolStack(protocolStack);
        /*
         * an empty routing table, this shouldn't be used anyway, since we
         * always specify the right destination
         */
        responseState.setRoutingTable(new RoutingTable());
        environment = new Environment();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testResponseOut() throws InterruptedException {
        try {
            /* the response shared with the thread */
            Response<String> response = new Response<String>();

            /* the concurrent thread that'll wait for the response */
            WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                    "waiting thread");
            waitingForResponseProcess.response = response;
            WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);
            waitingForResponseProcess.start();

            /* the response state will share the same WaitingForResponse */
            responseState.setWaitingForResponse(waitingForResponse);

            /*
             * as the destination we use the local end of the protocolStack,
             * since this will be the same of the local end of the state that's
             * reading the response
             */
            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseOut(marshaler, protocolStack.getSession()
                    .getLocalEnd(), waitingForResponseProcess.getName(), true);
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String res = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.setProtocolStack(protocolStack);
            responseState.enter(null, new TransmissionChannel(unMarshaler));

            waitingForResponseProcess.join();

            assertTrue(response.error == null);
            assertTrue(response.responseContent != null);

            System.out.println("response: " + response.responseContent);

            assertEquals(ResponseState.OK_S, response.responseContent);
        } catch (ProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testOK() throws InterruptedException {
        try {
            // the response shared with the thread
            Response<String> response = new Response<String>();

            // the concurrent thread that'll wait for the response
            WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                    "waiting thread");
            waitingForResponseProcess.response = response;
            WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);
            waitingForResponseProcess.start();

            // the response state will share the same WaitingForResponse
            responseState.setWaitingForResponse(waitingForResponse);

            /*
             * as the destination we use the local end of the protocolStack,
             * since this will be the same of the local end of the state that's
             * reading the response
             */
            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseOkError(marshaler, TuplePacket.EVAL_S,
                    "OK", protocolStack.getSession().getLocalEnd(),
                    waitingForResponseProcess.getName());
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String res = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.setProtocolStack(protocolStack);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            waitingForResponseProcess.join();

            assertTrue(response.error == null);
            assertTrue(response.responseContent != null);

            System.out.println("response: " + response.responseContent);

            assertEquals("OK", response.responseContent);
        } catch (ProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
    
    public void testHasRoute() throws InterruptedException {
        try {
            // the response shared with the thread
            Response<String> response = new Response<String>();

            // the concurrent thread that'll wait for the response
            WaitingForResponseProcess<Response<String>> waitingForResponseProcess = new WaitingForResponseProcess<Response<String>>(
                    "waiting thread");
            waitingForResponseProcess.response = response;
            WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);
            waitingForResponseProcess.start();

            // the response state will share the same WaitingForResponse
            responseState.setWaitingForResponse(waitingForResponse);

            /*
             * as the destination we use the local end of the protocolStack,
             * since this will be the same of the local end of the state that's
             * reading the response
             */
            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseOkError(marshaler, RouteFinderState.HASROUTE_S,
                    "OK", protocolStack.getSession().getLocalEnd(),
                    waitingForResponseProcess.getName());
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String res = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.setProtocolStack(protocolStack);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            waitingForResponseProcess.join();

            assertTrue(response.error == null);
            assertTrue(response.responseContent != null);

            System.out.println("response: " + response.responseContent);

            assertEquals("OK", response.responseContent);
        } catch (ProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testTuple() throws InterruptedException {
        try {
            /* the response shared with the thread */
            TupleResponse tupleResponse = new TupleResponse();

            /* the concurrent thread that'll wait for the response */
            WaitingForResponseProcess<TupleResponse> waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                    "waiting thread");
            waitingForTupleProcess.response = tupleResponse;
            WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();
            waitingForTuple
                    .put(waitingForTupleProcess.getName(), tupleResponse);
            waitingForTupleProcess.start();

            /* the response state will share the same WaitingForResponse */
            responseState.setWaitingForTuple(waitingForTuple);

            /*
             * as the destination we use the local end of the protocolStack,
             * since this will be the same of the local end of the state that's
             * reading the response, and as the source the remote end, for the
             * same reason
             */
            Marshaler marshaler = protocolStack.createMarshaler();
            Tuple tuple = new Tuple(new KInteger(10), new String("foo"));
            ResponseState.sendResponseTuple(marshaler, TuplePacket.IN_S, tuple,
                    protocolStack.getSession().getRemoteEnd(), protocolStack
                            .getSession().getLocalEnd(), waitingForTupleProcess
                            .getName(), true);
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String response = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, response);

            responseState.setProtocolStack(protocolStack);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            waitingForTupleProcess.join();

            assertTrue(tupleResponse.error == null);
            assertTrue(tupleResponse.responseContent != null);

            System.out.println("tuple response: "
                    + tupleResponse.responseContent);

            assertEquals(tupleResponse.responseContent, tuple);

            marshaler = protocolStack.createMarshaler();
            // the process name is intentionally wrong
            ResponseState.sendResponseTuple(marshaler, TuplePacket.READ_S,
                    tuple, protocolStack.getSession().getRemoteEnd(),
                    protocolStack.getSession().getLocalEnd(), "foo", true);
            protocolStack.releaseMarshaler(marshaler);

            unMarshaler = protocolStack.createUnMarshaler();

            // this will be used to read responses from the ResponseState
            ByteArrayOutputStream forResponses = new ByteArrayOutputStream();

            response = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, response);

            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    forResponses), unMarshaler));

            // since it was a READ response, even if the process is
            // non-existence we must not receive anything back
            byte res[] = forResponses.toByteArray();
            assertEquals(res.length, 0);

            marshaler = protocolStack.createMarshaler();
            // the process name is intentionally wrong
            ResponseState.sendResponseTuple(marshaler, TuplePacket.IN_S, tuple,
                    protocolStack.getSession().getRemoteEnd(), protocolStack
                            .getSession().getLocalEnd(), "foo", true);
            protocolStack.releaseMarshaler(marshaler);

            unMarshaler = protocolStack.createUnMarshaler();

            // this will be used to read responses from the ResponseState
            forResponses = new ByteArrayOutputStream();

            response = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, response);

            // we must set a session because it is used by the response state
            // to send the tuple back
            protocolStack.setSession(new Session(protocolLayer,
                    new IpSessionId("localhost", 9999), new IpSessionId(
                            "localhost", 11000)));
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    forResponses), unMarshaler));

            // since it was a IN response, we must receive the tuple back
            res = forResponses.toByteArray();
            assertTrue(res.length > 0);

            System.out.println("res: " + res);

            TupleOpState tupleOpState = new TupleOpState();
            tupleOpState.setDoRead(true);

            tupleOpState.enter(null, new TransmissionChannel(
                    new IMCUnMarshaler(new ByteArrayInputStream(res))));

            TuplePacket tuplePacket = tupleOpState.getTuplePacket();

            System.out.println(tuplePacket.toString());

            assertEquals(tuple, tuplePacket.tuple);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testTupleAbsent() throws InterruptedException {
        try {
            // the response shared with the thread
            TupleResponse tupleResponse = new TupleResponse();

            // the concurrent thread that'll wait for the response
            WaitingForResponseProcess<TupleResponse> waitingForTupleProcess = new WaitingForResponseProcess<TupleResponse>(
                    "waiting thread");
            waitingForTupleProcess.response = tupleResponse;
            WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();
            waitingForTuple
                    .put(waitingForTupleProcess.getName(), tupleResponse);
            waitingForTupleProcess.start();

            // the response state will share the same WaitingForResponse
            responseState.setWaitingForTuple(waitingForTuple);

            Marshaler marshaler = protocolStack.createMarshaler();
            ResponseState.sendResponseTuple(marshaler,
                    TuplePacket.TUPLEABSENT_S, null, protocolStack.getSession()
                            .getRemoteEnd(), protocolStack.getSession()
                            .getLocalEnd(), waitingForTupleProcess.getName(),
                    false);
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String response = unMarshaler.readStringLine();
            assertEquals(ResponseState.RESPONSE_S, response);

            responseState.setProtocolStack(protocolStack);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            waitingForTupleProcess.join();

            assertTrue(tupleResponse.error != null);
            assertTrue(tupleResponse.responseContent == null);

            System.out.println("tuple response: " + tupleResponse.error);

            assertEquals(tupleResponse.error, ResponseState.FAIL_S + ": "
                    + TuplePacket.TUPLEABSENT_S);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public void testLocality() throws InterruptedException {
        try {
            // the response shared with the thread
            Response<PhysicalLocality> response = new Response<PhysicalLocality>();

            // the concurrent thread that'll wait for the response
            WaitingForResponseProcess<Response<PhysicalLocality>> waitingForResponseProcess = new WaitingForResponseProcess<Response<PhysicalLocality>>(
                    "waiting thread");
            waitingForResponseProcess.response = response;
            WaitingForResponse<Response<PhysicalLocality>> waitingForResponse = new WaitingForResponse<Response<PhysicalLocality>>();
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);
            waitingForResponseProcess.start();

            // the response state will share the same WaitingForResponse
            responseState.setWaitingForLocality(waitingForResponse);

            Marshaler marshaler = protocolStack.createMarshaler();
            PhysicalLocality physicalLocality = new PhysicalLocality(
                    "tcp-localhost:9999");
            ResponseState.sendResponseLocality(marshaler, physicalLocality,
                    waitingForResponseProcess.getName(), true);
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String res = unMarshaler.readStringLine();

            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            System.out.println("waiting for process...");
            waitingForResponseProcess.join();

            assertTrue(response.error == null);
            assertTrue(response.responseContent != null);

            System.out
                    .println("locality response: " + response.responseContent);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (KlavaMalformedPhyLocalityException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testResolveLocality() throws InterruptedException {
        try {
            LocalityResolverState localityResolverState = new LocalityResolverState(
                    new EnvironmentLogicalLocalityResolver(environment, null,
                            null));
            localityResolverState.setProtocolStack(protocolStack);

            // the response shared with the thread
            Response<PhysicalLocality> response = new Response<PhysicalLocality>();

            // the concurrent thread that'll wait for the response
            WaitingForResponseProcess<Response<PhysicalLocality>> waitingForResponseProcess = new WaitingForResponseProcess<Response<PhysicalLocality>>(
                    "waiting thread");
            waitingForResponseProcess.response = response;
            WaitingForResponse<Response<PhysicalLocality>> waitingForResponse = new WaitingForResponse<Response<PhysicalLocality>>();
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);
            waitingForResponseProcess.start();

            // the response state will share the same WaitingForResponse
            responseState.setWaitingForLocality(waitingForResponse);

            PhysicalLocality physicalLocality = new PhysicalLocality(
                    "tcp-localhost:9999");
            LogicalLocality logicalLocality = new LogicalLocality("foo");
            environment.try_add(logicalLocality, physicalLocality);

            Marshaler marshaler = protocolStack.createMarshaler();
            LocalityResolverState.sendResolveLocality(marshaler,
                    logicalLocality, waitingForResponseProcess.getName());
            protocolStack.releaseMarshaler(marshaler);

            UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
            String res = unMarshaler.readStringLine();
            assertEquals(LocalityResolverState.RESOLVELOC_S, res);

            localityResolverState.enter(null, new TransmissionChannel(
                    unMarshaler));

            /*
             * we have to wait that the concurrent thread dealing with resolving
             * sends something back
             */
            Thread.sleep(3000);

            unMarshaler = protocolStack.createUnMarshaler();
            res = unMarshaler.readStringLine();

            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            System.out.println("waiting for process...");
            waitingForResponseProcess.join();

            assertTrue(response.error == null);
            assertTrue(response.responseContent != null);

            System.out
                    .println("locality response: " + response.responseContent);

            assertEquals(physicalLocality, response.responseContent);

            /* reinsert the process name in the table */
            waitingForResponse.put(waitingForResponseProcess.getName(),
                    response);

            marshaler = protocolStack.createMarshaler();
            /* now check failure of logical locality resolution */
            LocalityResolverState.sendResolveLocality(marshaler,
                    new LogicalLocality("unexistent"),
                    waitingForResponseProcess.getName());
            protocolStack.releaseMarshaler(marshaler);

            unMarshaler = protocolStack.createUnMarshaler();
            res = unMarshaler.readStringLine();
            assertEquals(LocalityResolverState.RESOLVELOC_S, res);

            localityResolverState.enter(null, new TransmissionChannel(
                    unMarshaler));

            /*
             * we have to wait that the concurrent thread dealing with resolving
             * sends something back
             */
            Thread.sleep(3000);

            unMarshaler = protocolStack.createUnMarshaler();
            res = unMarshaler.readStringLine();

            assertEquals(ResponseState.RESPONSE_S, res);
            responseState.enter(null, new TransmissionChannel(new IMCMarshaler(
                    System.err), unMarshaler));

            assertTrue(response.error != null);

            System.out.println("locality response error: " + response.error);

            /* in case of failure the response is still not null */
            assertTrue(response.responseContent != null);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (KlavaMalformedPhyLocalityException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
