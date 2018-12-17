/*
 * Created on Feb 23, 2006
 */
package klava.events;

import klava.Environment;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Environment.EnvironmentEntry;

import org.mikado.imc.events.AddRemoveEvent;


/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EnvironmentEvent extends AddRemoveEvent<EnvironmentEntry> {
    public final static String EnvironmentEventId = "EnvironmentEvent";
    
    /**
     * @param source
     */
    public EnvironmentEvent(Object source, EventType eventType, LogicalLocality logicalLocality, PhysicalLocality physicalLocality) {
        super(source, eventType, new Environment.EnvironmentEntry(logicalLocality, physicalLocality));
    }

}
