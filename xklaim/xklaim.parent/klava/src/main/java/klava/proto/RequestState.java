/**
 * 
 */
package klava.proto;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;

/**
 * The base class for a state involving a request, thus dealing with a FROM and
 * PROCESS string.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class RequestState extends ProtocolStateSimple {
    public final static String FROM_S = "FROM";

    public final static String PROCESS_S = "PROCESS";

    /**
     * The value received with FROM
     */
    protected SessionId from;

    /**
     * The value received with PROCESS
     */
    protected String process;

    /**
     * Reads the FROM and the PROCESS fields.
     * 
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

        try {
            String fromString = unMarshaler.readStringLine();
            if (!fromString.equals(FROM_S))
                throw new WrongStringProtocolException(FROM_S, fromString);
            from = SessionId.parseSessionId(unMarshaler.readStringLine());

            process = unMarshaler.readStringLine();
            if (!process.equals(PROCESS_S))
                throw new WrongStringProtocolException(PROCESS_S, process);
            process = unMarshaler.readStringLine();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Writes the FROM and PROCESS field in the passed Marshaler.
     * 
     * @param marshaler
     * @param from
     * @param process
     * @throws IOException
     */
    public static void sendRequest(Marshaler marshaler, SessionId from,
            String process) throws IOException {
        marshaler.writeStringLine(FROM_S);
        marshaler.writeStringLine(from.toString());
        marshaler.writeStringLine(PROCESS_S);
        marshaler.writeStringLine(process);
    }
}
