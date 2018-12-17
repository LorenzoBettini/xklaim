/*
 * Created on Mar 29, 2006
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;

/**
 * Generic interface for a resource that can be closed. 
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface Closeable {

    /**
     * Closes this resource.
     * 
     * @throws IMCException
     */
    void close() throws IMCException;

}