/**
 * 
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Tuple;

import org.mikado.imc.protocols.ProtocolException;


/**
 * Tests for Node local and remote operations
 * 
 * @author Lorenzo Bettini
 * 
 */
public class NodeOperationTest extends ClientServerBase {

    public void testLocalOperations() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();

        Tuple tuple = new Tuple(new KString("foo"));
        Tuple template = new Tuple(new KString());
        
        /* we reuse the same template to match the same tuple */
        template.setHandleRetrieved(false);
        
        /*
         * this must be a local operation and the the tuple must be in the
         * client tuple space.
         */
        clientNode.out(tuple, clientLoc);
        assertTrue(clientNode.getTupleSpace().read_nb(template));
        assertTrue(clientNode.read_nb(template));
        
        /*
         * this must be a local operation and the the tuple must be in the
         * client tuple space.
         */
        assertTrue(clientNode.read_nb(template, clientLoc));
        assertTrue(clientNode.read_t(template, clientLoc, 1000));
        clientNode.read(template, clientLoc);
        clientNode.in(template, clientLoc);
        assertFalse(clientNode.in_nb(template, clientLoc));
        assertFalse(clientNode.in_t(template, clientLoc, 1000));
        
        /* now use self */
        
        /*
         * this must be a local operation and the the tuple must be in the
         * client tuple space.
         */
        clientNode.out(tuple, self);
        assertTrue(clientNode.getTupleSpace().read_nb(template));
        assertTrue(clientNode.read_nb(template));
        
        /*
         * this must be a local operation and the the tuple must be in the
         * client tuple space.
         */
        assertTrue(clientNode.read_nb(template, self));
        assertTrue(clientNode.read_t(template, self, 1000));
        clientNode.read(template, self);
        clientNode.in(template, self);
        assertFalse(clientNode.in_nb(template, self));
        assertFalse(clientNode.in_t(template, self, 1000));

    }
}
