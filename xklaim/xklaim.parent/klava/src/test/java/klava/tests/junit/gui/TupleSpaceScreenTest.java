/*
 * Created on Mar 14, 2006
 */
package klava.tests.junit.gui;

import junit.framework.TestCase;
import klava.Tuple;
import klava.gui.TupleSpaceScreen;

/**
 * Tests for TupleSpaceScreen
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleSpaceScreenTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInsertedStrings() throws InterruptedException {
        TupleSpaceScreen tupleSpaceScreen = new TupleSpaceScreen();

        Tuple tuple = new Tuple("only string");

        tupleSpaceScreen.out(tuple);

        /*
         * since the tuple contains only one string, we should read back only
         * the string (not in a tuple representation without the \n).
         */
        assertEquals(tuple.getItem(0), tupleSpaceScreen.getJTextArea()
                .getText());

        tupleSpaceScreen.removeAllTuples();

        /* the screen should be empty now */
        assertTrue(tupleSpaceScreen.getJTextArea().getText().length() == 0);

        tuple = new Tuple("a", 10);

        tupleSpaceScreen.out(tuple);

        /*
         * since the tuple contains is complex we should read it back in its
         * string representation (with the \n).
         */
        assertEquals(tuple.toString() + "\n", tupleSpaceScreen.getJTextArea()
                .getText());

        tupleSpaceScreen.out(tuple);

        /* it should contain both tuples */
        assertEquals(tuple.toString() + "\n" + tuple.toString() + "\n",
                tupleSpaceScreen.getJTextArea().getText());
        
        /* read and in should always return false */
        assertFalse(tupleSpaceScreen.in(tuple));
        assertFalse(tupleSpaceScreen.in_nb(tuple));
        assertFalse(tupleSpaceScreen.in_t(tuple, 5000));
        assertFalse(tupleSpaceScreen.read(tuple));
        assertFalse(tupleSpaceScreen.read_nb(tuple));
        assertFalse(tupleSpaceScreen.read_t(tuple, 5000));
        
        /* lenght should always return 0 */
        assertTrue(tupleSpaceScreen.length() == 0);
    }
}
