/*
 * Created on Jan 12, 2006
 */
package klava.tests.junit;

import junit.framework.TestCase;
import klava.KBoolean;
import klava.KInteger;
import klava.KString;
import klava.KVector;
import klava.KlavaMalformedPhyLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;
import klava.TupleSpaceVector;
import klava.topology.KlavaProcessVar;

/**
 * Tests for tuples
 * 
 * @author Lorenzo Bettini
 */
public class TupleTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testProcessMatch() {
        SimpleProcess simpleProcess = new SimpleProcess();

        /* formal process variable */
        KlavaProcessVar klavaProcessVar = new KlavaProcessVar();

        Tuple tuple = new Tuple(simpleProcess);
        Tuple template = new Tuple(klavaProcessVar);

        boolean matched = tuple.match(template);
        assertTrue(matched);
    }

    public void testResetOriginalTemplate() {
        KString s = new KString(); // formal declaration
        KInteger i = new KInteger(); // formal declaration
        Tuple t1 = new Tuple(s, i);
        Tuple t2 = new Tuple(new KString("Hello"), new KInteger(10));
        Tuple t3 = new Tuple(new KString("World"), new KInteger(20));
        System.out.println("tuples, " + t1 + " " + t2 + " " + t3);
        assertTrue(t2.match(t1)); // true
        t1.resetOriginalTemplate();
        assertTrue(((TupleItem) t1.getItem(0)).isFormal());
        assertTrue(((TupleItem) t1.getItem(1)).isFormal());
        assertFalse(t2.match(t1)); // false: already matched
        assertTrue(t3.match(t1)); // true
    }

    /**
     * a KString must not match a PhysicalLocality
     * 
     * @throws KlavaMalformedPhyLocalityException
     */
    public void testPhysicalLocality()
            throws KlavaMalformedPhyLocalityException {
        Tuple actual = new Tuple(new PhysicalLocality("pipe-foo"));
        KString str = new KString();
        Tuple template = new Tuple(str);

        assertFalse(actual.match(template));
    }

    /**
     * Check that setValue with a formal actually makes the value
     * a formal tuple field.
     * 
     * @param <T>
     * @param formal
     * @param value
     */
    <T extends TupleItem> void checkSetFormalValue(T formal, T value) {
        System.out.println("VALUE: " + value + ", FORMAL: " + formal);
        assertTrue(formal.isFormal());
        assertFalse(value.isFormal());

        // now set the formal value
        value.setValue(formal);
        assertTrue(value.isFormal());
    }

    /**
     * @throws KlavaMalformedPhyLocalityException
     */
    public void testSetFormalValue() throws KlavaMalformedPhyLocalityException {
        checkSetFormalValue(new KInteger(), new KInteger(10));
        checkSetFormalValue(new KString(), new KString("foo"));
        checkSetFormalValue(new LogicalLocality(), new LogicalLocality("foo"));
        checkSetFormalValue(new PhysicalLocality(), new PhysicalLocality("foo"));
        checkSetFormalValue(new KBoolean(), new KBoolean(false));
        checkSetFormalValue(new KVector(), new KVector(10));
        TupleSpaceVector tupleSpaceVector = new TupleSpaceVector();
        tupleSpaceVector.out(new Tuple("foo"));
        checkSetFormalValue(new TupleSpaceVector(), tupleSpaceVector);
    }
    
    /**
     * Tests references to tuple fields when resetOriginalTemplate is
     * employed.
     * 
     * This uses TupleItem.
     */
    public void testReferenceToTupleField() {
        KString string = new KString(); // formal
        Tuple templateTuple = new Tuple(string);
        Tuple actualTuple = new Tuple(new KString("foo"));
        
        TupleSpaceVector tupleSpace = new TupleSpaceVector();
        tupleSpace.out(actualTuple);
        
        assertTrue(string.isFormal());
        assertTrue(tupleSpace.in_nb(templateTuple));
        assertFalse(tupleSpace.in_nb(templateTuple));
        
        assertFalse(string.isFormal());
        
        tupleSpace.out(new Tuple(string));
        
        templateTuple.resetOriginalTemplate();
        
        /* now string should be formal again */
        assertTrue(string.isFormal());
        
        /* but the copy inserted in the tuple space must not have
         * been touched
         */
        assertEquals(new KString("foo"), tupleSpace.getTuple(0).getItem(0));
    }
    
    /**
     * Tests references to tuple fields when resetOriginalTemplate is
     * employed.
     * 
     * This uses non TupleItem.
     */
    public void testReferenceToTupleFieldNoTupleItem() {
        Tuple templateTuple = new Tuple(String.class);
        Tuple actualTuple = new Tuple(new String("foo"));
        
        TupleSpaceVector tupleSpace = new TupleSpaceVector();
        tupleSpace.out(actualTuple);
        
        assertTrue(tupleSpace.in_nb(templateTuple));
        assertFalse(tupleSpace.in_nb(templateTuple));
        
        assertEquals("foo", templateTuple.getItem(0));
        
        tupleSpace.out(new Tuple(templateTuple.getItem(0)));
        
        templateTuple.resetOriginalTemplate();
        
        /* now string should be formal again */
        assertEquals(String.class, templateTuple.getItem(0));
        
        /* but the copy inserted in the tuple space must not have
         * been touched
         */
        assertEquals("foo", tupleSpace.getTuple(0).getItem(0));
    }
}
