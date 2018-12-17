/*
 * Created on May 27, 2005
 */
package examples.event;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;

/**
 * An EventListener that stores the string representation of
 * an event into a StringBuffer.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class BufferedListener implements EventListener {
	StringBuffer buffer = new StringBuffer();
	
	/**
	 * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
	 */
	public void notify(Event event) {
		buffer.append("received event: " + event.toString() + "\n");        
	}

	/**
	 * @return Returns the buffer.
	 */
	public final StringBuffer getBuffer() {
		return buffer;
	}
}
