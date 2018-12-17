/*
 * Created on Apr 13, 2006
 */
package imctests.protocols;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionNumberLayer;
import org.mikado.imc.protocols.UnMarshaler;

import junit.framework.TestCase;

/**
 * Tests for SessionNumberLayer
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class SessionNumberLayerTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Checks the sequence of packets
     * 
     * @throws IOException
     * @throws ProtocolException
     */
    public void testCorrectSequence() throws IOException, ProtocolException {
        SessionNumberLayer sessionNumberLayer = new SessionNumberLayer();
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();

        ProtocolStack protocolStack = new ProtocolStack(sessionNumberLayer);
        protocolStack.insertLayer(protocolLayerSharedBuffer);

        Marshaler marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine("foo");
        marshaler.writeBoolean(true);
        protocolStack.releaseMarshaler(marshaler);

        assertEquals(0, sessionNumberLayer.getRecNumber());
        assertEquals(1, sessionNumberLayer.getSendNumber());

        UnMarshaler unMarshaler = protocolStack.createUnMarshaler();
        String string = unMarshaler.readStringLine();
        boolean b = unMarshaler.readBoolean();
        protocolStack.releaseUnMarshaler(unMarshaler);

        assertEquals(1, sessionNumberLayer.getRecNumber());
        assertEquals(1, sessionNumberLayer.getSendNumber());

        assertEquals("foo", string);
        assertEquals(true, b);

        marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine("bar");
        marshaler.writeBoolean(false);
        protocolStack.releaseMarshaler(marshaler);

        assertEquals(1, sessionNumberLayer.getRecNumber());
        assertEquals(2, sessionNumberLayer.getSendNumber());

        unMarshaler = protocolStack.createUnMarshaler();
        string = unMarshaler.readStringLine();
        b = unMarshaler.readBoolean();
        protocolStack.releaseUnMarshaler(unMarshaler);

        assertEquals(2, sessionNumberLayer.getRecNumber());
        assertEquals(2, sessionNumberLayer.getSendNumber());

        assertEquals("bar", string);
        assertEquals(false, b);

        /* OK now close it */
        protocolStack.close();

        try {
            unMarshaler = protocolStack.createUnMarshaler();
            fail(); // must not get here
        } catch (ProtocolException e) {
            
        }
    }

    /**
     * Checks the wrong sequence of packets
     * 
     * @throws IOException
     * @throws ProtocolException
     */
    public void testInCorrectSequence() throws IOException, ProtocolException {
        SessionNumberLayer sessionNumberLayer = new SessionNumberLayer();
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();

        ProtocolStack protocolStack = new ProtocolStack(sessionNumberLayer);
        protocolStack.insertLayer(protocolLayerSharedBuffer);

        sessionNumberLayer.setSendNumber(1); // should have been 0 instead

        Marshaler marshaler = protocolStack.createMarshaler();
        marshaler.writeStringLine("foo");
        marshaler.writeBoolean(true);
        protocolStack.releaseMarshaler(marshaler);

        assertEquals(0, sessionNumberLayer.getRecNumber());
        assertEquals(2, sessionNumberLayer.getSendNumber());

        try {
            protocolStack.createUnMarshaler();
            fail(); // must not get here
        } catch (ProtocolException e) {
            assertEquals("packet number not in sequence: 1 instead of 0", e
                    .getMessage());
        }
    }
}
