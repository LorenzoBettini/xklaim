/**
 * created: Jan 10, 2006
 */
package klava.tests.junit;

import klava.KInteger;
import klava.KString;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;

import org.mikado.imc.protocols.ProtocolException;

/**
 * Tests for nodes.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class NodeTest extends ClientServerBase {
    /**
     * Test resetOriginalTemplate in the distributed setting
     * 
     * @throws KlavaException
     * @throws InterruptedException
     * @throws ProtocolException
     */
    public void testResetOriginalTemplate() throws KlavaException,
            ProtocolException, InterruptedException {
        clientLoginsToServer();

        KString s = new KString(); // formal declaration
        KInteger i = new KInteger(); // formal declaration
        Tuple t1 = new Tuple(s, i);
        Tuple t2 = new Tuple(new KString("Hello"), new KInteger(10));
        Tuple t3 = new Tuple(new KString("World"), new KInteger(20));
        System.out.println("tuples, " + t1 + " " + t2 + " " + t3);
        serverNode.out(t2);
        serverNode.out(t3);

        assertTrue(clientNode.read_nb(t1, serverLoc)); // true
        assertEquals(new KString("Hello"), t1.getItem(0));
        assertEquals(new KInteger(10), t1.getItem(1));
        t1.resetOriginalTemplate();
        assertTrue(((TupleItem) t1.getItem(0)).isFormal());
        assertTrue(((TupleItem) t1.getItem(1)).isFormal());
        
        /* now read the other one */
        assertTrue(clientNode.read_nb(t1, serverLoc)); // true
        assertEquals(new KString("World"), t1.getItem(0));
        assertEquals(new KInteger(20), t1.getItem(1));
    }

    public void testFailSelf() {
        /*
         * the node tries to translate self, but no physical locality is defined
         */
        try {
            clientNode.getPhysical(new LogicalLocality("self"));

            /* must not get here */
            fail();
        } catch (KlavaException e) {
            assertTrue(e instanceof KlavaLogicalLocalityException);
            assertEquals("no physical locality for self", e.getMessage());
        }
    }

    public void testMainPhysicalLocality() throws KlavaException {
        /*
         * the server has no connections but we use the main physical locality
         */
        serverNode.setMainPhysicalLocality(serverLoc);

        PhysicalLocality physicalLocality = serverNode
                .getPhysical(new LogicalLocality("self"));

        assertEquals(serverLoc, physicalLocality);
    }

    public void testMainPhysicalLocalityWithConnections()
            throws KlavaException, ProtocolException, InterruptedException {
        /*
         * the client has a connection but uses the main physical locality thus
         * self must be translated to it.
         */
        PhysicalLocality mainLoc = new PhysicalLocality("localhost", 50000);
        clientNode.setMainPhysicalLocality(mainLoc);

        clientLoginsToServer();

        PhysicalLocality physicalLocality = clientNode
                .getPhysical(new LogicalLocality("self"));

        assertEquals(mainLoc, physicalLocality);
    }

    public void testSelfWithConnections() throws KlavaException,
            ProtocolException, InterruptedException {
        /*
         * no main locality is used but there's a connection
         */
        clientLoginsToServer();

        PhysicalLocality physicalLocality = clientNode
                .getPhysical(new LogicalLocality("self"));

        assertEquals(clientLoc, physicalLocality);
    }

}
