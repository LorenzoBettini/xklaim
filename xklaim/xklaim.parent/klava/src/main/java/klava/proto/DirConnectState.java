/*
 * Created on Jul 25, 2005
 *
 */
package klava.proto;

import java.io.IOException;

import klava.NetUtils;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;


/**
 * The ProtocolState that tries to establish a direct connection
 * 
 * @author bettini
 *
 */
public class DirConnectState extends ProtocolStateSimple {
    private boolean success = false;
    
    private int port;
    
    /**
     * 
     */
    public DirConnectState(int port) {
        this.port = port;
    }

    /**
     * @return Returns the success.
     */
    public final boolean isSuccess() {
        return success;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object, org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {
        Marshaler marshaler = createMarshaler();
        try {
            marshaler.writeStringLine("CONNECT");
            // my physical locality
            marshaler.writeStringLine(
                    new IpSessionId(NetUtils.getLocalIPAddress(),port).toString());
            releaseMarshaler(marshaler);
    
            UnMarshaler unMarshaler = createUnMarshaler();
            success = Boolean.parseBoolean(unMarshaler.readStringLine());
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

}
