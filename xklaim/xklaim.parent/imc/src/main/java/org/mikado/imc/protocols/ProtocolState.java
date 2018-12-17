/*
 * Created on Jan 12, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.events.EventGeneratorAdapter;

/**
 * Represents a state of a Protocol.
 * 
 * A ProtocolState includes a ProtocolStack. Subclasses should override enter(),
 * which is abstract and updateReference. Notice that the "set" methods depend
 * on the implementation of the state (for instance, if the state contains other
 * structures, they should be overridden concordingly).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public abstract class ProtocolState extends EventGeneratorAdapter {
    /** The ProtocolLayer inside this state */
    protected ProtocolStack protocolStack;

    /** The (possible) protocol layer this ProtocolState belongs to */
    protected ProtocolLayer protocolLayer = null;

    /** The next state of this state. Default: END */
    protected String next_state = Protocol.END;

    /**
     * Creates a new ProtocolState object.
     */
    public ProtocolState() {
    }

    /**
     * Creates a ProtocolState that belongs to a specific ProtocolLayer
     * 
     * @param protocolLayer
     */
    public ProtocolState(ProtocolLayer protocolLayer) {
        this.protocolLayer = protocolLayer;
    }

    /**
     * Creates a ProtocolState that has a specific next state.
     * 
     * @param next_state
     */
    public ProtocolState(String next_state) {
        this.next_state = next_state;
    }

    /**
     * Returns the next state.
     * 
     * @return Returns the next_state.
     */
    public final String getNextState() {
        return next_state;
    }

    /**
     * Sets the next state.
     * 
     * @param next_state
     *            The next_state to set.
     */
    public final void setNextState(String next_state) {
        this.next_state = next_state;
    }

    /**
     * Delegates to the ProtocolStack
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        protocolStack.close();
    }

    /**
     * This method is called when the ProtocolState is closed.
     * 
     * The default implementation is empty, and subclasses can specialize this
     * method in order to perform some cleaning operations.
     * 
     * @throws ProtocolException
     */
    public void closed() throws ProtocolException {
    }

    /**
     * Delegates to the ProtocolStack
     * 
     * @param marshaler
     * 
     * @throws ProtocolException
     */
    public void releaseMarshaler(Marshaler marshaler) throws ProtocolException {
        protocolStack.releaseMarshaler(marshaler, protocolLayer);
    }

    /**
     * Delegates to the ProtocolStack
     * 
     * @param unMarshaler
     * 
     * @throws ProtocolException
     */
    public void releaseUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        protocolStack.releaseUnMarshaler(unMarshaler, protocolLayer);
    }

    /**
     * Returns the Session of the ProtocolStack.
     * 
     * @return the Session of the ProtocolStack.
     * @throws ProtocolException
     */
    public Session getSession() throws ProtocolException {
        return protocolStack.getSession();
    }

    /**
     * Delegates to the ProtocolStack
     * 
     * @return the created Marshaler
     * 
     * @throws ProtocolException
     */
    public Marshaler createMarshaler() throws ProtocolException {
        return protocolStack.createMarshaler(protocolLayer);
    }

    /**
     * Delegates to the ProtocolStack
     * 
     * @return the created UnMarshaler
     * 
     * @throws ProtocolException
     */
    public UnMarshaler createUnMarshaler() throws ProtocolException {
        return protocolStack.createUnMarshaler(protocolLayer);
    }

    /**
     * Returns the protocolStack.
     * 
     * @return Returns the protocolStack.
     */
    public final ProtocolStack getProtocolStack() {
        return protocolStack;
    }

    /**
     * Checks whether the passed TransmissionChannel contains a non-null
     * UnMarshaler and returns it, otherwise tries to retrieve one from the
     * underlying ProtocolStack and sets into the TransmissionChannel too,
     * provided the TransmissionChannel is not null.
     * 
     * @param transmissionChannel
     * @return the UnMarshaler
     * @throws ProtocolException
     */
    protected UnMarshaler getUnMarshaler(TransmissionChannel transmissionChannel)
            throws ProtocolException {
        if (transmissionChannel != null
                && transmissionChannel.unMarshaler != null)
            return transmissionChannel.unMarshaler;

        try {
            UnMarshaler unMarshaler = createUnMarshaler();
            if (transmissionChannel != null)
                transmissionChannel.unMarshaler = unMarshaler;
            return unMarshaler;
        } catch (NullPointerException e) {
            throw new ProtocolException("cannot create UnMarshaler: null stack");
        }
    }

    /**
     * Checks whether the passed TransmissionChannel contains a non-null
     * Marshaler and returns it, otherwise tries to retrieve one from the
     * underlying ProtocolStack and sets into the TransmissionChannel too,
     * provided the TransmissionChannel is not null.
     * 
     * @param transmissionChannel
     * @return the Marshaler
     * @throws ProtocolException
     */
    protected Marshaler getMarshaler(TransmissionChannel transmissionChannel)
            throws ProtocolException {
        if (transmissionChannel != null
                && transmissionChannel.marshaler != null)
            return transmissionChannel.marshaler;

        try {
            Marshaler marshaler = createMarshaler();
            if (transmissionChannel != null)
                transmissionChannel.marshaler = marshaler;
            return marshaler;
        } catch (NullPointerException e) {
            throw new ProtocolException("cannot create Marshaler: null stack");
        }
    }

    /**
     * <p>
     * The method to enter this state.
     * </p>
     * 
     * <p>
     * This is the main method of a protocol state, since it should contain the
     * actual implementation of this protocol state.
     * </p>
     * 
     * <p>
     * IMPORTANT: please remember that before using the passed
     * transmissionChannel, you should check whether it is not null and whether
     * the contained unMarshaller is null; if it is null, you should invoke
     * createUnMarshaler(). There's an utility method that already checks this,
     * getUnMarshaler(TransmissionChannel), and to finish releaseUnMarshaler().
     * The same is for the marshaler contained in the transmissionChannel and if
     * it is null you should always invoke createMarshaler() (or
     * getMarshaler(TransmissionChannel), and to finish the writing you should
     * always invoke releaseMarshaler().
     * </p>
     * 
     * @param param
     *            A parameter that is protocol dependent.
     * @param transmissionChannel
     *            The channel to read from and write into. This can be null, and
     *            it is protocol implementation specific. In particular, the
     *            state should check whether it is null.
     * 
     * @throws ProtocolException
     */
    public abstract void enter(Object param,
            TransmissionChannel transmissionChannel) throws ProtocolException;

    /**
     * Sets the protocol stack used by this state.
     * 
     * @param protocolStack
     */
    public void setProtocolStack(ProtocolStack protocolStack) {
        this.protocolStack = protocolStack;
    }

    /**
     * @return Returns the protocolLayer.
     */
    public ProtocolLayer getProtocolLayer() {
        return protocolLayer;
    }

    /**
     * @param protocolLayer
     *            The protocolLayer to set.
     */
    public void setProtocolLayer(ProtocolLayer protocolLayer) {
        this.protocolLayer = protocolLayer;
    }
}
