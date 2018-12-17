/*
 * Created on Mar 13, 2006
 */
package klava.tests.junit.gui;

import org.mikado.imc.events.EventManager;

import junit.framework.TestCase;
import klava.KString;
import klava.Tuple;
import klava.TupleSpaceVector;
import klava.gui.TupleSpaceList;

/**
 * Tests for the class TupleSpaceList
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleSpaceListTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSelected() {
        TupleSpaceList tupleSpaceList = new TupleSpaceList(
                new TupleSpaceVector());
        tupleSpaceList.setEventManager(new EventManager());

        tupleSpaceList.out(new Tuple("first"));
        tupleSpaceList.out(new Tuple("second"));
        tupleSpaceList.out(new Tuple("third"));
        tupleSpaceList.out(new Tuple("fourth"));

        /* make request for the selected item (which should be none) */
        tupleSpaceList.out(new Tuple(TupleSpaceList.cmdString, new KString(
                "getSelectedItem")));

        KString selected = new KString();

        /*
         * first, make sure that the response is not put into the graphical list
         * (but in a separate TupleSpace)
         */
        assertFalse(tupleSpaceList.getTupleSpace().in_nb(
                new Tuple(TupleSpaceList.cmdString, new KString(
                        "getSelectedItem"), selected)));

        assertTrue(tupleSpaceList.in_nb(new Tuple(TupleSpaceList.cmdString,
                new KString("getSelectedItem"), selected)));
        assertEquals("", selected.toString());

        /* make request for the selected items (which should be none) */
        tupleSpaceList.out(new Tuple(TupleSpaceList.cmdString, new KString(
                "getSelectedItems")));

        TupleSpaceVector selecteds = new TupleSpaceVector();

        /*
         * first, make sure that the response is not put into the graphical list
         * (but in a separate TupleSpace)
         */
        assertFalse(tupleSpaceList.getTupleSpace().in_nb(
                new Tuple(TupleSpaceList.cmdString, new KString(
                        "getSelectedItems"), selecteds)));

        assertTrue(tupleSpaceList.in_nb(new Tuple(TupleSpaceList.cmdString,
                new KString("getSelectedItems"), selecteds)));
        assertTrue(selecteds.length() == 0);

        int indices[] = new int[1];

        indices[0] = 1;

        /* select the second entry */
        tupleSpaceList.setSelectedIndices(indices);

        /* make request for the selected item */
        tupleSpaceList.out(new Tuple(TupleSpaceList.cmdString, new KString(
                "getSelectedItem")));

        selected = new KString();
        assertTrue(tupleSpaceList.in_nb(new Tuple(TupleSpaceList.cmdString,
                new KString("getSelectedItem"), selected)));
        assertEquals("second", selected.toString());

        /* now select more entries */
        indices = new int[2];

        indices[0] = 0;
        indices[1] = 3;

        /* select the first and the fourth entries */
        tupleSpaceList.setSelectedIndices(indices);

        /* make request for the selected items */
        tupleSpaceList.out(new Tuple(TupleSpaceList.cmdString, new KString(
                "getSelectedItems")));

        selecteds = new TupleSpaceVector();
        assertTrue(tupleSpaceList.in_nb(new Tuple(TupleSpaceList.cmdString,
                new KString("getSelectedItems"), selecteds)));
        assertEquals("first", selecteds.getTuple(0).getItem(0).toString());
        assertEquals("fourth", selecteds.getTuple(1).getItem(0).toString());
    }
}
