/*
 * Created on May 22, 2006
 */
/**
 * 
 */
package org.mikado.imc.topology;

/**
 * Container for CollectableThreads.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ThreadContainer extends ProcessContainer<CollectableThread> {

    /**
     * Also sets this container in the CollectableThread added.
     * 
     * @see org.mikado.imc.topology.ProcessContainer#doAddElement(Thread)
     */
    @Override
    protected void doAddElement(CollectableThread element) {
        super.doAddElement(element);
        element.setThreadContainer(this);
    }
}
