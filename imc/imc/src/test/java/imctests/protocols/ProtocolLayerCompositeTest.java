/*
 * Created on Apr 13, 2006
 */
package imctests.protocols;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IncrementProtocolLayer;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerComposite;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.PutGetProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;

import junit.framework.TestCase;

/**
 * Tests for ProtocolLayerComposite
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ProtocolLayerCompositeTest extends TestCase {

    public static void main(String[] args) {
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that the layers inside ProtocolLayerComposite are used in the
     * correct order
     * 
     * @throws IOException
     * @throws ProtocolException
     */
    public void testCorrectness() throws IOException, ProtocolException {
        PutGetProtocolLayer putGetProtocolLayer = new PutGetProtocolLayer();
        IncrementProtocolLayer incrementProtocolLayer = new IncrementProtocolLayer();
        ProtocolLayerComposite protocolLayerComposite = new ProtocolLayerComposite(
                incrementProtocolLayer, putGetProtocolLayer);
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();

        ProtocolStack protocolStack = new ProtocolStack(
                protocolLayerSharedBuffer);
        protocolStack.insertFirstLayer(protocolLayerComposite);

        Marshaler marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine("foo");
        protocolStack.releaseMarshaler(marshaler);

        UnMarshaler unMarshaler;

        /*
         * since PUT/GET is lower than incremental layer we must first find the
         * GET:foo and then the integer 
         */
        byte[] toByteArray = protocolLayerSharedBuffer.toByteArray();
        unMarshaler = new IMCUnMarshaler(new ByteArrayInputStream(
                toByteArray));

        assertEquals('G', unMarshaler.readByte());
        assertEquals('E', unMarshaler.readByte());
        assertEquals('T', unMarshaler.readByte());
        assertEquals(':', unMarshaler.readByte());
        assertEquals(1, unMarshaler.readInt());

        /*
         * also checks that the written foo string can be read back using the
         * stack.  But first, we have to replace GET with PUT
         */
        toByteArray[0] = 'P';
        toByteArray[1] = 'U';
        toByteArray[2] = 'T';
        protocolLayerSharedBuffer.setBuffer(toByteArray);
        
        unMarshaler = protocolStack.createUnMarshaler();
        assertEquals("foo", unMarshaler.readStringLine());
        protocolStack.releaseUnMarshaler(unMarshaler);
    }
}
