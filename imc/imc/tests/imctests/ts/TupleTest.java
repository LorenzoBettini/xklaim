/*
 * Created on Jan 12, 2006
 */
package imctests.ts;

import org.mikado.imc.ts.Tuple;
import org.mikado.imc.ts.TupleBoolean;
import org.mikado.imc.ts.TupleException;
import org.mikado.imc.ts.TupleInteger;
import org.mikado.imc.ts.TupleItem;
import org.mikado.imc.ts.TupleSpace;
import org.mikado.imc.ts.TupleSpaceVector;
import org.mikado.imc.ts.TupleString;
import org.mikado.imc.ts.TupleVector;

import junit.framework.TestCase;

/**
 * Tests for tuples
 * 
 * @author Lorenzo Bettini
 */
public class TupleTest extends TestCase {

    /**
     * Performs a blocking out.
     * 
     * @author Lorenzo Bettini
     */
    static public class BlockingOutThread extends Thread {
        TupleSpace tupleSpace;

        Tuple tuple;

        /**
         * @param tupleSpace
         * @param tuple
         */
        public BlockingOutThread(TupleSpace tupleSpace, Tuple template) {
            this.tupleSpace = tupleSpace;
            this.tuple = template;
        }

        public void run() {
            try {
                tupleSpace.out_b(tuple);
                System.out.println("performed out of " + tuple);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (TupleException e) {
                e.printStackTrace();
            }

        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testResetOriginalTemplate() throws TupleException {
        TupleString s = new TupleString(); // formal declaration
        TupleInteger i = new TupleInteger(); // formal declaration
        Tuple t1 = new Tuple(s, i);
        Tuple t2 = new Tuple(new TupleString("Hello"), new TupleInteger(10));
        Tuple t3 = new Tuple(new TupleString("World"), new TupleInteger(20));
        System.out.println("tuples, " + t1 + " " + t2 + " " + t3);
        assertTrue(t2.match(t1)); // true
        t1.resetOriginalTemplate();
        assertTrue(((TupleItem) t1.getItem(0)).isFormal());
        assertTrue(((TupleItem) t1.getItem(1)).isFormal());
        assertFalse(t2.match(t1)); // false: already matched
        assertTrue(t3.match(t1)); // true
    }

    /**
     * Check that setValue with a formal actually makes the value a formal tuple
     * field.
     * 
     * @param <T>
     * @param formal
     * @param value
     * @throws TupleException 
     */
    <T extends TupleItem> void checkSetFormalValue(T formal, T value) throws TupleException {
        System.out.println("VALUE: " + value + ", FORMAL: " + formal);
        assertTrue(formal.isFormal());
        assertFalse(value.isFormal());

        // now set the formal value
        value.setValue(formal);
        assertTrue(value.isFormal());
    }

    /**
     * @throws TupleException 
     * @throws KlavaMalformedPhyLocalityException
     */
    public void testSetFormalValue() throws TupleException {
        checkSetFormalValue(new TupleInteger(), new TupleInteger(10));
        checkSetFormalValue(new TupleString(), new TupleString("foo"));
        checkSetFormalValue(new TupleBoolean(), new TupleBoolean(false));
        checkSetFormalValue(new TupleVector(), new TupleVector(10));
        TupleSpaceVector tupleSpaceVector = new TupleSpaceVector();
        tupleSpaceVector.out(new Tuple("foo"));
        checkSetFormalValue(new TupleSpaceVector(), tupleSpaceVector);
    }

    /**
     * Tests references to tuple fields when resetOriginalTemplate is employed.
     * 
     * This uses TupleItem.
     * @throws TupleException 
     */
    public void testReferenceToTupleField() throws TupleException {
        TupleString string = new TupleString(); // formal
        Tuple templateTuple = new Tuple(string);
        Tuple actualTuple = new Tuple(new TupleString("foo"));

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

        /*
         * but the copy inserted in the tuple space must not have been touched
         */
        assertEquals(new TupleString("foo"), tupleSpace.getTuple(0).getItem(0));
    }

    /**
     * Tests references to tuple fields when resetOriginalTemplate is employed.
     * 
     * This uses non TupleItem.
     * @throws TupleException 
     */
    public void testReferenceToTupleFieldNoTupleItem() throws TupleException {
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

        /*
         * but the copy inserted in the tuple space must not have been touched
         */
        assertEquals("foo", tupleSpace.getTuple(0).getItem(0));
    }
    
    /**
     * Spawns a thread that performs a blocking out
     * @throws InterruptedException 
     * @throws TupleException 
     */
    public void testBlockingOut() throws InterruptedException, TupleException {
        Tuple outTuple = new Tuple(new TupleString("foo"));
        TupleSpace tupleSpace = new TupleSpaceVector();
        
        BlockingOutThread blockingOutThread = new BlockingOutThread(tupleSpace, outTuple);
        blockingOutThread.start();
        
        Tuple template = new Tuple(new TupleString());
        tupleSpace.in(template);
        
        blockingOutThread.join();
        
        assertEquals(outTuple.getItem(0), template.getItem(0));
    }
    
    /**
     * Spawns a thread that performs a blocking out;
     * the thread is interrupted during the waiting and we
     * check that the tuple is removed from the tuple space.
     * @throws InterruptedException 
     * @throws TupleException 
     */
    public void testInterruptedBlockingOut() throws InterruptedException, TupleException {
        Tuple outTuple = new Tuple(new TupleString("foo"));
        TupleSpace tupleSpace = new TupleSpaceVector();
        
        BlockingOutThread blockingOutThread = new BlockingOutThread(tupleSpace, outTuple);
        blockingOutThread.start();
        
        Tuple template = new Tuple(new TupleString());
        
        // we read the tuple to make sure that the tuple is actually in the ts
        // notice that read won't notify the thread that's performing an out.
        tupleSpace.read(template);
        
        System.out.println("tuple space before interruption: " + tupleSpace);
        
        blockingOutThread.interrupt();
        blockingOutThread.join();
        
        System.out.println("tuple space after interruption: " + tupleSpace);
        assertTrue(tupleSpace.length() == 0);
    }

    /**
     * Tests the version of in without parameters
     * @throws TupleException 
     * @throws InterruptedException 
     */
    public void testIn() throws TupleException, InterruptedException {
        TupleSpace tupleSpace = new TupleSpaceVector();
        Tuple t2 = new Tuple(new TupleString("Hello"), new TupleInteger(10));
        Tuple t3 = new Tuple(new TupleString("World"), new TupleInteger(20));
        
        tupleSpace.out(t2);
        tupleSpace.out(t3);
        
        Tuple t;
        
        // although it is not specified, the tuples are returned in FIFO
        // so we use this information to perform the check
        t = tupleSpace.in();
        assertEquals(t, t2);
        t = tupleSpace.in();
        assertEquals(t, t3);
    }
    
    /**
     * Spawns a thread that performs a blocking out and the in with
     * no parameter
     * 
     * @throws InterruptedException 
     * @throws TupleException 
     */
    public void testBlockingOutAndIn() throws InterruptedException, TupleException {
        Tuple outTuple = new Tuple(new TupleString("foo"));
        TupleSpace tupleSpace = new TupleSpaceVector();
        
        BlockingOutThread blockingOutThread = new BlockingOutThread(tupleSpace, outTuple);
        blockingOutThread.start();
        
        Tuple t = tupleSpace.in();
        
        blockingOutThread.join();
        
        assertEquals(outTuple, t);
    }

}
