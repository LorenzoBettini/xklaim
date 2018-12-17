package org.mikado.imc.protocols.tcp;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerFactory;

/**
 * The factory for tcp protocol layers.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TcpIpProtocolLayerFactory implements ProtocolLayerFactory {
    public ProtocolLayer createProtocolLayer() throws ProtocolException {
		return new TcpIpProtocolLayer();
	}

}