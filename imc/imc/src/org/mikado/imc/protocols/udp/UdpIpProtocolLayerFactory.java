package org.mikado.imc.protocols.udp;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerFactory;

/**
 * The factory for udp protocol layers.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class UdpIpProtocolLayerFactory implements ProtocolLayerFactory {
    public ProtocolLayer createProtocolLayer() throws ProtocolException {
		return new UdpIpProtocolLayer();
	}

}