package org.mikado.imc.ts;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import org.mikado.imc.common.TimeoutException;
import org.mikado.imc.events.EventGeneratorAdapter;
import org.mikado.imc.ts.TupleEvent.TupleEventType;

/**
 * Implements a TupleSpace through a Vector.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class TupleSpaceVector extends EventGeneratorAdapter implements
        TupleItem, Serializable, TupleSpace {

    /**
     * 
     */
    private static final long serialVersionUID = 835829051669003105L;

    protected Vector<Tuple> tuples;

    public TupleSpaceVector() {
        tuples = new Vector<Tuple>();
    }

    public TupleSpaceVector(Vector<Tuple> v) {
        tuples = v;
    }

    @SuppressWarnings("unchecked")
    public TupleSpaceVector(TupleSpaceVector ts) {
        tuples = (Vector<Tuple>) ts.tuples.clone();
    }

    public void out(Tuple t) throws TupleException {
        _out(t);
    }

    protected void _out(Tuple t) {
        synchronized (tuples) {
            add(t);
            tuples.notifyAll(); // wakes up all waiting processes
        }
    }

    /**
     * Adds a tuple into the tuple space (and also generates an addition
     * TupleEvent).
     * 
     * @param t
     */
    public void add(Tuple t) {
        tuples.addElement(t);
        generate(TupleEvent.EventId, new TupleEvent(this, t,
                TupleEventType.ADDED));
    }

    public boolean read(Tuple t) throws InterruptedException, TupleException {
        return ReadIn(t, false, true);
    }

    public boolean in(Tuple t) throws InterruptedException, TupleException {
        return ReadIn(t, true, true);
    }

    public boolean read(Tuple t, long TimeOut) throws TimeoutException,
            InterruptedException, TupleException {
        if (TimeOut < 0)
            return read(t);
        else
            return ReadIn(t, false, TimeOut);
    }

    public boolean in(Tuple t, long TimeOut) throws TimeoutException,
            InterruptedException, TupleException {
        if (TimeOut < 0)
            return in(t);
        else
            return ReadIn(t, true, TimeOut);
    }

    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException,
            TupleException {
        try {
            return read(t, TimeOut);
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException,
            TupleException {
        try {
            return in(t, TimeOut);
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean read_nb(Tuple t) throws TupleException {
        return findTuple(t, false);
    }

    public boolean in_nb(Tuple t) throws TupleException {
        return findTuple(t, true);
    }

    protected boolean ReadIn(Tuple t, boolean removeTuple, boolean blocking)
            throws InterruptedException, TupleException {
        boolean matched = false;

        synchronized (tuples) {
            while (!matched) {
                matched = findTuple(t, removeTuple);
                if (!matched) {
                    if (blocking) {
                        tuples.wait();
                    } else
                        return false;
                }
            } // while ( ! matched )
        } // synchronized ( tuples )

        // if we're here we found a matching tuple
        return true;
    }

    /**
     * Scans the tuple space for a matching tuple. This is the core of tuple
     * search and retrieval.
     * 
     * @param t
     * @param removeTuple
     * @return
     * @throws TupleException
     */
    private boolean findTuple(Tuple t, boolean removeTuple)
            throws TupleException {
        Tuple toMatch;
        for (int i = 0; i < length(); i++) {
            toMatch = getTuple(i);
            if (toMatch.match(t)) {
                if (removeTuple) {
                    removeTuple(i);
                    // notify possible blocked out operations
                    synchronized (toMatch) {
                        toMatch.setWasMatched(true);
                        toMatch.notifyAll();
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected boolean ReadIn(Tuple t, boolean removeTuple, long TimeOut)
            throws TimeoutException, TupleException {
        boolean matched = false;

        synchronized (tuples) {
            long waitTime = TimeOut;
            long startTime = System.currentTimeMillis();
            long timeSoFar;
            while (!matched) {
                matched = findTuple(t, removeTuple);
                if (!matched) {
                    timeSoFar = System.currentTimeMillis() - startTime;
                    if (timeSoFar >= TimeOut) {
                        // TIME OUT !
                        throw new TimeoutException("tuple : " + t);
                    }
                    try {
                        waitTime = TimeOut - timeSoFar;
                        tuples.wait(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                    timeSoFar = System.currentTimeMillis() - startTime;
                    if (timeSoFar >= TimeOut) {
                        // TIME OUT !
                        throw new TimeoutException("tuple : " + t);
                    }
                }
            } // while ( ! matched )
        } // synchronized ( tuples )

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("{ ");

        synchronized (tuples) {
            for (int i = 0; i < length(); i++) {
                sb.append(getTuple(i));
                if (i < length() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    public int length() {
        return tuples.size();
    }

    public Tuple getTuple(int index) {
        return tuples.elementAt(index);
    }

    public void removeTuple(int i) {
        synchronized (tuples) {
            Tuple tuple = tuples.elementAt(i);
            tuples.removeElementAt(i);
            generate(TupleEvent.EventId, new TupleEvent(this, tuple,
                    TupleEventType.REMOVED));
        }
    }

    public void removeAllTuples() {
        synchronized (tuples) {
            tuples.removeAllElements();
            generate(TupleEvent.EventId, new TupleEvent(this,
                    TupleEventType.REMOVEDALL));
        }
    }

    public Vector<Tuple> getTuples() {
        return tuples;
    }

    public Enumeration<Tuple> getTupleEnumeration() {
        return tuples.elements();
    }

    public boolean isFormal() {
        return length() == 0;
    }

    public void setFormal() {
        tuples = new Vector<Tuple>();
    }

    public void setValue(Object o) throws TupleException {
        if (o != null && o instanceof TupleSpaceVector)
            tuples = ((TupleSpaceVector) o).getTuples();
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof TupleSpaceVector) {
            TupleSpace tupleSpace = (TupleSpace) o;

            if (length() != tupleSpace.length())
                return false;

            Enumeration<Tuple> mine = getTupleEnumeration();
            Enumeration<Tuple> its = tupleSpace.getTupleEnumeration();

            while (mine.hasMoreElements()) {
                if (!mine.nextElement().equals(its.nextElement()))
                    return false;
            }

            return true;
        }

        return false;
    }

    public Object duplicate() {
        return new TupleSpaceVector(this);
    }

    public void setValue(String o) throws TupleException {
        // TODO implement it with a recursive call?
        throw new TupleException(
                "TupleSpaceVector: conversion from string feature not implemented");
    }

    public void out_b(Tuple t) throws InterruptedException, TupleException {
        // before inserting the tuple reset wasMatched, since the tuple we out
        // could have been retrieved (and thus already matched)
        t.setWasMatched(false);
        
        out(t);
        synchronized (t) {
            // wait for someone to match this tuple with an in
            while (!t.wasMatched) {
                try {
                    t.wait();
                } catch (InterruptedException e) {
                    // in case of exception, make sure to remove the tuple
                    in_nb(t);
                    throw e;
                }
            }
        }
    }

    public Tuple in() throws InterruptedException {
        synchronized (tuples) {
            while (tuples.size() == 0)
                tuples.wait();

            // we remove the first one,
            Tuple t = tuples.elementAt(0); 
            tuples.removeElementAt(0);
            synchronized (t) {
                t.setWasMatched(true);
                t.notifyAll();
            }

            return t;
        }
    }
}
