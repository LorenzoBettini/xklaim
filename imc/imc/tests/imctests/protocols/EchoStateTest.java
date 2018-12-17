/*
 * Created on Jan 7, 2005
 *
 */
package imctests.protocols;

import junit.framework.TestCase;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.HTTPTunnelProtocolLayer;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IncrementProtocolLayer;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.PutGetProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * Test the EchoStateTest
 *
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class EchoStateTest extends TestCase {
    /** DOCUMENT ME! */
    UnMarshaler unmarshaler;

    /** DOCUMENT ME! */
    Marshaler marshaler;

    /** DOCUMENT ME! */
    EchoProtocolState echo;

    /** DOCUMENT ME! */
    PipedInputStream in;

    /** DOCUMENT ME! */
    PipedOutputStream out;

    /** DOCUMENT ME! */
    DataInputStream myin;

    /** DOCUMENT ME! */
    DataOutputStream myout;

    /** DOCUMENT ME! */
    EchoListenerThread t;
    
    ProtocolStack protocol;

    /**
     * Constructor for EchoStateTest.
     *
     * @param arg0
     */
    public EchoStateTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {        
        echo = new EchoProtocolState();
        protocol = new ProtocolStack();
        echo.setProtocolStack(protocol);
        in = new PipedInputStream();
        out = new PipedOutputStream();

        PipedInputStream _in = new PipedInputStream();
        PipedOutputStream _out = new PipedOutputStream();

        _in.connect(out);
        _out.connect(in);

        unmarshaler = new IMCUnMarshaler(_in);
        marshaler = new IMCMarshaler(_out);
        protocol.setLowLayer(new ProtocolLayerEndPoint(unmarshaler, marshaler));

        myout = new DataOutputStream(out);
        myin = new DataInputStream(new BufferedInputStream(in));

        t = new EchoListenerThread();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        unmarshaler.close();
        marshaler.close();
        t.interrupt();
        t.join();
        Thread.sleep(500);
    }

    /**
     * Build a long string made up by characters
     *
     * @return 
     */
    static String longString() {
        StringBuffer buffer = new StringBuffer();

        for (int j = 1; j <= 5; ++j) {
            for (int i = 'a'; i != ('z' + 1); ++i)
                buffer.append((char) i);

            for (int i = 'A'; i != ('Z' + 1); ++i)
                buffer.append((char) i);
        }

        return buffer.toString();
    }

    static String readLine(DataInputStream input) throws IOException {
        StringBuffer buffer = new StringBuffer();

        byte b;

        while (true) {
            b = input.readByte();

            if ((b == '\n')) {
                break;
            }

            if (b != '\r')
            	buffer.append((char) b);
        }

        return buffer.toString();
    }

    /**
     * send a string and then waits for the answer and check that they are the
     * same
     *
     * @param s the string to send
     *
     * @throws IOException
     */
    void send_and_receive(String s) throws IOException {
        String sent = s + "\n";
        myout.writeBytes(sent + "\r");
        myout.flush();
        System.out.print("sent: " + sent);

        String received = readLine(myin) + "\n";
        System.out.print("received: " + received);
        assertTrue(received.length() > 1);
        assertEquals(sent, received);
    }

    /**
     * simply send strings and then check that the received strings are the
     * same
     */
    public void testSimpleEcho() {
        t.start();
        try {
            send_and_receive("Hello");
            send_and_receive(longString());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    void send_and_receive_http(ProtocolStack stack,
    		String s) throws IOException, ProtocolException {
        Marshaler mymarshaler = stack.createMarshaler();
        mymarshaler.writeStringLine(s);
        stack.releaseMarshaler(mymarshaler);
    	System.out.print("sent: " + s);

        UnMarshaler myunmarshaler = stack.createUnMarshaler();
        String result = myunmarshaler.readStringLine();
        
        System.out.print("received real result: " + result);
        assertTrue(result.length() > 1);
        assertEquals(s, result);
    }
    
    public void testTunneling() throws ProtocolException {
        try {
            HTTPTunnelProtocolLayer http = new HTTPTunnelProtocolLayer();
            echo.getProtocolStack().insertFirstLayer(http);
            t.start();
            HTTPTunnelProtocolLayer httprequest = new HTTPTunnelProtocolLayer();
            httprequest.setSenderMode(true);
            IMCUnMarshaler myunmarshaler = new IMCUnMarshaler(myin);
            IMCMarshaler mymarshaler = new IMCMarshaler(myout);
            ProtocolLayerEndPoint protocolLayerEndPoint =
            	new ProtocolLayerEndPoint(myunmarshaler, mymarshaler);
            ProtocolStack protocolStack = new ProtocolStack(httprequest);
            protocolStack.insertLayer(protocolLayerEndPoint);
            send_and_receive_http(protocolStack, "Hello");
            send_and_receive_http(protocolStack, longString());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * send a string with the PUT/GET protocol: add a PUT: before sending and
     * strip the GET: upon receptions, and check that the sent and received
     * strings are the same
     *
     * @param s the string to send
     *
     * @throws IOException DOCUMENT ME!
     */
    void send_and_receive_put_get(String s) throws IOException {
        String sent = "PUT:" + s + "\n";
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + sent);

        String received = readLine(myin);
        System.out.println("received: " + received);
        received = received.replaceAll("GET:", "");
        assertEquals(s, received);
    }
    
    /**
     * send a string with the PUT/GET protocol: uses a wrong
     * output format.
     *
     * @param s the string to send
     *
     * @throws IOException DOCUMENT ME!
     */
    void send_and_receive_put_get_error(String s) throws IOException {
        String sent = "PUTfoo:" + s + "\n";
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + sent);

        String received = readLine(myin);
        System.out.println("received: " + received);
        assertEquals(PutGetProtocolLayer.malformed, received);
    }

    /**
     * test the PUT/GET protocol. It inserts the PutGetProtocol Layer in the protocol stack
     * @throws ProtocolException 
     */
    public void testPutGet() throws ProtocolException {
        protocol.insertFirstLayer(new PutGetProtocolLayer());
        t.start();
        try {
            send_and_receive_put_get("Hello");
            send_and_receive_put_get_error("Hello");
            send_and_receive_put_get(longString());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * send a string with the sequence protocol: add a sequence number before
     * sending and strip the returned sequence number upon receptions, and
     * check that the sent and received strings are the same and that the
     * received sequence number is correctly incremented.
     *
     * @param s the string to send
     * @param seq the sequence number
     *
     * @throws IOException
     */
    void send_and_receive_sequence(String s, int seq) throws IOException {
        String sent = s + "\n";
        myout.writeInt(seq);
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + seq + sent);

        int received_seq = myin.readInt();
        String received = readLine(myin);
        System.out.println("received seq: " + received_seq);
        System.out.println("received: " + received);
        assertEquals(s, received);
        assertTrue(received_seq == (seq + 1));
    }

    /**
     * test the sequence protocol. It inserts the IncrementProtocolLayer Layer
     * in the protocol stack
     * @throws ProtocolException 
     */
    public void testSequence() throws ProtocolException {
        protocol.insertFirstLayer(new IncrementProtocolLayer());
        t.start();
        try {
            send_and_receive_sequence("Hello", 1);
            send_and_receive_sequence(longString(), 2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Uses both the PUT/SET and the sequence protocol layers
     *
     * @param s
     * @param seq
     *
     * @throws IOException
     */
    void send_and_receive_put_get_sequence(String s, int seq)
        throws IOException {
        String sent = s + "\n";
        myout.writeBytes("PUT:");
        myout.writeInt(seq);
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + seq + sent);

        String received = PutGetProtocolLayer.getCode(myin);
        System.out.println("received (first line): " + received);

        int received_seq = myin.readInt();
        received = readLine(myin);
        System.out.println("received (second line): " + received);
        System.out.println("received seq: " + received_seq);
        assertEquals(s, received);
        assertTrue(received_seq == (seq + 1));
    }

    /**
     * Uses both the PUT/SET and the sequence protocol layers
     * @throws ProtocolException 
     */
    public void testPutGetSequence() throws ProtocolException {
        System.err.println("*** testPutGetSequence ***");
        protocol.insertFirstLayer(new PutGetProtocolLayer());
        protocol.insertFirstLayer(new IncrementProtocolLayer());
        t.start();
        try {
            send_and_receive_put_get_sequence("Hello", 1);
            send_and_receive_put_get_sequence(longString(), 2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Uses both the sequence protocol and the PUT/SET layers
     *
     * @param s DOCUMENT ME!
     * @param seq DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void send_and_receive_sequence_put_get(String s, int seq)
        throws IOException {
        String sent = s + "\n";
        myout.writeInt(seq);
        myout.writeBytes("PUT:");
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + seq + sent);

        int received_seq = myin.readInt();
        String received = readLine(myin);
        System.out.println("received seq: " + received_seq);
        System.out.println("received: " + received);
        received = received.replaceAll("GET:", "");
        assertEquals(s, received);
        assertTrue(received_seq == (seq + 1));
    }

    /**
     * Uses both the sequence protocol and the PUT/SET layers
     * @throws ProtocolException 
     */
    public void testSequencePutGet() throws ProtocolException {
        protocol.insertFirstLayer(new IncrementProtocolLayer());
        protocol.insertFirstLayer(new PutGetProtocolLayer());
        t.start();
        try {
            send_and_receive_sequence_put_get("Hello", 1);
            send_and_receive_sequence_put_get(longString(), 2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Uses both the sequence protocol and the PUT/SET layers twice
     *
     * @param s DOCUMENT ME!
     * @param seq DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void send_and_receive_sequence_put_get2(String s, int seq)
        throws IOException {
        String sent = s + "\n";
        myout.writeInt(seq);
        myout.writeInt(seq);
        myout.writeBytes("PUT:");
        myout.writeBytes("PUT:");
        myout.writeBytes(sent);
        myout.flush();
        System.out.print("sent: " + seq + seq + sent);

        int received_seq = myin.readInt();
        int received_seq2 = myin.readInt();
        String received = readLine(myin);
        System.out.println("received seq: " + received_seq);
        System.out.println("received seq2: " + received_seq2);
        System.out.println("received: " + received);
        received = received.replaceAll("GET:", "");
        assertEquals(s, received);
        assertTrue(received_seq == (seq + 1));
        assertTrue(received_seq2 == (seq + 1));
    }

    /**
     * Uses both the sequence protocol and the PUT/SET layers twice. So we
     * output:  <tt>(seq)(seq)PUT:PUT:(string)</tt>  and we get
     * <tt>(seq+1)(seq+1)GET:GET:(string)</tt>
     * @throws ProtocolException 
     */
    public void testSequencePutGet2() throws ProtocolException {
        protocol.insertFirstLayer(new IncrementProtocolLayer());
        protocol.insertFirstLayer(new IncrementProtocolLayer());
        protocol.insertFirstLayer(new PutGetProtocolLayer());
        protocol.insertFirstLayer(new PutGetProtocolLayer());
        t.start();
        try {
            send_and_receive_sequence_put_get2("Hello", 1);
            send_and_receive_sequence_put_get2(longString(), 2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    /**
     * DOCUMENT ME!
     *
     * @author bettini
     */
    public class EchoListenerThread extends Thread {
        public void run() {
            while (true) {
                try {
                    echo.enter(null, null);
                } catch (ProtocolException e) {
                    e.printStackTrace();

                    return;
                }
            }
        }
    }
}
