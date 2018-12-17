/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.mikado.imc.events.EventGenerator;
import org.mikado.imc.events.EventManager;

/**
 * A specialized ProtocolState that reads a line, interprets it as a command
 * request, and passes control to the associated ProtocolState. If such a
 * request is not found in the registered request table, it writes back an
 * unknown request error.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.13 $
 */
public class ProtocolSwitchState extends ProtocolState {
    /** The table storing the registered states for each requests. */
    protected Hashtable<String, Object> requests = new Hashtable<String, Object>();

    /** The string sent back to notify a failure (default "FAIL"). */
    protected String unknownRequest = "FAIL: UNKNOWN REQUEST";

    /**
     * Creates a new ProtocolSwitchState object.
     */
    public ProtocolSwitchState() {
    }

    /**
     * Creates a new ProtocolSwitchState object.
     * 
     * @param next_state
     */
    public ProtocolSwitchState(String next_state) {
        super(next_state);
    }

    /**
     * Associates a state to a request. If a state was already registered for
     * the request, it simply overrides the previous state.
     * 
     * @param request
     * @param state
     */
    public void addRequestState(String request, ProtocolState state) {
        requests.put(request, state);
        state.setProtocolStack(getProtocolStack());
    }

    /**
     * Associates a next state to a request. If a next state was already
     * registered for the request, it simply overrides the previous state.
     * 
     * @param request
     * @param next
     */
    public void addRequestState(String request, String next) {
        requests.put(request, next);
    }

    /**
     * Removes a string request association.
     * 
     * @param request
     * 
     * @return The state that was associated to the specified request or the
     *         next state.
     */
    public Object removeRequestState(String request) {
        return requests.remove(request);
    }

    /**
     * Returns the state associated to the specified request.
     * 
     * @param request
     * 
     * @return The state associated to the specified request (or the next state)
     *         or null if no state is associated to the specified request.
     */
    public Object getRequestState(String request) {
        return requests.get(request);
    }

    /**
     * Given a request string line: <br> - if a state is registered for such a
     * request, returns the result of such state enter() (it passes to enter the
     * request string as a parameter) <br> - otherwise writes back an unknown
     * request error, and returns the next state <br>
     * If a parameter is passed, it is simply ignored: it always tries to read a
     * request from the unmarshaler.
     * 
     * @param param
     *            Ignored
     * @param transmissionChannel
     *            The UnMarshaler to read from (if not null)
     * 
     * @throws ProtocolException
     */
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        try {
            String request;

            /* this will be used to pass information to the substates */
            if (transmissionChannel == null)
                transmissionChannel = new TransmissionChannel();
            
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            request = unMarshaler.readStringLine();

            Object associated_state = getRequestState(request);

            if (associated_state == null) {
                Marshaler marshaler = getMarshaler(transmissionChannel);
                marshaler.writeStringLine(unknownRequest + ": " + request);
                releaseMarshaler(marshaler);
            } else if (associated_state instanceof ProtocolState) {
                ProtocolState protocolState = ((ProtocolState) associated_state);
                protocolState.enter(request, transmissionChannel);
                setNextState(protocolState.getNextState());
            } else {
                setNextState(associated_state.toString());
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Updates the EventManager of all the states that implement the
     * EventGenerator interface.
     * 
     * @param eventManager
     */
    @Override
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);

        Enumeration<Object> states = requests.elements();

        while (states.hasMoreElements()) {
            Object next = states.nextElement();

            if (next instanceof EventGenerator) {
                ((EventGenerator) next).setEventManager(eventManager);
            }
        }
    }

    /**
     * Also sets the protocol stack in all the registered states.
     * 
     * @param protocolStack
     */
    public void setProtocolStack(ProtocolStack protocolStack) {
        this.protocolStack = protocolStack;

        Enumeration<Object> states = requests.elements();

        while (states.hasMoreElements()) {
            Object next = states.nextElement();

            if (next instanceof ProtocolState) {
                ((ProtocolState) next).setProtocolStack(protocolStack);
            }
        }
    }

    /**
     * Forwards this message to all the states.
     * 
     * @see org.mikado.imc.protocols.ProtocolState#closed()
     */
    public void closed() throws ProtocolException {
        Enumeration<Object> states = requests.elements();

        while (states.hasMoreElements()) {
            Object next = states.nextElement();

            if (next instanceof ProtocolState) {
                ((ProtocolState) next).closed();
            }
        }
    }

    /**
     * The string sent back in case of an unknown request.
     * 
     * @return Returns the unknownRequest.
     */
    public final String getUnknownRequest() {
        return unknownRequest;
    }

    /**
     * Sets the string to be sent back in case of an unknown request.
     * 
     * @param unknownRequest
     *            The unknownRequest to set.
     */
    public final void setUnknownRequest(String unknownRequest) {
        this.unknownRequest = unknownRequest;
    }
}
