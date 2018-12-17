/*
 * Created on Feb 23, 2006
 */
package org.mikado.imc.events;

/**
 * An event representing the addition/removal of a process.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ProcessEvent extends AddRemoveEvent<Thread> {
    public static final String ProcessEventId = "ProcessEvent";

    /**
     * @param source
     * @param processEventType
     * @param thread
     */
    public ProcessEvent(Object source, EventType processEventType, Thread thread) {
        super(source, processEventType, thread);
    }
}
