/*
 * Created on Jan 9, 2005
 */
package org.mikado.imc.protocols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple thread that, given a ProtocolLayer, continuosly performs a read
 * operation from the layer.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class ProtocolLayerThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolLayerThread.class);

    /** the protocol layer used to read */
    protected ProtocolLayer protocolLayer;

    /**
     * Creates a new ProtocolLayerThread object.
     *
     * @param layer 
     */
    public ProtocolLayerThread(ProtocolLayer layer) {
        this(layer, "ProtoclLayerThread");
    }

    /**
     * Creates a new ProtocolLayerThread object.
     *
     * @param layer 
     * @param name
     */
    public ProtocolLayerThread(ProtocolLayer layer, String name) {
        super(name);
        protocolLayer = layer;
    }

    /**
     * Continuosly performs read from the layer
     */
    public void run() {
        while (true) {
            try {
                protocolLayer.doCreateUnMarshaler(null);
            } catch (ProtocolException e) {
                LOGGER.error("protocol layer error", e);

                return;
            }
        }
    }
    
    public void close() throws ProtocolException {
        protocolLayer.doClose();
    }
}
