/*
 * Created on Jan 9, 2005
 */
package org.mikado.imc.protocols;



/**
 * A simple thread that, given a ProtocolLayer, continuosly performs a read
 * operation from the layer.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class ProtocolLayerThread extends Thread {
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
                e.printStackTrace();

                return;
            }
        }
    }
    
    public void close() throws ProtocolException {
        protocolLayer.doClose();
    }
}
