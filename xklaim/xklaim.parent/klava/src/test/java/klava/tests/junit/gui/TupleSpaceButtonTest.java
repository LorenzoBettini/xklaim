/*
 * Created on Mar 15, 2006
 */
package klava.tests.junit.gui;

import junit.framework.TestCase;
import klava.KString;
import klava.Tuple;
import klava.gui.TupleSpaceButton;

/**
 * Tests for TupleSpaceButton class
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class TupleSpaceButtonTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClicked() {
        TupleSpaceButton tupleSpaceButton = new TupleSpaceButton();

        /* set the text of the button */
        tupleSpaceButton.out(new Tuple(new KString("button")));

        /*
         * the previous tuple is not inserted in the tuple space but simply used
         * to set the text of the button
         */
        assertFalse(tupleSpaceButton.in_nb(new Tuple(new KString())));
        assertEquals("button", tupleSpaceButton.getJButton().getText());

        Tuple complexTuple = new Tuple("this is a button", 10);

        /* set the text of the button */
        tupleSpaceButton.out(complexTuple);

        /*
         * the previous tuple is not inserted in the tuple space but simply used
         * to set the text of the button
         */
        assertFalse(tupleSpaceButton.in_nb(complexTuple));
        assertEquals(Tuple.cleanString(complexTuple.toString()),
                tupleSpaceButton.getJButton().getText());
        
        Tuple clicked = new Tuple(TupleSpaceButton.clickedString);
        
        assertFalse(tupleSpaceButton.in_nb(clicked));
        
        /* generate a click event */
        tupleSpaceButton.getJButton().doClick();
        
        /* we should be able to intercept the event now */
        assertTrue(tupleSpaceButton.in_nb(clicked));
        
        clicked = new Tuple(TupleSpaceButton.clickedString);
        
        assertFalse(tupleSpaceButton.in_nb(clicked));
        
        /* generate a click event */
        tupleSpaceButton.out(new Tuple(TupleSpaceButton.clickedString));
        
        /* we should be able to intercept the event now */
        assertTrue(tupleSpaceButton.in_nb(clicked));
    }
}
