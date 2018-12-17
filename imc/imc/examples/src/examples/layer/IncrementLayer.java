/*
 * Created on Jan 20, 2005
 *
 */
package examples.layer;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * This layer removes an integer from the input, and writes the received number
 * incremented to the output. The integer is read as a string.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class IncrementLayer extends ProtocolLayer {
    /** The sequence number */
    int sequence = 0;

    /**
     * Removes a string line representing an integer and records it as a
     * sequence number.
     * 
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        try {
            while (true) {
                String sequenceString = unMarshaler.readStringLine();

                try {
                    sequence = Integer.parseInt(sequenceString);

                    if (sequence < 0) {
                        Marshaler marshaler = createMarshaler();
                        marshaler
                                .writeStringLine("ERROR: negative sequence number");
                        releaseMarshaler(marshaler);
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    Marshaler marshaler = createMarshaler();
                    marshaler.writeStringLine("ERROR: invalid number: "
                            + sequenceString);
                    releaseMarshaler(marshaler);
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return unMarshaler;
    }

    /**
     * Writes the previously read sequence number as a string after incrementing
     * it.
     * 
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        try {
            marshaler.writeStringLine("" + (sequence + 1));
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return marshaler;
    }
}
