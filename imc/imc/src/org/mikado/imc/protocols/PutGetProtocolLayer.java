/*
 * Created on Jan 20, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.DataInput;
import java.io.IOException;

/**
 * This layer removes a string "PUT:" when reading and adds a string "GET:" when
 * performing down in the stack.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class PutGetProtocolLayer extends ProtocolLayerWithStates {
    public static final String malformed = "MALFORMED REQUEST";

    /**
     * The state reading a "PUT:" string.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.8 $
     */
    public class PutGetState extends ProtocolStateSimple {
        /**
         * Constructus a PutGetState whose next state is END.
         */
        public PutGetState() {
            this(Protocol.END);
        }

        /**
         * Constructus a PutGetState.
         * 
         * @param next_state
         */
        public PutGetState(String next_state) {
            super(next_state);
        }

        /**
         * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            if (transmissionChannel == null
                    || transmissionChannel.unMarshaler == null) {
                throw new ProtocolException("no transmission available");
            }

            String req;
            while (true) {
                try {
                    req = getCode(transmissionChannel.unMarshaler);

                    // System.out.println("request: " + req);
                    if (!req.equals("PUT:")) {
                        Marshaler marshaler = createMarshaler();
                        marshaler.writeStringLine(malformed);
                        releaseMarshaler(marshaler);
                        // discard the rest and perform another read
                        transmissionChannel.unMarshaler.clear();
                        transmissionChannel.unMarshaler = createUnMarshaler();
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    throw new ProtocolException(e);
                }
            }
        }
    }

    /**
     * Constructs a PutGetProtocolLayer.
     * @throws ProtocolException 
     */
    public PutGetProtocolLayer() throws ProtocolException {
        setState(Protocol.START, new PutGetState());
    }

    /**
     * Inserts a "GET:" before the actual payload.
     * 
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        try {
            marshaler.writeBytes("GET:");
            return marshaler;
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Reads a command string delimited by ":"
     * 
     * @param in
     *            The input source.
     * 
     * @return The string terminated by ":"
     * 
     * @throws IOException
     */
    public static String getCode(DataInput in) throws IOException {
        StringBuffer buff = new StringBuffer();

        while (true) {
            byte b;

            b = in.readByte();
            buff.append((char) b);

            // System.out.println("Read BYTE: " + b);
            if (b == ':') {
                break;
            }
        }

        return buff.toString();
    }
}
