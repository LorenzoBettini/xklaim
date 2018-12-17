/*
 * Created on Mar 13, 2006
 */
package klava;

import java.util.Enumeration;

import org.mikado.imc.events.EventGenerator;

/**
 * The interface for tuple spaces
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface TupleSpace extends EventGenerator {

    public abstract void out(Tuple t);

    public abstract boolean read(Tuple t) throws InterruptedException;

    public abstract boolean in(Tuple t) throws InterruptedException;

    // Timeout boolean versions
    public abstract boolean read_t(Tuple t, long TimeOut)
            throws InterruptedException;

    public abstract boolean in_t(Tuple t, long TimeOut)
            throws InterruptedException;

    public abstract boolean read_nb(Tuple t);

    public abstract boolean in_nb(Tuple t);

    public abstract int length();

    /**
     * Removes the i-th tuple from the tuple space (and generates a removed
     * tuple event).
     * 
     * @param i
     */
    public abstract void removeTuple(int i);

    /**
     * Removes all tuples from the tuple space (and generates a removed all
     * tuple event).
     */
    public abstract void removeAllTuples();

    public abstract Enumeration<Tuple> getTupleEnumeration();

}