/*
 * Created on Feb 22, 2006
 */
package org.mikado.imc.ts;

import org.mikado.imc.events.Event;


/**
 * An event indicating the addition/removal of a tuple to/from a tuple space.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleEvent extends Event {

    /**
     * The tuple involved in the event. This is null in case the event is a
     * remove all event
     */
    public Tuple tuple = null;

    public TupleEventType eventType;

    public static final String EventId = "TupleEvent";

    /**
     * The enum representing possible tuple event types
     * 
     * @author Lorenzo Bettini
     */
    public enum TupleEventType {
        ADDED, REMOVED, REMOVEDALL
    }

    /**
     * @param source
     * @param tuple
     * @param eventType
     */
    public TupleEvent(Object source, Tuple tuple, TupleEventType eventType) {
        super(source);
        this.tuple = tuple;
        this.eventType = eventType;
    }

    /**
     * @param source
     * @param eventType
     */
    public TupleEvent(Object source, TupleEventType eventType) {
        this(source, null, eventType);
    }

    /**
     * @see org.mikado.imc.events.Event#toString()
     */
    @Override
    public String toString() {
        return "" + eventType + (tuple != null ? ": " + tuple : "");
    }

}
