/*
 * Created on Jan 20, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

/**
 * Simple state that just echoes what it receives.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class EchoProtocolState extends ProtocolStateSimple {
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
        print("reading a line");

        try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            String line = unMarshaler.readStringLine();
            print("read line: " + line);
            releaseUnMarshaler(unMarshaler);
            Marshaler marshaler = getMarshaler(transmissionChannel);
            marshaler.writeStringLine(line);
            releaseMarshaler(marshaler);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    void print(String s) {
        System.out.println(getClass().getSimpleName() + ": " + s);
    }
}
