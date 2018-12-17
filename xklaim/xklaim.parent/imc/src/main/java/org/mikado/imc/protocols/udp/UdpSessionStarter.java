/*
 * Created on Apr 13, 2006
 */
/**
 * 
 */
package org.mikado.imc.protocols.udp;

import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerComposite;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionNumberLayer;

/**
 * This is a specialization of UdpSessionStarterPure that uses also
 * SessionNumberProtocolLayer to enumerate each UDP packet with a sequential
 * number, and also sends a closing packet when closing.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class UdpSessionStarter extends UdpSessionStarterPure {

    /**
     * 
     */
    public UdpSessionStarter() {
        super();
    }

    /**
     * @param localSessionId
     * @param remoteSessionId
     */
    public UdpSessionStarter(SessionId localSessionId, SessionId remoteSessionId) {
        super(localSessionId, remoteSessionId);
    }

    /**
     * @param datagramDispatcher
     */
    public UdpSessionStarter(DatagramDispatcher datagramDispatcher) {
        super(datagramDispatcher);
    }

    /**
     * @param datagramSocket
     */
    public UdpSessionStarter(DatagramSocket datagramSocket) {
        super(datagramSocket);
    }

    /**
     * @throws ProtocolException
     * @see org.mikado.imc.protocols.udp.UdpSessionStarterPure#createProtocolLayer()
     */
    @Override
    protected ProtocolLayer createProtocolLayer() throws ProtocolException {
        return new ProtocolLayerComposite(new SessionNumberLayer(), super
                .createProtocolLayer());
    }

    /**
     * @throws ProtocolException
     * @see org.mikado.imc.protocols.udp.UdpSessionStarterPure#createProtocolLayer(org.mikado.imc.protocols.udp.DatagramQueue,
     *      java.net.SocketAddress)
     */
    @Override
    protected ProtocolLayer createProtocolLayer(DatagramQueue datagramQueue,
            SocketAddress remote) throws ProtocolException {
        return new ProtocolLayerComposite(new SessionNumberLayer(), super
                .createProtocolLayer(datagramQueue, remote));
    }
}
