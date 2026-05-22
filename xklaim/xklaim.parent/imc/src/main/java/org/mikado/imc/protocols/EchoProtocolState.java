/*
 * Created on Jan 20, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple state that just echoes what it receives.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class EchoProtocolState extends ProtocolStateSimple {
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoProtocolState.class);

    /**
     * Creates a new EchoProtocolState object.
     */
    public EchoProtocolState() {
        super();
    }

    /**
     * Creates a new EchoProtocolState object.
     * 
     * @param next_state
     */
    public EchoProtocolState(String next_state) {
        super(next_state);
    }

    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        LOGGER.debug("{}: reading a line", getClass().getSimpleName());

        try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            String line = unMarshaler.readStringLine();
            LOGGER.debug("{}: read line: {}", getClass().getSimpleName(), line);
            releaseUnMarshaler(unMarshaler);
            Marshaler marshaler = getMarshaler(transmissionChannel);
            marshaler.writeStringLine(line);
            releaseMarshaler(marshaler);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}
