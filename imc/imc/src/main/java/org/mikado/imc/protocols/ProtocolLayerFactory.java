/*
 * Created on Feb 6, 2005
 */
package org.mikado.imc.protocols;

/**
 * Abstract factory for protocol layers.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public interface ProtocolLayerFactory {
    /**
     * Creates a protocol layer.
     * 
     * @return the created ProtocolLayer
     * @throws ProtocolException
     */
    ProtocolLayer createProtocolLayer() throws ProtocolException;
}
