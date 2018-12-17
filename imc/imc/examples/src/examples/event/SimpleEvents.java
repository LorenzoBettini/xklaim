/*
 * Created on May 27, 2005
 */
package examples.event;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventManager;

/**
 * An example showing simple events.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class SimpleEvents {
    public static void main(String[] args) {
		EventManager eventManager = new EventManager();
		BufferedListener listener = new BufferedListener();
        eventManager.addListener("TEST1", listener);
        eventManager.addListener("TEST2", listener);
        BufferedListener listener2 = new BufferedListener();
        eventManager.addListener("TEST1", listener2);        
        
        eventManager.generate("TEST1", new Event("Source1"));
        eventManager.generate("TEST2", new Event("Source2"));
        
        System.out.println("Buffer1:\n" + listener.buffer.toString());
        System.out.println("Buffer2:\n" + listener2.buffer.toString());
        
        eventManager.removeListener("TEST2", listener);
        eventManager.generate("TEST2", new Event("Source1"));        
        System.out.println("Buffer1:\n" + listener.buffer.toString());
        
        eventManager.addListener("TEST2", listener);        
        eventManager.generate("TEST2", new Event("Source2"));        
        System.out.println("Buffer1:\n" + listener.buffer.toString());
	}

}
