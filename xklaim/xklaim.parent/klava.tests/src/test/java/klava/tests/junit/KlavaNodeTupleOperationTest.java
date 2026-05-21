package klava.tests.junit;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.Session;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.WaitingForResponse;
import klava.proto.TuplePacket;
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
