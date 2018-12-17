/*
 * Created on Jan 17, 2005
 *
 */
package imctests.protocols;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.log.BufferMessagePrinter;
import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactory;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.NodeClassLoader;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.topology.ProtocolThread;

import junit.framework.TestCase;

/**
 * @author bettini
 *
 */
public class ObjectTest extends TestCase {

    StringBuffer received_buffer = new StringBuffer();
    
    ProtocolLayerEndPoint senderLayer;
    ProtocolLayerEndPoint receiverLayer;
    
    MigratingCode received;

    
    /**
     * @author bettini
     *
     */
    public class ObjectSenderState extends ProtocolStateSimple {
        Serializable objectToSend;
        
        /**
         * @param protocolLayer
         */
        public ObjectSenderState(Serializable o) {
            objectToSend = o;
        }

         public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {
            try {
                System.out.println("sending an object: " + objectToSend.toString());
                Marshaler marshaler = createMarshaler();
                marshaler.writeStringLine("OBJECT");
                marshaler.writeReference(objectToSend);
                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }

    }
    
    /**
     * @author bettini
     *
     */
    public class MigratingCodeSenderState extends ProtocolStateSimple {
        MigratingCode objectToSend;
        
        /**
         * @param protocolLayer
         */
        public MigratingCodeSenderState(MigratingCode o) {
            objectToSend = o;
        }

         public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {
            try {
                System.out.println("sending a migrating code: " + objectToSend.toString());
                Marshaler marshaler = createMarshaler();
                marshaler.writeStringLine("CODE");
                try {
                	marshaler.writeMigratingCode(objectToSend);
                } catch (MigrationUnsupported e1) {
                    e1.printStackTrace();
                    fail(e1.getMessage());
                }
                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }

    }
    
    /**
     * @author bettini
     *
     */
    public class ObjectReceiverState extends ProtocolStateSimple {
        /**
         * @param next_state
         */
        public ObjectReceiverState(String next_state) {
            super(next_state);
        }
        
        public ObjectReceiverState() {
            
        }
        
        public void enter(Object param, TransmissionChannel ignore) throws ProtocolException {
            try {
                System.out.println("waiting for an object");
                UnMarshaler unMarshaler = createUnMarshaler();
                String line = unMarshaler.readStringLine();
                if (line.equals("OBJECT")) {
                    Object o = unMarshaler.readReference();
                    System.out.println("received object " + o.toString());
                    received_buffer.append(o.toString());
                } 
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }

    }
    
    /**
     * @author bettini
     *
     */
    public class MigratingCodeReceiverState extends ProtocolStateSimple {
         /**
         * @param next_state
         */
        public MigratingCodeReceiverState(String next_state) {
            super(next_state);
        }
        
        public MigratingCodeReceiverState() {
            
        }
        
        public void enter(Object param, TransmissionChannel ignore) throws ProtocolException {
            try {
                System.out.println("waiting for a migrating object");
                UnMarshaler unMarshaler = createUnMarshaler();
                String line = unMarshaler.readStringLine();
                if (line.equals("CODE")) {
                    MigratingCode o;
                    try {
                        o = unMarshaler.readMigratingCode();
                        System.out.println("received object " + o.toString());
                        received = o;
                        received_buffer.append(o.toString());
                    } catch (MigrationUnsupported e1) {
                        e1.printStackTrace();
                        fail(e1.getMessage());
                    }
                } 
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }

    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        received_buffer = new StringBuffer();
        
        senderLayer = new ProtocolLayerEndPoint();
        receiverLayer = new ProtocolLayerEndPoint();
        
        PipedInputStream pipe_in = new PipedInputStream();
        PipedOutputStream pipe_out = new PipedOutputStream(pipe_in);
        
        // what the receiver writes is read by the sender.
        senderLayer.setUnmarshaler(new IMCUnMarshaler(pipe_in));
        receiverLayer.setMarshaler(new IMCMarshaler(pipe_out));
        
        PipedInputStream pipe_in2 = new PipedInputStream();
        PipedOutputStream pipe_out2 = new PipedOutputStream(pipe_in2);
        
        // what the sender writes is read by the receiver
        senderLayer.setMarshaler(new IMCMarshaler(pipe_out2));
        receiverLayer.setUnmarshaler(new IMCUnMarshaler(pipe_in2));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    void send_and_receive(Protocol sender, Protocol receiver, Object tosend) throws InterruptedException {
        received_buffer = new StringBuffer();
        
        /*
        ProtocolThread protocolThread = new ProtocolThread(receiver);
        protocolThread.start();
        
        try {
            sender.start();
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        protocolThread.join();
        */
        
        try {
            sender.start();
            receiver.start();
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        assertEquals(tosend.toString(), received_buffer.toString());
    }
    
    void send_and_receive_code(Protocol sender, Protocol receiver, Object tosend) throws InterruptedException {
        received_buffer = new StringBuffer();
        
        ProtocolThread protocolThread = new ProtocolThread(receiver);
        protocolThread.start();
        
        try {
            sender.start();
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        protocolThread.join();        
        
        assertEquals(tosend.toString(), received_buffer.toString());
        
        System.out.println("class of the received object: " + received.getClass().getName());
        
        assertEquals(tosend.getClass().getName(), received.getClass().getName());
        
        try {
            MyMigratingCodeImpl casted = (MyMigratingCodeImpl)received;
            // the cast must not succeed since these two classes (although they have the
            // same name) are not compatible, since they are loaded through different
            // class loaders.
            System.out.println("received obj class: " + casted.getClass().getCanonicalName());
            assertTrue(false);
        } catch (ClassCastException e) {
            assertTrue(true);
        }
    }
    
    public void testSimpleObject() throws IOException, ProtocolException, InterruptedException {        
        ObjectSenderState sender = new ObjectSenderState("Hello!");
        ObjectReceiverState receiver = new ObjectReceiverState();
        
        Protocol senderProtocol = new Protocol();
        senderProtocol.insertLayer(senderLayer);        
        senderProtocol.setState(Protocol.START, sender);
        
        Protocol receiverProtocol = new Protocol();
        receiverProtocol.insertLayer(receiverLayer);
        receiverProtocol.setState(Protocol.START, receiver);
        
        send_and_receive(senderProtocol, receiverProtocol, "Hello!");
        
        send_and_receive(senderProtocol, receiverProtocol, "Hello!");
        
        Date date = new Date();
        
        sender.objectToSend = date;
        send_and_receive(senderProtocol, receiverProtocol, date);
                
        date = new Date();
        
        sender.objectToSend = date;
        send_and_receive(senderProtocol, receiverProtocol, date);
        
        ComplexObject complexObject = new ComplexObject(longString());
        sender.objectToSend = complexObject;
        
        send_and_receive(senderProtocol, receiverProtocol, complexObject);
    }
    
    public void testMigratingCode() throws IOException, ProtocolException, InterruptedException {
        MigratingCode code = new MyMigratingCodeImpl();
        
        MigratingCodeSenderState sender = new MigratingCodeSenderState(code);
        MigratingCodeReceiverState receiver = new MigratingCodeReceiverState();
        
        Protocol senderProtocol = new Protocol();
        senderProtocol.insertLayer(senderLayer);        
        senderProtocol.setState(Protocol.START, sender);
        
        Protocol receiverProtocol = new Protocol();
        receiverProtocol.insertLayer(receiverLayer);
        receiverProtocol.setState(Protocol.START, receiver);
        
        JavaByteCodeMigratingCodeFactory javaByteCodeMigratingCodeFactory = 
            new JavaByteCodeMigratingCodeFactory();
        NodeClassLoader classLoader = new NodeClassLoader(true); // force loading
		javaByteCodeMigratingCodeFactory.setNodeClassLoader(classLoader);
		BufferMessagePrinter printer = new BufferMessagePrinter();
		classLoader.addMessagePrinter(printer);
        classLoader.addMessagePrinter(new DefaultMessagePrinter());
		
        senderLayer.getMarshaler().setMigratingCodeFactory(javaByteCodeMigratingCodeFactory);
        receiverLayer.getUnmarshaler().setMigratingCodeFactory(javaByteCodeMigratingCodeFactory);
        
        send_and_receive_code(senderProtocol, receiverProtocol, code);
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
    
    /**
     * Constructor for ObjectTest.
     * @param arg0
     */
    public ObjectTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
    }
}

/**
 * @author bettini
 *
 */
class ComplexObject implements Serializable {
    private static final long serialVersionUID = 3258408430619145009L;
	String s;
    Date date = new Date();
    
    public ComplexObject(String s) {
        this.s = s;
    }
    
    public String toString() {
        return s + "\n" + date;
    }
}

