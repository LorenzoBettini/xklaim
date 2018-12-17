/*
 * Created on Mar 14, 2006
 */
package klava.tests.junit.gui;

import junit.framework.TestCase;
import klava.KInteger;
import klava.KString;
import klava.Tuple;
import klava.gui.TupleSpaceKeyboard;

/**
 * Tests for TupleSpaceKeyboard
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class TupleSpaceKeyboardTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInserted() {
        TupleSpaceKeyboard tupleSpaceKeyboard = new TupleSpaceKeyboard();

        tupleSpaceKeyboard.out(new Tuple("inserted"));
        tupleSpaceKeyboard.out(new Tuple("inserted int", new KInteger(10)));

        /* also manually set the text field */
        tupleSpaceKeyboard.getJTextField().setText(
                new Tuple("inserted int", new KInteger(10)).toString());

        KInteger read = new KInteger();
        assertTrue(tupleSpaceKeyboard.read_nb(new Tuple("inserted int", read)));
        assertEquals(new KInteger(10), read);
        assertEquals(new Tuple("inserted int", read).toString(),
                tupleSpaceKeyboard.getJTextField().getText());

        assertTrue(tupleSpaceKeyboard.in_nb(new Tuple("inserted int", read)));
        assertEquals(new KInteger(10), read);

        /* now we must not find the tuple */
        assertFalse(tupleSpaceKeyboard.in_nb(new Tuple("inserted int", read)));

        /* and the text field must be empty */
        assertEquals("", tupleSpaceKeyboard.getJTextField().getText());

        assertTrue(tupleSpaceKeyboard.read_nb(new Tuple("inserted")));
        assertTrue(tupleSpaceKeyboard.read_nb(new Tuple("inserted")));

        Tuple commandTuple = new Tuple(new KString("getText"));
        assertFalse(tupleSpaceKeyboard.isCommand(commandTuple));
        KString text = new KString();
        commandTuple = new Tuple(new KString("getText"), text);
        assertTrue(tupleSpaceKeyboard.isCommand(commandTuple));

        /* the text field should be empty */
        commandTuple = new Tuple(new KString("getText"), text);
        assertTrue(tupleSpaceKeyboard.in_nb(commandTuple));
        assertEquals("", text.toString());

        /* now manually set the text field and retrieve the text */
        tupleSpaceKeyboard.getJTextField().setText("foo");
        text = new KString();
        commandTuple = new Tuple(new KString("getText"), text);
        assertTrue(tupleSpaceKeyboard.read_nb(commandTuple));
        assertEquals("foo", text.toString());
        /* the text field should still contain text */
        assertEquals("foo", tupleSpaceKeyboard.getJTextField().getText());

        /* now also remove the text */
        text = new KString();
        commandTuple = new Tuple(new KString("getText"), text);
        assertTrue(tupleSpaceKeyboard.in_nb(commandTuple));
        assertEquals("foo", text.toString());
        /* the text field should still contain text */
        assertEquals("", tupleSpaceKeyboard.getJTextField().getText());

        /* now set the text */
        commandTuple = new Tuple(new KString("setText"), "foo text " + 1);
        tupleSpaceKeyboard.out(commandTuple);
        assertEquals("foo text " + 1, tupleSpaceKeyboard.getJTextField()
                .getText());
    }
}
