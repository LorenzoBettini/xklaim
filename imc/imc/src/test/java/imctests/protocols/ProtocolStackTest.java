/*
 * Created on Apr 13, 2006
 */
package imctests.protocols;

import org.mikado.imc.protocols.IncrementProtocolLayer;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolLayerSharedBuffer;
import org.mikado.imc.protocols.ProtocolStack;

import junit.framework.TestCase;

/**
 * Tests for ProtocolStack
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ProtocolStackTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests insertAfter and replace
     * @throws ProtocolException 
     */
    public void testCorrectManipulation() throws ProtocolException {
        ProtocolStack protocolStack = new ProtocolStack();
        
        ProtocolLayerEndPoint protocolLayerEndPoint = new ProtocolLayerEndPoint();
        ProtocolLayerSharedBuffer protocolLayerSharedBuffer = new ProtocolLayerSharedBuffer();
        IncrementProtocolLayer incrementProtocolLayer = new IncrementProtocolLayer();
        
        protocolStack.insertFirstLayer(protocolLayerEndPoint);
        protocolStack.insertFirstLayer(protocolLayerSharedBuffer);
        protocolStack.insertFirstLayer(incrementProtocolLayer);
        
        /* check layers' positions, which must be reversed */
        assertEquals(0, protocolStack.indexOf(incrementProtocolLayer));
        assertEquals(1, protocolStack.indexOf(protocolLayerSharedBuffer));
        assertEquals(2, protocolStack.indexOf(protocolLayerEndPoint));
        
        IncrementProtocolLayer incrementProtocolLayer2 = new IncrementProtocolLayer();
        
        protocolStack.insertAfter(protocolLayerEndPoint, incrementProtocolLayer2);
        
        /* check that insertion after succedded */
        assertEquals(0, protocolStack.indexOf(incrementProtocolLayer));
        assertEquals(1, protocolStack.indexOf(protocolLayerSharedBuffer));
        assertEquals(2, protocolStack.indexOf(protocolLayerEndPoint));
        assertEquals(3, protocolStack.indexOf(incrementProtocolLayer2));
        
        IncrementProtocolLayer incrementProtocolLayer3 = new IncrementProtocolLayer();
        
        protocolStack.insertAfter(protocolLayerSharedBuffer, incrementProtocolLayer3);
        
        /* its stack must not be null */
        assertTrue(protocolLayerSharedBuffer.getProtocolStack() != null);
        
        /* check that insertion after succedded */
        assertEquals(0, protocolStack.indexOf(incrementProtocolLayer));
        assertEquals(1, protocolStack.indexOf(protocolLayerSharedBuffer));
        assertEquals(2, protocolStack.indexOf(incrementProtocolLayer3));
        assertEquals(3, protocolStack.indexOf(protocolLayerEndPoint));
        assertEquals(4, protocolStack.indexOf(incrementProtocolLayer2));
        
        /* check replace */
        IncrementProtocolLayer incrementProtocolLayer4 = new IncrementProtocolLayer();
        
        assertTrue(protocolLayerSharedBuffer.getProtocolStack() != null);
        
        protocolStack.replace(protocolLayerSharedBuffer, incrementProtocolLayer4);
        
        assertTrue(protocolLayerSharedBuffer.getProtocolStack() != null);
        
        assertEquals(0, protocolStack.indexOf(incrementProtocolLayer));
        assertEquals(1, protocolStack.indexOf(incrementProtocolLayer4));
        assertEquals(2, protocolStack.indexOf(incrementProtocolLayer3));
        assertEquals(3, protocolStack.indexOf(protocolLayerEndPoint));
        assertEquals(4, protocolStack.indexOf(incrementProtocolLayer2));
        
        IncrementProtocolLayer absent = new IncrementProtocolLayer();
        
        /* check errors */
        try {
            protocolStack.insertAfter(absent, incrementProtocolLayer3);
            fail(); // must not get here
        } catch (ProtocolException e) {
            
        }
        
        try {
            protocolStack.replace(absent, incrementProtocolLayer3);
            fail(); // must not get here
        } catch (ProtocolException e) {
            
        }
    }
}
