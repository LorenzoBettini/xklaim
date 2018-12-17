/*
 * Created on Jan 19, 2005
 *
 */
package examples.linda;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.ProtocolSwitchState;
import org.mikado.imc.protocols.TransmissionChannel;

import java.io.IOException;


/**
 * Implements the simple Linda protocol for exchanging tuples.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LindaProtocolState extends ProtocolSwitchState {
    /** The tuple space. */
    protected TupleSpace tupleSpace;
    private final String ok = "OK";
    private final String not_found = "NOT FOUND";

    /**
     * Creates a new LindaProtocolState object.
     *
     * @param tupleSpace
     */
    public LindaProtocolState(TupleSpace tupleSpace) {
        this.tupleSpace = tupleSpace;
        addRequestState("OUT", new OutState());
        addRequestState("IN", new InState());
        addRequestState("READ", new InState());
        addRequestState("STOP", Protocol.END);
        setNextState(Protocol.START);
    }

    /**
     * Implements the state where we received an out request.
     *
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public class OutState extends ProtocolStateSimple {
        /**
         * Creates a new OutState object.
         */
        public OutState() {
            setNextState(Protocol.START);
        }

        /* (non-Javadoc)
        * @see org.mikado.imc.newprotocols.ProtocolState#enter(java.lang.Object)
        */
        public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {            
            try {
                String tuple = transmissionChannel.readStringLine();
                tupleSpace.add(tuple);
                Marshaler marshaler = createMarshaler();
                marshaler.writeStringLine(ok);
                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }

    /**
     * Implements the state where we received an input request.
     *
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public class InState extends ProtocolStateSimple {
        /**
         * Creates a new InState object.
         */
        public InState() {
            setNextState(Protocol.START);
        }

        /* (non-Javadoc)
        * @see org.mikado.imc.newprotocols.ProtocolState#enter(java.lang.Object)
        */
        public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {            
            try {
                String tuple = transmissionChannel.readStringLine();
                Object matched = null;

                if (param.equals("IN")) {
                    matched = tupleSpace.remove(tuple);
                } else {
                    matched = tupleSpace.get(tuple);
                }

                Marshaler marshaler = createMarshaler();

                if (matched != null) {
                    marshaler.writeStringLine(matched.toString());
                } else {
                    marshaler.writeStringLine(not_found);
                }

                releaseMarshaler(marshaler);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }
}
