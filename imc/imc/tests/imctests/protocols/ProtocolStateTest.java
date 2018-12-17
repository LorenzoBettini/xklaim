/*
 * Created on Jan 12, 2005
 *
 */
package imctests.protocols;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolComposite;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolState;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.ProtocolThread;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ProtocolStateTest extends TestCase {

    /**
     * @author bettini
     * 
     */
    public class TestStateRes extends ProtocolStateSimple {
        String name;

        /**
         * @param name
         * @param next
         */
        public TestStateRes(String name, String next) {
            super(next);
            this.name = name;
        }

        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            Marshaler marshaler = createMarshaler();
            try {
                marshaler.writeStringLine(param + " " + name);
            } catch (IOException e) {
                fail(e.getMessage());
            }
            releaseMarshaler(marshaler);
        }

    }

    /**
     * @author bettini
     * 
     */
    public class SimpleLayer extends ProtocolLayer {

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.protocols.apis.ProtocolLayer#doUp()
         */
        public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
                throws ProtocolException {
            buffer.append("up called\n");
            return unMarshaler;
        }

    }

    /**
     * @author bettini
     * 
     */
    public class SimpleStateLayer extends ProtocolStateSimple {

        public SimpleStateLayer() {

        }

    }

    /**
     * @author bettini
     * 
     */
    public class TestState extends ProtocolState {
        String name;

        public TestState(String name, String next) {
            super(next);
            this.name = name;
        }

        /**
         * Appends its name to the shared buffer.
         * 
         * @see org.mikado.imc.newprotocols.ProtocolState#enter()
         */
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            buffer.append(name + "\n");
        }
    }

    Protocol protocol;

    StringBuffer buffer;

    TestState start_state = new TestState(Protocol.START, "FIRST");

    TestState end_state = new TestState(Protocol.END, "");

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        protocol = new Protocol(start_state, end_state);
        buffer = new StringBuffer();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testProtocolFail() throws ProtocolException {
        protocol.setState("FIRST", new TestState("FIRST", "SECOND"));
        protocol.setState("SECOND", new TestState("SECOND", "THIRD"));
        try {
            protocol.setState("FIRST", new TestState("THIRD", "END"));
        } catch (ProtocolException e) {
            assertTrue(true);
            // the key "FIRST" is already present so we must get an exception
            return;
        }
        assertTrue(false);
        protocol.start();
        System.out.println(buffer.toString());
    }

    public void testProtocol() throws ProtocolException {
        protocol.setState("FIRST", new TestState("FIRST", "SECOND"));
        protocol.setState("SECOND", new TestState("SECOND", "THIRD"));
        protocol.setState("THIRD", new TestState("THIRD", "END"));
        protocol.start();
        assertEquals(buffer.toString(), "START\nFIRST\nSECOND\nTHIRD\nEND\n");
    }

    public void testSimpleLayer() throws ProtocolException {
        protocol.insertLayer(new SimpleLayer());
        protocol.setState("FIRST", new SimpleStateLayer());
        protocol.start();
        assertEquals(buffer.toString(), "START\nup called\nEND\n");
    }

    public void testProtocolComposite() throws ProtocolException {
        protocol.setState("FIRST", new TestState("FIRST", Protocol.END));
        ProtocolComposite protocolComposite = new ProtocolComposite(
                new TestState("NEW START", ""), new TestState("NEW END", ""),
                protocol);

        protocolComposite.start();
        System.out.println("protocol composite: " + buffer);
        assertEquals(buffer.toString(),
                "NEW START\nSTART\nFIRST\nEND\nNEW END\n");

        /* a further composition */
        buffer = new StringBuffer();
        protocolComposite = new ProtocolComposite(new TestState("NEW START 2",
                ""), new TestState("NEW END 2", ""), protocolComposite);

        protocolComposite.start();
        System.out.println("protocol composite: " + buffer);
        assertEquals(buffer.toString(),
                "NEW START 2\nNEW START\nSTART\nFIRST\nEND\nNEW END\nNEW END 2\n");
    }

    public void testProtocolCompositeNoEnd() throws ProtocolException {
        protocol.setState("FIRST", new TestState("FIRST", Protocol.END));
        /* this first composition does not add an end */
        ProtocolComposite protocolComposite = new ProtocolComposite(
                new TestState("NEW START", ""), protocol);

        protocolComposite.start();
        System.out.println("protocol composite: " + buffer);
        assertEquals(buffer.toString(),
                "NEW START\nSTART\nFIRST\nEND\n");

        /* a further composition */
        buffer = new StringBuffer();
        protocolComposite = new ProtocolComposite(new TestState("NEW START 2",
                ""), new TestState("NEW END 2", ""), protocolComposite);

        protocolComposite.start();
        System.out.println("protocol composite: " + buffer);
        assertEquals(buffer.toString(),
                "NEW START 2\nNEW START\nSTART\nFIRST\nEND\nNEW END 2\n");
    }

    public void testSwitch() throws ProtocolException, IOException {
        PipedOutputStream pipe_out = new PipedOutputStream();
        PipedInputStream pipe_in = new PipedInputStream(pipe_out);

        PipedOutputStream my_pipe_out = new PipedOutputStream();
        PipedInputStream my_pipe_in = new PipedInputStream(my_pipe_out);

        UnMarshaler unMarshaler = new IMCUnMarshaler(pipe_in);
        Marshaler marshaler = new IMCMarshaler(my_pipe_out);

        Marshaler myMarshaler = new IMCMarshaler(pipe_out);
        UnMarshaler myUnMarshaler = new IMCUnMarshaler(my_pipe_in);

        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState();
        ProtocolLayer protocolLayer = new ProtocolLayerEndPoint(unMarshaler,
                marshaler);

        Protocol myProtocol = new Protocol();
        myProtocol.setState(Protocol.START, protocolSwitchState);
        protocolSwitchState.addRequestState("REQ1", new TestStateRes(
                "REQ1STATE", Protocol.START));
        protocolSwitchState.addRequestState("REQ2", new TestStateRes(
                "REQ2STATE", Protocol.START));
        protocolSwitchState.addRequestState("REQ3", new TestStateRes(
                "REQ3STATE", Protocol.START));

        myProtocol.setLowLayer(protocolLayer);

        ProtocolThread protocolThread = new ProtocolThread(myProtocol);
        protocolThread.start();

        String res;

        myMarshaler.writeStringLine("REQ1");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "REQ1 REQ1STATE");

        myMarshaler.writeStringLine("REQ2");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "REQ2 REQ2STATE");

        myMarshaler.writeStringLine("REQ3");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "REQ3 REQ3STATE");

        myMarshaler.writeStringLine("REQFOO");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "FAIL: UNKNOWN REQUEST" + ": REQFOO");
    }

    public void testSwitchString() throws ProtocolException, IOException,
            InterruptedException {
        PipedOutputStream pipe_out = new PipedOutputStream();
        PipedInputStream pipe_in = new PipedInputStream(pipe_out);

        PipedOutputStream my_pipe_out = new PipedOutputStream();
        PipedInputStream my_pipe_in = new PipedInputStream(my_pipe_out);

        UnMarshaler unMarshaler = new IMCUnMarshaler(pipe_in);
        Marshaler marshaler = new IMCMarshaler(my_pipe_out);

        Marshaler myMarshaler = new IMCMarshaler(pipe_out);
        UnMarshaler myUnMarshaler = new IMCUnMarshaler(my_pipe_in);

        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState();
        ProtocolLayer protocolLayer = new ProtocolLayerEndPoint(unMarshaler,
                marshaler);

        Protocol myProtocol = new Protocol();
        myProtocol.setState(Protocol.START, protocolSwitchState);
        protocolSwitchState.addRequestState("REQ1", new TestStateRes(
                "REQ1STATE", Protocol.START));
        protocolSwitchState.addRequestState("REQ2", new TestStateRes(
                "REQ2STATE", Protocol.START));
        protocolSwitchState.addRequestState("REQ3", Protocol.END);

        ProtocolStateSuccess protocolStateSuccess = new ProtocolStateSuccess();
        myProtocol.setState(Protocol.END, protocolStateSuccess);
        myProtocol.setLowLayer(protocolLayer);

        ProtocolThread protocolThread = new ProtocolThread(myProtocol);
        protocolThread.start();

        String res;

        myMarshaler.writeStringLine("REQ1");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "REQ1 REQ1STATE");

        myMarshaler.writeStringLine("REQ2");
        myMarshaler.flush();
        res = myUnMarshaler.readStringLine();
        System.out.println("res: " + res);
        assertEquals(res, "REQ2 REQ2STATE");

        myMarshaler.writeStringLine("REQ3");
        myMarshaler.flush();
        Thread.sleep(500); // give time to the protocol to reach the next state
        assertTrue(protocolStateSuccess.success);
    }

    public void testProtocolLayerSharedBuffer() throws ProtocolException,
            IOException {
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();
        ProtocolStack protocolStack = new ProtocolStack(
                protocolLayerSharedBuffer);

        String s = "FOO";
        int i = 10;

        Marshaler marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine(s);
        marshaler.writeInt(i);
        protocolStack.releaseMarshaler(marshaler);

        UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
        String s1 = unMarshaler.readStringLine();
        int i1 = unMarshaler.readInt();

        assertEquals(s, s1);
        assertEquals(i, i1);

        // now performs another run to check that the previous buffer is cleared
        marshaler = protocolStack.createMarshaler();
        marshaler.writeInt(i);
        marshaler.writeBoolean(true);
        marshaler.writeStringLine(s);
        protocolStack.releaseMarshaler(marshaler);

        unMarshaler = protocolStack.createUnMarshaler();
        i1 = unMarshaler.readInt();
        boolean b = unMarshaler.readBoolean();
        s1 = unMarshaler.readStringLine();

        assertEquals(s, s1);
        assertEquals(i, i1);
        assertTrue(b);
    }

    /**
     * @author bettini
     * 
     */
    public class ProtocolStateSuccess extends ProtocolStateSimple {
        boolean success = false;

        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            success = true; // it means everything went fine
        }
    }
}
