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
 * Reads a string line and checks that it does not start with the string ERROR.
 * If it does, tells the user about the error.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class CheckErrorLayer extends ProtocolLayer {
    /**
     * Checks that the read string does not start with the string ERROR. If so,
     * writes back to try again.
     * 
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        try {
            if (!unMarshaler.markSupported())
                throw new ProtocolException("mark not supported");

            while (true) {
                // set a check point, in order to read some bytes
                // and then put it back; just to check it does not contain ERROR
                unMarshaler.mark(255);

                String string = unMarshaler.readStringLine();

                if (string.startsWith("ERROR")) {
                    Marshaler marshaler = createMarshaler();
                    marshaler.writeStringLine(string);
                    marshaler.writeStringLine("please try again!");
                    releaseMarshaler(marshaler);
                } else {
                    // put back what we read so far
                    unMarshaler.reset();
                    return unMarshaler;
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

}
