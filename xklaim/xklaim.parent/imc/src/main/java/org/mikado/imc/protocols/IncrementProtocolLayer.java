/*
 * Created on Jan 20, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This layer removes an integer from the input, and writes the received number
 * incremented to the output.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class IncrementProtocolLayer extends ProtocolLayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IncrementProtocolLayer.class);

    /** The sequence number */
    int sequence = 0;

    /**
     * Removes an integer and records it as a sequence number.
     *
     * @throws ProtocolException 
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler) throws ProtocolException {
    	while (true) {
	        try {
	            sequence = unMarshaler.readInt();
	            LOGGER.debug("received sequence number: {}", sequence);
	
	            if (sequence < 0) {
	            	Marshaler marshaler = createMarshaler();
	                marshaler.writeStringLine("NEGATIVE SEQUENCE NUMBER");
	                releaseMarshaler(marshaler);
	            } else {
	            	break;
	            }
	        } catch (IOException e) {
	            throw new ProtocolException(e);
	        }
    	}
    	
    	return unMarshaler;
    }

    /**
     * Writes the previously read sequence number incremented.
     *
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler) throws ProtocolException {
        try {
            marshaler.writeInt(sequence + 1);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
        
        return marshaler;
    }
}
