/**
 * 
 */
package klava.events;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.log.MessagePrinter;

/**
 * A specialized EventListener that simply prints the
 * string representation of an Event into a MessagePrinter.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 *
 */
public class EventToStringListener implements EventListener {
    protected MessagePrinter message_printer = null;
    
    /**
     * @param message_printer
     */
    public EventToStringListener(MessagePrinter message_printer) {
        this.message_printer = message_printer;
    }

    /**
     * @param msg
     */
    void setMessagePrinter(MessagePrinter msg) {
        message_printer = msg;
    }
    
    /**
     * Simply prints the string representation of the passed
     * Event by using a MessagePrinter.
     * 
     * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        if (message_printer != null)
            message_printer.Print(event.toString());
    }

}
