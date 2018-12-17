/*
 * Created on Oct 21, 2005
 */
package klava.events;

import java.util.Vector;

import klava.PhysicalLocality;

import org.mikado.imc.events.Event;


/**
 * An event about the addition or removal of a locality.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LocalityEvent extends Event {
    /**
     * If true it is a locality concerning the addition of
     * a locality, otherwise it is about the removal.
     */
    public boolean addition = true;
    
    /**
     * The localities involved in this event.
     */
    public Vector<PhysicalLocality> physicalLocalities;
    
    public static final String ADDLOCALITY_EVENT = "ADD LOCALITY";
    
    public static final String REMOVELOCALITY_EVENT = "REMOVE LOCALITY";
    
    /**
     * @param source
     */
    public LocalityEvent(Object source, Vector<PhysicalLocality> physicalLocalities, boolean addition) {
        super(source);
        this.physicalLocalities = physicalLocalities;
        this.addition = addition;
    }

    /**
     * @see org.mikado.imc.events.Event#toString()
     */
    @Override
    public String toString() {
        return (addition ? ADDLOCALITY_EVENT : REMOVELOCALITY_EVENT) +
            ": " + physicalLocalities;
    }    
}
