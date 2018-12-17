/*
 * Created on Feb 24, 2005
 */
package org.mikado.imc.protocols;

/**
 * A more involved ProtocolLayer that implements a protocol with states.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.10 $
 */
public class ProtocolLayerWithStates extends ProtocolLayer {
    private Protocol protocol = new Protocol();

    /**
     * Construct a ProtocolLayerWithStates
     */
    public ProtocolLayerWithStates() {
    }

    /**
     * Construct a ProtocolLayerWithStates with an associated first protocol
     * state.
     * 
     * @param startState
     */
    public ProtocolLayerWithStates(ProtocolState startState) {
        protocol = new Protocol(startState);
        startState.setProtocolLayer(this);
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolLayer#createUnMarshaler()
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        TransmissionChannel transmissionChannel = new TransmissionChannel(
                unMarshaler);
        protocol.start(transmissionChannel);
        return transmissionChannel.unMarshaler;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolLayer#setProtocolStack(org.mikado.imc.protocols.ProtocolStack)
     */
    public void setProtocolStack(ProtocolStack protocolStack) throws ProtocolException {
        super.setProtocolStack(protocolStack);
        protocol.setProtocolStack(protocolStack);
    }

    /**
     * Removes the ProtocolState corresponding to the passed identifier
     * 
     * @param state_id
     * @return the removed ProtocolState
     */
    public ProtocolState removeState(String state_id) {
        return protocol.removeState(state_id);
    }

    /**
     * @param state_id
     * @param state
     * @throws ProtocolException
     */
    public void setState(String state_id, ProtocolState state)
            throws ProtocolException {
        state.setProtocolLayer(this);
        protocol.setState(state_id, state);
    }
}
