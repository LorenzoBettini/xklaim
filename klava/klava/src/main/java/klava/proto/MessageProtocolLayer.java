/*
 * Created on Nov 29, 2005
 */
package klava.proto;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;

/**
 * Handles message header information
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class MessageProtocolLayer extends ProtocolLayer {

    public static final String MESSAGE_BEGIN = "MESSAGE";

    public static final String MESSAGE_END = "END MESSAGE";
    
    /**
     * 
     */
    public MessageProtocolLayer() {
    }

    /**
     * Sends a message header by using the passed Marshaler and returns the
     * Marshaler.
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateMarshaler(org.mikado.imc.protocols.Marshaler)
     */
    @Override
    public Marshaler doCreateMarshaler(Marshaler marshaler) throws ProtocolException {
        try {
            marshaler.writeStringLine(MESSAGE_BEGIN);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return marshaler;
    }

    /**
     * Removes the message header from the passed UnMarshaler and returns
     * the UnMarshaler.
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateUnMarshaler(org.mikado.imc.protocols.UnMarshaler)
     */
    @Override
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler) throws ProtocolException {
        String s;
        try {
            s = unMarshaler.readStringLine();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
        
        if (!s.equals(MESSAGE_BEGIN))
            throw new WrongStringProtocolException(MESSAGE_BEGIN, s);
        
        return unMarshaler;
    }
}
