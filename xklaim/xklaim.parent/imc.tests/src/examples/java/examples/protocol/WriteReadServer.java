/*
 * Created on Mar 24, 2005
 */
package examples.protocol;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class WriteReadServer {

    List<String> strings = Collections
            .synchronizedList(new LinkedList<String>());

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     * 
     * Handle a READ or a REMOVE request.
     */
    public class ReadState extends ProtocolStateSimple {
        /**
         * @param next_state
         */
        public ReadState(String next_state) {
            super(next_state);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            String line = "";
            try {
                try {
                    if (param.equals("REMOVE"))
                        line = strings.remove(0);
                    else
                        line = strings.get(0);
                } catch (IndexOutOfBoundsException ie) {
                    line = "ERROR: nothing to read or remove";
                }

                Marshaler marshaler = createMarshaler();
                marshaler.writeStringLine(line);
                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     * 
     * Handle a WRITE request.
     */
    public class WriteState extends ProtocolStateSimple {
        /**
         * @param next_state
         */
        public WriteState(String next_state) {
            super(next_state);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            String line = "";
            try {
                line = transmissionChannel.readStringLine();
                strings.add(line);

                Marshaler marshaler = createMarshaler();
                marshaler.writeStringLine("OK");
                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }

    public WriteReadServer(String host) throws ProtocolException, IOException {
        ProtocolSwitchState protocolSwitchState = new ProtocolSwitchState(
                Protocol.START);
        protocolSwitchState.addRequestState("WRITE", new WriteState(
                Protocol.START));
        protocolSwitchState.addRequestState("READ", new ReadState(
                Protocol.START));
        protocolSwitchState.addRequestState("REMOVE", new ReadState(
                Protocol.START));
        protocolSwitchState.addRequestState("QUIT", Protocol.END);
        Protocol protocol = new Protocol();
        protocol.setState(Protocol.START, protocolSwitchState);
        new GenericServer(host, protocol);
    }

    public static void main(String[] args) throws ProtocolException,
            IOException {
        String host = "tcp" + SessionId.PROTO_SEPARATOR + "localhost:9999";

        if (args.length > 0)
            host = args[0];

        new WriteReadServer(host);
    }
}
