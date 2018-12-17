/*
 * Created on Jan 12, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.events.EventGenerator;
import org.mikado.imc.events.EventGeneratorAdapter;
import org.mikado.imc.events.EventManager;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <p>
 * Represents the protocol as a set of states. The protocol enters a state at a
 * time, and each states, upon exiting, specifies the next state to enter. Each
 * state is identified with a String.
 * </p>
 * 
 * <p>
 * It is important to notice that a protocol is thought to be executed by a
 * single thread: it is not guaranteed that concurrent executions of the same
 * protocol object do not lead to race conditions.
 * </p>
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.11 $
 */
public class Protocol extends EventGeneratorAdapter {
    /** The start state identifier. */
    static public final String START = "START";

    /** The end state identifier. */
    static public final String END = "END";

    /** The set of states. */
    protected Hashtable<String, ProtocolState> states = new Hashtable<String, ProtocolState>();

    /** Used to rename a state. */
    protected int state_counter = 0;

    /** The protocol stack shared by all the states of the protocol. */
    protected ProtocolStack protocolStack = new ProtocolStack();

    /**
     * The current state of the protocol
     */
    protected String current_state = "";

    /**
     * Creates a new Protocol object.
     */
    public Protocol() {
    }

    /**
     * Creates a new Protocol object. You must specify the start and the end
     * state.
     * 
     * @param start
     *            The start state.
     * @param end
     *            The end state.
     */
    public Protocol(ProtocolState start, ProtocolState end) {
        setState0(START, start);
        setState0(END, end);
    }

    /**
     * Creates a new Protocol object. You must specify the start state.
     * 
     * @param start
     *            The start state.
     */
    public Protocol(ProtocolState start) {
        setState0(START, start);
    }

    /**
     * Starts the execution of the protocol from the start state until it
     * reaches the end state. In case an exception is raised during the
     * execution of the protocol states, the protocol terminates with that
     * exception. The passed parameter and the passed transmissionChannel are
     * passed to the start state.
     * 
     * Before exiting this method, even due to an exception, all the states are
     * sent the message "closed".
     * 
     * @param param
     *            The argument to pass to the start state.
     * @param transmissionChannel
     *            The transmissionChannel to pass to the start state.
     * 
     * @throws ProtocolException
     */
    public final void start(Object param,
            TransmissionChannel transmissionChannel) throws ProtocolException {
        current_state = START;
        ProtocolState next_state;

        try {
            do {
                next_state = getState(current_state);

                if (current_state.equals(START)) {
                    next_state.enter(param, transmissionChannel);
                } else {
                    next_state.enter(null, null);
                }

                current_state = next_state.getNextState();
            } while (current_state != END);

            if (containsState(END) != null) {
                next_state = getState(END);
                next_state.enter(null, null);
                current_state = "";
            }
        } finally {
            Enumeration<ProtocolState> allstates = states.elements();

            while (allstates.hasMoreElements()) {
                ProtocolState state = allstates.nextElement();
                state.closed();
            }
        }
    }

    /**
     * Starts the execution of the protocol from the start state until it
     * reaches the end state. In case an exception is raised during the
     * execution of the protocol states, the protocol terminates with that
     * exception.
     * 
     * @throws ProtocolException
     */
    public final void start() throws ProtocolException {
        start(null, null);
    }

    /**
     * Starts the execution of the protocol from the start state until it
     * reaches the end state, passing a transmissionChannel. In case an
     * exception is raised during the execution of the protocol states, the
     * protocol terminates with that exception.
     * 
     * @param transmissionChannel
     * @throws ProtocolException
     */
    public final void start(TransmissionChannel transmissionChannel)
            throws ProtocolException {
        start(null, transmissionChannel);
    }

    /**
     * Closes the protocol by invoking close on the stack.
     * 
     * @throws ProtocolException
     */
    public final void close() throws ProtocolException {
        protocolStack.close();
    }

    /**
     * Returns the state with the specified id. If such a state is not found, it
     * throws a ProtocolException.
     * 
     * @param state_id
     *            The state identifier.
     * 
     * @return The state with the specified identifier.
     * 
     * @throws ProtocolException
     *             If a state with such identifier is not registered.
     */
    public ProtocolState getState(String state_id) throws ProtocolException {
        ProtocolState next_state = states.get(state_id);

        if (next_state == null) {
            throw new ProtocolException("state " + state_id + " undefined");
        }

        return next_state;
    }

    /**
     * Returns the state with the specified id.
     * 
     * @param state_id
     *            The state identifier.
     * 
     * @return The state with the specified identifier or null if none is found.
     */
    public ProtocolState containsState(String state_id) {
        return states.get(state_id);
    }

    /**
     * Sets the state for the specific state identifier.
     * 
     * @param state_id
     *            The state identifier.
     * @param state
     *            The state representing this identifier.
     * 
     * @throws ProtocolException
     *             If a state with this identifier is already registered.
     */
    public void setState(String state_id, ProtocolState state)
            throws ProtocolException {
        if (states.containsKey(state_id)) {
            throw new ProtocolException("already existing state " + state_id);
        }

        setState0(state_id, state);
    }

    /**
     * Sets the state for the specific state identifier. Does not check whether
     * a state was already registered for an id.
     * 
     * @param state_id
     *            The state identifier.
     * @param state
     *            The state representing this identifier.
     */
    protected void setState0(String state_id, ProtocolState state) {
        states.put(state_id, state);
        state.setProtocolStack(protocolStack);
    }

    /**
     * Removes the state associated to the specified id and returns it.
     * 
     * @param state_id
     *            The state id of the state to remove.
     * 
     * @return The state associated to the specified id.
     */
    public ProtocolState removeState(String state_id) {
        return states.remove(state_id);
    }

    /**
     * Updates the low layer of all the states of the protocol by using the
     * specified ProtocolLayer.
     * 
     * @param layer
     * @throws ProtocolException
     */
    public void setLowLayer(ProtocolLayer layer) throws ProtocolException {
        protocolStack.setLowLayer(layer);
    }

    /**
     * Updates the EventManager of all the states that implement the
     * EventGenerator interface.
     * 
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);

        Enumeration<ProtocolState> allstates = states.elements();

        while (allstates.hasMoreElements()) {
            ProtocolState state = allstates.nextElement();

            if (state instanceof EventGenerator) {
                ((EventGenerator) state).setEventManager(eventManager);
            }
        }
    }

    /**
     * @return Returns the protocolStack.
     */
    public ProtocolStack getProtocolStack() {
        return protocolStack;
    }

    /**
     * @param protocolStack
     *            The protocolStack to set.
     */
    public void setProtocolStack(ProtocolStack protocolStack) {
        this.protocolStack = protocolStack;
        Enumeration<ProtocolState> allstates = states.elements();

        while (allstates.hasMoreElements()) {
            ProtocolState state = allstates.nextElement();
            state.setProtocolStack(protocolStack);
        }
    }

    /**
     * Inserts a layer in the protocol stack.
     * 
     * @param layer
     * @throws ProtocolException
     */
    public void insertLayer(ProtocolLayer layer) throws ProtocolException {
        protocolStack.insertLayer(layer);
    }

    /**
     * Accepts a new session using the passed SessionStarter.
     * 
     * @param sessionStarter
     * @return the accepted Session
     * @throws ProtocolException
     */
    public Session accept(SessionStarter sessionStarter)
            throws ProtocolException {
        return protocolStack.accept(sessionStarter);
    }

    /**
     * Establishes a new session using the passed SessionStarter.
     * 
     * @param sessionStarter
     * @return the connected Session
     * @throws ProtocolException
     */
    public Session connect(SessionStarter sessionStarter)
            throws ProtocolException {
        return protocolStack.connect(sessionStarter);
    }

    /**
     * Returns the current state of the protocol.
     * 
     * @return Returns the current_state. If it is an empty string then either
     *         the protocol has not started yet, or it is already finished.
     */
    public final String getCurrentState() {
        return current_state;
    }
}
