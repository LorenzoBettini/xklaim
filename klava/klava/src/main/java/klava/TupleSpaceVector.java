package klava;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import klava.events.TupleEvent;
import klava.events.TupleEvent.TupleEventType;

import org.mikado.imc.events.EventGeneratorAdapter;

/**
 * Implements a TupleSpace through a Vector.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
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

    /**
     * @see klava.TupleSpace#out(klava.Tuple)
     */
    public void out(Tuple t) {
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

    public void outString(String s) throws KlavaTupleParsingException {
        out(TupleParser.parseString(s));
    }

    /**
     * @see klava.TupleSpace#read(klava.Tuple)
     */
    public boolean read(Tuple t) throws InterruptedException {
        return ReadIn(t, false, true);
    }

    /** 
     * @see klava.TupleSpace#in(klava.Tuple)
     */
    public boolean in(Tuple t) throws InterruptedException {
        return ReadIn(t, true, true);
    }

    // time out versions
    public boolean read(Tuple t, long TimeOut) throws KlavaTimeOutException,
            InterruptedException {
        if (TimeOut < 0)
            return read(t);
        else
            return ReadIn(t, false, TimeOut);
    }

    public boolean in(Tuple t, long TimeOut) throws KlavaTimeOutException,
            InterruptedException {
        if (TimeOut < 0)
            return in(t);
        else
            return ReadIn(t, true, TimeOut);
    }

    /** 
     * @see klava.TupleSpace#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple t, long TimeOut) throws InterruptedException {
        try {
            return read(t, TimeOut);
        } catch (KlavaTimeOutException e) {
            return false;
        }
    }

    /** 
     * @see klava.TupleSpace#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple t, long TimeOut) throws InterruptedException {
        try {
            return in(t, TimeOut);
        } catch (KlavaTimeOutException e) {
            return false;
        }
    }

    /** 
     * @see klava.TupleSpace#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple t) {
        return findTuple(t, false);
    }

    /** 
     * @see klava.TupleSpace#in_nb(klava.Tuple)
     */
    public boolean in_nb(Tuple t) {
        return findTuple(t, true);
    }

    protected boolean ReadIn(Tuple t, boolean removeTuple, boolean blocking)
            throws InterruptedException {
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

    private boolean findTuple(Tuple t, boolean removeTuple) {
        Tuple toMatch;
        for (int i = 0; i < length(); i++) {
            toMatch = getTuple(i);
            if (toMatch.match(t)) {
                if (removeTuple)
                    removeTuple(i);
                return true;
            }
        }
        return false;
    }

    // time out version
    protected boolean ReadIn(Tuple t, boolean removeTuple, long TimeOut)
            throws KlavaTimeOutException {
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
                        throw new KlavaTimeOutException("tuple : " + t);
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
                        throw new KlavaTimeOutException("tuple : " + t);
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

    /*
     * (non-Javadoc)
     * 
     * @see klava.TupleSpace#length()
     */
    public int length() {
        return tuples.size();
    }

    public Tuple getTuple(int index) {
        return tuples.elementAt(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see klava.TupleSpace#removeTuple(int)
     */
    public void removeTuple(int i) {
        synchronized (tuples) {
            Tuple tuple = tuples.elementAt(i);
            tuples.removeElementAt(i);
            generate(TupleEvent.EventId, new TupleEvent(this, tuple,
                    TupleEventType.REMOVED));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see klava.TupleSpace#removeAllTuples()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see klava.TupleSpace#getTupleEnumeration()
     */
    public Enumeration<Tuple> getTupleEnumeration() {
        return tuples.elements();
    }

    // TupleItem interface
    public boolean isFormal() {
        return length() == 0;
    }

    public void setFormal() {
        tuples = new Vector<Tuple>();
    }

    public void setValue(Object o) {
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

    public void setValue(String o) throws KlavaException {
        // TODO implement it with a recursive call?
        throw new KlavaException(
                "TupleSpaceVector: conversion from string feature not implemented");
    }
}
