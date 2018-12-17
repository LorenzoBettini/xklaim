/*
 * Created on Jan 12, 2005
 *
 */
package org.mikado.imc.protocols;

/**
 * This ProtocolState implements enter by simply calling createUnMarshaler() on
 * the ProtocolLayer and then returns END as the next state. Subclasses should
 * override enter() to fit their needs (unless they're confortable with the
 * default behavior).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.9 $
 */
public class ProtocolStateSimple extends ProtocolState {
    /**
     * Creates a new ProtocolStateSimple object.
     */
    public ProtocolStateSimple() {
    }

    /**
     * Creates a ProtocolStateSimple that has a specific next state.
     * 
     * @param next_state
     */
    public ProtocolStateSimple(String next_state) {
        super(next_state);
    }

    /**
     * <p>
     * Simply calls createUnMarshaler() on the ProtocolLayer and then returns
     * END as the next state. Subclasses should override this behavior to fit
     * their needs (unless they're confortable with the default behavior).
     * </p>
     * 
     * <p>
     * Please take a look at the note about implementing enter() in
     * ProtocolState interface.
     * </p>
     * 
     * @param param
     * 
     * @throws ProtocolException
     */
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        protocolStack.createUnMarshaler();
    }

    /**
     * Sets the protocol stack used by this state.
     * 
     * @param protocolStack
     */
    public void setProtocolStack(ProtocolStack protocolStack) {
        this.protocolStack = protocolStack;
    }

    /**
     * Updates the next state id if it is equal to the specified one.
     * 
     * @param old_state
     *            The old id to change.
     * @param new_state
     *            The new id to change.
     */
    public void updateReference(String old_state, String new_state) {
        if (getNextState().equals(old_state)) {
            setNextState(new_state);
        }
    }

    /**
     * Closes the ProtocolStack
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        protocolStack.close();
    }

    /**
     * the Session of the ProtocolStack
     * 
     * @return the Session of the ProtocolStack
     */
    public Session getSession() throws ProtocolException {
        return protocolStack.getSession();
    }
}
