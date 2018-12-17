/*
 * Created on Mar 13, 2006
 */
package org.mikado.imc.ts;

import java.util.Enumeration;

import org.mikado.imc.events.EventGenerator;

/**
 * The interface for tuple spaces
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public interface TupleSpace extends EventGenerator {

    /**
     * Puts the tuple in the tuple space (non blocking operation)
     * 
     * @param t
     * @throws TupleException 
     */
    public abstract void out(Tuple t) throws TupleException;

    /**
     * Puts the tuple in the tuple space and waits that someone removes it
     * (blocking operation).
     * 
     * @param t
     * @throws InterruptedException
     * @throws TupleException 
     */
    public abstract void out_b(Tuple t) throws InterruptedException, TupleException;

    /**
     * Searches for a matching tuple in the tuple space; blocks until it finds
     * one. Does not remove the matching tuple.
     * 
     * The formal fields of the template tuple will be updated with the
     * corresponding matching actual fields.
     * 
     * @param t
     * @return whether the matching tuple is found
     * @throws InterruptedException
     * @throws TupleException 
     */
    public abstract boolean read(Tuple t) throws InterruptedException, TupleException;

    /**
     * Searches for a matching tuple in the tuple space; blocks until it finds
     * one. Removes the matching tuple.
     * 
     * The formal fields of the template tuple will be updated with the
     * corresponding matching actual fields.
     * 
     * @param t
     * @return whether the matching tuple is found
     * @throws InterruptedException
     * @throws TupleException 
     */
    public abstract boolean in(Tuple t) throws InterruptedException, TupleException;

    /**
     * Removes and returns a tuple from the tuple space; blocks until it finds
     * one.
     * 
     * This is not very Linda style, but it's useful if you want to retrieve
     * a tuple without knowing its structure.
     * 
     * @return the retrieved tuple
     * @throws InterruptedException
     */
    public abstract Tuple in() throws InterruptedException;

    /**
     * Timeout version of read.
     * 
     * @param t
     * @param timeOut
     *            the timeout in milliseconds
     * @return whether the matching tuple is found
     * @throws InterruptedException
     * @throws TupleException 
     */
    public abstract boolean read_t(Tuple t, long timeOut)
            throws InterruptedException, TupleException;

    /**
     * Timeout version of in.
     * 
     * @param t
     * @param timeOut
     *            the timeout in milliseconds
     * @return whether the matching tuple is found
     * @throws InterruptedException
     * @throws TupleException 
     */
    public abstract boolean in_t(Tuple t, long timeOut)
            throws InterruptedException, TupleException;

    /**
     * Non blocking version of read: if a matching tuple is not found in the
     * tuple space, then simply returns false
     * 
     * @param t
     * @return whether the matching tuple is found
     * @throws TupleException 
     */
    public abstract boolean read_nb(Tuple t) throws TupleException;

    /**
     * Non blocking version of in: if a matching tuple is not found in the tuple
     * space, then simply returns false
     * 
     * @param t
     * @return whether the matching tuple is found
     * @throws TupleException 
     */
    public abstract boolean in_nb(Tuple t) throws TupleException;

    /**
     * @return the number of tuples in the tuple space.
     */
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

    /**
     * @return the Enumeration of all the tuples in the tuple space
     */
    public abstract Enumeration<Tuple> getTupleEnumeration();

}