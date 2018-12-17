/*
 * Created on Jan 13, 2005
 *
 */
package org.mikado.imc.protocols;



/**
 * Abstract factory used to create a Protocol
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface ProtocolFactory {
    /**
     * Creates a Protocol.
     *
     * @return The created Protocol.
     *
     * @throws ProtocolException 
     */
    Protocol createProtocol() throws ProtocolException;
}
