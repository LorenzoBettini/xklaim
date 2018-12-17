/*
 * Created on Jan 25, 2006
 */
package org.mikado.imc.topology;

import java.util.Enumeration;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.ProcessEvent;

/**
 * Contains references to processes running on a node.
 * 
 * The processes that are inserted here are supposed to remove themselves from
 * the collection when they finished the execution.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public class ProcessContainer<T extends Thread> extends CloseableContainer<T> {
    /**
     * Timeout waiting for a thread to terminate (default: 4 seconds)
     */
    int timeout = 4000; // 4 seconds should be enough?

    /**
     * @param element
     */
    @Override
    protected void doAddElement(T element) {
        elements.put(element.getName(), element);
        generate(ProcessEvent.ProcessEventId, new ProcessEvent(this,
                ProcessEvent.EventType.ADDED, element));
    }

    /**
     * Adds a thread to the collection and start it.
     * 
     * @param thread
     * @throws IMCException
     */
    public void addAndStart(T thread) throws IMCException {
        addElement(thread);
        thread.start();
    }

    /**
     * @param element
     */
    @Override
    protected void doRemoveElement(T element) {
        elements.remove(element.getName());
        generate(ProcessEvent.ProcessEventId, new ProcessEvent(this,
                ProcessEvent.EventType.REMOVED, element));
    }

    /**
     * @param thread
     * @throws IMCException
     */
    @Override
    protected void doCloseElement(T thread) throws IMCException {
        /* first we try to interrupt the thread */
        thread.interrupt();

        /*
         * we also call the method close, if it's an IMC related thread
         */
        if (thread instanceof NodeProcess) {
            ((NodeProcess) thread).close();
        } else if (thread instanceof NodeCoordinator) {
            ((NodeCoordinator) thread).close();
        }

        try {
            thread.join(timeout);
            if (thread.isAlive()) {
                throw new IMCException("non responding process: "
                        + thread.getName());
            } else {
                generate(ProcessEvent.ProcessEventId, new ProcessEvent(this,
                        ProcessEvent.EventType.REMOVED, thread));
            }
        } catch (InterruptedException e) {
            throw new IMCException("while terminating process"
                    + thread.getName(), e);
        }

    }

    /**
     * Waits for all the threads currently stored in the container terminate the
     * execution.
     * 
     * @param timeout
     *            Timeout for waiting (0 means forever)
     * @throws InterruptedException
     */
    public void join(long timeout) throws InterruptedException {
        Enumeration<T> enumeration = null;

        enumeration = elements.elements();

        while (enumeration.hasMoreElements()) {
            Thread thread = enumeration.nextElement();
            thread.join(timeout);
        }
    }

    /**
     * Waits for all the threads currently stored in the container terminate the
     * execution.
     * 
     * @throws InterruptedException
     */
    public void join() throws InterruptedException {
        join(0);
    }
}
