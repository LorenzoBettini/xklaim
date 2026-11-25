/**
 * 
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Tuple;
import klava.TupleSpaceVector;

import org.mikado.imc.protocols.ProtocolException;


/**
 * Tests for Node local and remote operations
 * 
 * @author Lorenzo Bettini
 * 
 */
public class NodeOperationTest extends ClientServerBase {

    public void testTupleSpaceTimeoutBoundaries() throws Exception {
        TupleSpaceVector space = new TupleSpaceVector();
        Tuple template = new Tuple(new KString("foo"));
        template.setHandleRetrieved(false);
        space.out(new Tuple(new KString("foo")));

        assertFalse(space.read_t(template, -1));
        assertFalse(space.in_t(template, -1));
        assertFalse(space.read(template, -1));
        assertFalse(space.in(template, -1));
        assertEquals(1, space.length());

        assertTrue(space.read_t(template, 0));
        assertEquals(1, space.length());
        assertTrue(space.in_t(template, 0));
        assertEquals(0, space.length());
        assertFalse(space.read(template, 0));
        assertFalse(space.in(template, 0));

        space.out(new Tuple(new KString("foo")));
        assertTrue(space.read(template, 0));
        assertTrue(space.in(template, 0));
    }

    public void testNodeTimeoutBoundaries() throws Exception {
        Tuple template = new Tuple(new KString("foo"));
        template.setHandleRetrieved(false);
        clientNode.out(new Tuple(new KString("foo")));

        assertFalse(clientNode.read_t(template, -1));
        assertFalse(clientNode.in_t(template, self, -1));
        assertFalse(clientNode.read_t(template, serverLoc, -1));
        assertFalse(clientNode.in_t(template, serverLoc, -1));
        assertEquals(1, clientNode.getTupleSpace().length());

        assertTrue(clientNode.read_t(template, 0));
        assertTrue(clientNode.in_t(template, self, 0));
        assertFalse(clientNode.read_t(template, 0));
        assertFalse(clientNode.in_t(template, self, 0));

        clientLoginsToServer();
        serverNode.out(new Tuple(new KString("foo")));
        assertTrue(clientNode.read_t(template, serverLoc, 0));
        assertEquals(1, serverNode.getTupleSpace().length());
        assertTrue(clientNode.in_t(template, serverLoc, 0));
        assertEquals(0, serverNode.getTupleSpace().length());
        assertFalse(clientNode.read_t(template, serverLoc, 0));
        assertFalse(clientNode.in_t(template, serverLoc, 0));
    }

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
