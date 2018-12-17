/*
 * Created on Apr 13, 2006
 */
/**
 * 
 */
package org.mikado.imc.protocols;

/**
 * A pair of ProtocolLayers.
 * 
 * This very ProtocolLayer instance will never be present in a ProtocolStack
 * since, as soon as it is inserted, this layer will replace itself with the two
 * ProtocolLayers (the first and the second).
 * 
 * NOTE: For the moment this class is for internal use only and it is likely to
 * change.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ProtocolLayerComposite extends ProtocolLayer {
    private ProtocolLayer first;

    private ProtocolLayer second;

    /**
     * Constructs a ProtocolLayerComposite where the first layer will be
     * considered higher in the stack than the second layer.
     * 
     * @param first
     * @param second
     * @throws ProtocolException
     */
    public ProtocolLayerComposite(ProtocolLayer first, ProtocolLayer second)
            throws ProtocolException {
        this.first = first;
        this.second = second;

        /*
         * this is useful if this layer has no protocolStack associated; this
         * way, the two enclosed layers will still be able to execute operations
         * such as close.
         */
        protocolStack = new ProtocolStack();
        protocolStack.insertFirstLayer(first);
        protocolStack.setLowLayer(second);
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolLayer#setProtocolStack(org.mikado.imc.protocols.ProtocolStack)
     */
    @Override
    public void setProtocolStack(ProtocolStack protocolStack)
            throws ProtocolException {
        super.setProtocolStack(protocolStack);
        
        if (protocolStack == null)
            return;

        protocolStack.replace(this, first);
        protocolStack.insertAfter(first, second);
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolLayer#doClose()
     */
    @Override
    public void doClose() throws ProtocolException {
        /* this ensures that the two enclosed layers' doClose methods are called */
        protocolStack.close();
    }

}
