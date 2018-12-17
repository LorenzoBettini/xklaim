/*
 * Created on Jan 25, 2006
 */
package org.mikado.imc.topology;

import java.util.Enumeration;
import java.util.Hashtable;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventGeneratorAdapter;

/**
 * Contains references to elements that can be closed.
 * 
 * The elements that are inserted here are supposed to remove themselves from
 * the collection when they finished the execution.
 * 
 * This is an abstract class since the actual addition, removal, and closing of
 * each single element is left unimplemented. However, it guarantees that once
 * it is closing no further addition is accepted (it raises an exception) and
 * that subsequent close invocation simply return.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public abstract class CloseableContainer<E> extends EventGeneratorAdapter {
    /**
     * The actuall table storing processes indexed with their name
     */
    protected Hashtable<String, E> elements = new Hashtable<String, E>();

    /**
     * Avoids processes add other processes when this is closing.
     */
    protected boolean closing = false;

    /**
     * Adds an element to the collection.
     * 
     * @param element
     * @throws IMCException
     *             if we try to add a process when this is closing
     */
    synchronized public void addElement(E element) throws IMCException {
        if (closing)
            throw new IMCException("sorry we're closed");

        doAddElement(element);
    }

    /**
     * The actual implementation of addition of an element.
     * 
     * @param element
     */
    protected abstract void doAddElement(E element);

    /**
     * Removes an element from the collection.
     * 
     * If we are already closing this method simply returns.
     * 
     * @param element
     */
    synchronized public void removeElement(E element) {
        /* no need to remove it, since we're already closing */
        if (closing)
            return;

        doRemoveElement(element);
    }

    /**
     * The actual implementation of removal of an element
     * 
     * @param element
     */
    protected abstract void doRemoveElement(E element);

    /**
     * Closes all the elements in this collection.
     * 
     * It is guaranteed that during the retrieval of the enumeration of all the
     * elements we are synchronized and after that moment the closing flag is
     * set. (and in that case a further addition would throw an exception).
     * 
     * If during the closing of an element an exception is thrown, that
     * exception is stored and we continue on closing all the remaining
     * elements. Then, in case exceptions were thrown, we throw re-throw them
     * (all in one single exception with the messages of all caught exceptions).
     * 
     * @throws IMCException
     */
    public void close() throws IMCException {
        Enumeration<E> enumeration = null;

        /*
         * we synchronize only the updating of closing and the retrieval of
         * process references; from now on we're sure that no one will modify
         * the collection of processes (remove will return, and add will throw
         * an exception)
         */
        synchronized (this) {
            closing = true;
            enumeration = elements.elements();
        }

        StringBuffer exceptions = new StringBuffer();

        while (enumeration.hasMoreElements()) {
            try {
                doCloseElement(enumeration.nextElement());
            } catch (IMCException e) {
                exceptions.append(" " + e.getMessage());
            }
        }

        if (exceptions.length() > 0)
            throw new IMCException(exceptions.toString());
    }

    /**
     * The actual implementation of closing of an element
     * 
     * @param element
     * @throws IMCException
     */
    protected abstract void doCloseElement(E element) throws IMCException;

    /**
     * @see java.util.Hashtable#size()
     */
    public int size() {
        return elements.size();
    }
}
