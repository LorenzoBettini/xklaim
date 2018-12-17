/*
 * Created on May 25, 2005
 */
package examples.protocol;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Reads something from an UnMarshaler, writes it to the stack, receives
 * something from the stack and writes it into a Marshaler
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EchoClientState extends ProtocolStateSimple {
    private UnMarshaler input;

    private Marshaler output;

    /**
     * @param input
     * @param output
     */
    public EchoClientState(UnMarshaler unMarshaler, Marshaler marshaler) {
        this.input = unMarshaler;
        this.output = marshaler;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        try {
            System.out.print("Reading a string to send ");
            String toSend = input.readStringLine();

            Marshaler marshaler = getMarshaler(transmissionChannel);
            marshaler.writeStringLine(toSend);
            releaseMarshaler(marshaler);
            output.writeStringLine("sent: " + toSend);

            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            String received = unMarshaler.readStringLine();
            output.writeStringLine("received: " + received);
            releaseUnMarshaler(unMarshaler);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}
