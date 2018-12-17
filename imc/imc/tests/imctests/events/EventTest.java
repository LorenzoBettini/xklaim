/*
 * Created on Jan 14, 2005
 *
 */
package imctests.events;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventCounter;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;

import junit.framework.TestCase;


/**
 * Test events.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EventTest extends TestCase {
    /**
     * @author bettini
     *
     */
    public class SimpleListener implements EventListener {
        StringBuffer buffer = new StringBuffer();
        
        /* (non-Javadoc)
         * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
         */
        public void notify(Event event) {            
            buffer.append("received event: " + event.toString() + "\n");
        }

    }
    EventManager eventManager;
 
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        eventManager = new EventManager();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEvent() {
        SimpleListener listener = new SimpleListener();
        eventManager.addListener("TEST1", listener);
        eventManager.addListener("TEST2", listener);
        SimpleListener listener2 = new SimpleListener();
        eventManager.addListener("TEST1", listener2);        
        
        eventManager.generate("TEST1", new Event("Source1"));
        eventManager.generate("TEST2", new Event("Source2"));
        
        System.out.println("Buffer1: " + listener.buffer.toString());
        
        assertEquals(listener.buffer.toString(),
                "received event: Event from Source1\nreceived event: Event from Source2\n");
        
        System.out.println("Buffer2: " + listener2.buffer.toString());
        
        assertEquals(listener2.buffer.toString(),
                "received event: Event from Source1\n");
        
        eventManager.generate("TEST1", new Event("Source1"));
        
        System.out.println("Buffer1: " + listener.buffer.toString());
        
        assertEquals(listener.buffer.toString(),
        "received event: Event from Source1\nreceived event: Event from Source2\nreceived event: Event from Source1\n");
        
        eventManager.removeListener("TEST2", listener);
        
        eventManager.generate("TEST2", new Event("Source1"));
        
        System.out.println("Buffer1: " + listener.buffer.toString());
        
        assertEquals(listener.buffer.toString(),
        "received event: Event from Source1\nreceived event: Event from Source2\nreceived event: Event from Source1\n");
        
        eventManager.addListener("TEST2", listener);
        
        eventManager.generate("TEST2", new Event("Source2"));
        
        System.out.println("Buffer1: " + listener.buffer.toString());
        
        assertEquals(listener.buffer.toString(),
        "received event: Event from Source1\nreceived event: Event from Source2\nreceived event: Event from Source1\nreceived event: Event from Source2\n");
    }
    
    public void testEventCounter() throws InterruptedException {
        EventCounter eventCounter = new EventCounter();
        
        eventManager.addListener("MYEVENT", eventCounter);
        
        eventManager.generate("MYEVENT", new Event("1"));
        eventManager.generate("MYEVENT", new Event("1"));
        eventManager.generate("MYEVENT", new Event("1"));
        
        eventCounter.waitForEventNumber(3, 1000);
     
        assertTrue(eventCounter.getEventNumber() == 3);
    }
    
    /**
    *
    *
    * @param args DOCUMENT ME!
    */
   public static void main(String[] args) {
   }

}
