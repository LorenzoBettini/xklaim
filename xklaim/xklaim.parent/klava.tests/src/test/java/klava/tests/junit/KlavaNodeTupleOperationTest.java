package klava.tests.junit;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.pipe.ProtocolLayerPipe;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.WaitingForResponse;
import klava.proto.TuplePacket;
import klava.proto.TupleOpState;
import klava.proto.TupleResponse;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;

/**
 * Tests tuple operations performed by {@link KlavaNode}.
 */
public class KlavaNodeTupleOperationTest extends TestCase {

    private static final long WAIT_TIMEOUT = 5000;

    private static class TupleOperationProcess extends KlavaProcess {

        private static final long serialVersionUID = 1L;

        private final KlavaNode node;

        private final ProtocolStack protocolStack;

        private final PhysicalLocality destination;

        private final WaitingForResponse<TupleResponse> waitingForResponse;

        private final TupleResponse response = new TupleResponse();

        private KlavaException klavaException;

        TupleOperationProcess(KlavaNode node, ProtocolStack protocolStack,
                PhysicalLocality destination,
                WaitingForResponse<TupleResponse> waitingForResponse) {
            super("interrupted tuple operation process");
            this.node = node;
            this.protocolStack = protocolStack;
            this.destination = destination;
            this.waitingForResponse = waitingForResponse;
        }

        @Override
        public void executeProcess() {
            try {
                node.tupleOperation(protocolStack, TuplePacket.IN_S,
                        new Tuple(new KString("foo")), true,
                        waitingForResponse, response, -1, destination);
            } catch (KlavaException e) {
                klavaException = e;
            }
        }
    }

    private static class InterruptingProtocolLayer extends ProtocolLayer {

        private volatile boolean interruptAttempted;

        private volatile boolean interruptStatusVisibleDuringCommunication;

        @Override
        public Marshaler doCreateMarshaler(Marshaler marshaler) {
            /*
             * Simulate a cancellation request arriving after the stack has an
             * endpoint marshaler but before TupleOpState writes into it. Without
             * Klava's deferral, the interrupt flag would be visible here and the
             * following NIO pipe write could close the channel.
             */
            interruptAttempted = true;
            Thread.currentThread().interrupt();
            interruptStatusVisibleDuringCommunication =
                    Thread.currentThread().isInterrupted();
            return marshaler;
        }
    }

    public void testInterruptedTupleOperationRemovesWaitingResponse()
            throws Exception {
        KlavaNode node = new KlavaNode();
        WaitingForResponse<TupleResponse> waitingForResponse =
                new WaitingForResponse<TupleResponse>();
        ProtocolStack protocolStack = createProtocolStack();
        PhysicalLocality destination = new PhysicalLocality(protocolStack
                .getSession().getRemoteEnd());
        TupleOperationProcess process = new TupleOperationProcess(node,
                protocolStack, destination, waitingForResponse);

        try {
            process.start();
            waitForRegisteredResponse(waitingForResponse, process);

            process.interrupt();
            process.join(WAIT_TIMEOUT);

            assertFalse("process should have terminated after interrupt",
                    process.isAlive());
            assertFalse("interrupted process must be removed from waiting responses",
                    waitingForResponse.containsKey(process.getName()));
            assertNotNull("interruption should be reported as KlavaException",
                    process.klavaException);
            assertTrue("KlavaException should wrap the InterruptedException",
                    process.klavaException.getCause() instanceof InterruptedException);
        } finally {
            if (process.isAlive()) {
                process.interrupt();
                process.join(WAIT_TIMEOUT);
            }
            node.close();
        }
    }

    public void testInterruptDuringTupleOperationSendIsDeferred()
            throws Exception {
        KlavaNode node = new KlavaNode();
        WaitingForResponse<TupleResponse> waitingForResponse =
                new WaitingForResponse<TupleResponse>();
        ProtocolLayerPipe pipe = new ProtocolLayerPipe();
        InterruptingProtocolLayer interruptingProtocolLayer =
                new InterruptingProtocolLayer();
        ProtocolLayer localPipeEnd = pipe.getProtocolLayer1();
        ProtocolStack protocolStack = new ProtocolStack(
                interruptingProtocolLayer);
        protocolStack.setLowLayer(localPipeEnd);
        protocolStack.setSession(new Session(localPipeEnd,
                new SessionId("pipe", "local"),
                new SessionId("pipe", "remote")));
        PhysicalLocality destination = new PhysicalLocality(protocolStack
                .getSession().getRemoteEnd());
        TupleOperationProcess process = new TupleOperationProcess(node,
                protocolStack, destination, waitingForResponse);

        try {
            process.start();
            process.join(WAIT_TIMEOUT);

            assertFalse("process should have terminated after deferred interrupt",
                    process.isAlive());
            assertTrue("test protocol layer should have requested an interrupt",
                    interruptingProtocolLayer.interruptAttempted);
            assertFalse("interrupt must not be visible while writing to the pipe",
                    interruptingProtocolLayer
                            .interruptStatusVisibleDuringCommunication);
            assertFalse("interrupted process must be removed from waiting responses",
                    waitingForResponse.containsKey(process.getName()));
            assertNotNull("interruption should be reported as KlavaException",
                    process.klavaException);
            assertTrue("KlavaException should wrap the InterruptedException",
                    process.klavaException.getCause() instanceof InterruptedException);

            /*
             * Reaching the peer endpoint proves that the interrupted send did
             * not close the NIO pipe before the tuple-operation packet was
             * written and flushed.
             */
            UnMarshaler unMarshaler = pipe.getProtocolLayer2()
                    .doCreateUnMarshaler(null);
            assertEquals("tuple operation packet should have reached the pipe",
                    TupleOpState.OPERATION_S, unMarshaler.readStringLine());
        } finally {
            if (process.isAlive()) {
                process.interrupt();
                process.join(WAIT_TIMEOUT);
            }
            node.close();
        }
    }

    private ProtocolStack createProtocolStack() throws Exception {
        ProtocolLayer protocolLayer = new ProtocolLayerSharedBuffer();
        ProtocolStack protocolStack = new ProtocolStack(protocolLayer);
        protocolStack.setSession(new Session(protocolLayer, new IpSessionId(
                "localhost", 9999), new IpSessionId("localhost", 10000)));
        return protocolStack;
    }

    private void waitForRegisteredResponse(
            WaitingForResponse<TupleResponse> waitingForResponse,
            TupleOperationProcess process) throws InterruptedException {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT;
        while (!waitingForResponse.containsKey(process.getName())
                && process.isAlive()
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(10); // NOSONAR we need some delay
        }

        assertTrue("tuple operation should register a waiting response",
                waitingForResponse.containsKey(process.getName()));
    }
}
