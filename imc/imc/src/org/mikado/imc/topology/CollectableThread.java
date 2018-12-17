/*
 * Created on May 22, 2006
 */
/**
 * 
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;

/**
 * A specialized Thread that is not a NodeProcess, i.e., it needs not a Node to
 * execute, but can be collected in a ThreadCollector.
 * 
 * As NodeProcess, run is already redefined in order to call execute (that
 * subclasses must specialize) and to remove the thread from the collection it
 * is stored in (if the thread is stored in a collection).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public abstract class CollectableThread extends Thread {
    /** Where this thread is collected */
    protected ThreadContainer threadContainer = null;

    /**
     * Method used to start the thread. This will implicitly call execute().
     */
    public final void run() {
        try {
            execute();
        } catch (IMCException e) {
            e.printStackTrace();
        } finally {
            if (threadContainer != null)
                threadContainer.removeElement(this);
        }
    }

    /**
     * This is the method invoked to actually start the thread. Derived classes
     * must provide an implementation for it.
     * 
     * @throws IMCException
     */
    protected abstract void execute() throws IMCException;

    /**
     * @return Returns the threadContainer.
     */
    public ThreadContainer getThreadContainer() {
        return threadContainer;
    }

    /**
     * @param threadContainer The threadContainer to set.
     */
    public void setThreadContainer(ThreadContainer threadContainer) {
        this.threadContainer = threadContainer;
    }
}
