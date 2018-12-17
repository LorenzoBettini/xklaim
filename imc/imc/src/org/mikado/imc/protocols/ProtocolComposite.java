/**
 * created: Jan 19, 2006
 */
package org.mikado.imc.protocols;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventManager;

/**
 * A Protocol that embeds another Protocol instance. This protocol executes its
 * start state, then executes the embedded protocol and then its end state.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ProtocolComposite extends Protocol {
    /**
     * Executes the embedded protocol.
     * 
     * @author Lorenzo Bettini
     * 
     */
    public class InnerState extends ProtocolState {

        /**
         * 
         */
        public InnerState() {
            super(Protocol.END);
        }

        /**
         * Executes the embedded protocol
         * 
         * @see org.mikado.imc.protocols.ProtocolState#enter(java.lang.Object,
         *      org.mikado.imc.protocols.TransmissionChannel)
         */
        @Override
        public void enter(Object param, TransmissionChannel transmissionChannel)
                throws ProtocolException {
            protocol.start();
        }

    }

    protected Protocol protocol;

    /**
     * @param start
     * @param end
     * @throws ProtocolException
     */
    public ProtocolComposite(ProtocolState start, ProtocolState end,
            Protocol protocol) throws ProtocolException {
        super(start, end);
        this.protocol = protocol;
        super.setProtocolStack(protocol.protocolStack);

        /*
         * this protocol will execute the start state, the embedded protocol and
         * then the end state (the next state of InnerState is END).
         */
        setState("NEXT", new InnerState());
        start.setNextState("NEXT");
    }

    /**
     * @param start
     * @throws ProtocolException 
     */
    public ProtocolComposite(ProtocolState start, Protocol protocol) throws ProtocolException {
        super(start);
        this.protocol = protocol;
        super.setProtocolStack(protocol.protocolStack);
        
        /*
         * this protocol will execute the start state, the embedded protocol and
         * then the end state (the next state of InnerState is END).
         */
        setState("NEXT", new InnerState());
        start.setNextState("NEXT");
    }
    
    /**
     * @see org.mikado.imc.protocols.Protocol#accept(org.mikado.imc.protocols.SessionStarter)
     */
    public Session accept(SessionStarter sessionStarter)
            throws ProtocolException {
        Session session = protocol.accept(sessionStarter); 
        super.setProtocolStack(protocol.protocolStack);
        return session;
    }

    /**
     * @see org.mikado.imc.protocols.Protocol#connect(org.mikado.imc.protocols.SessionStarter)
     */
    public Session connect(SessionStarter sessionStarter)
            throws ProtocolException {
        Session session = protocol.connect(sessionStarter);
        super.setProtocolStack(protocol.protocolStack);
        return session;
    }

    /**
     * @see org.mikado.imc.events.EventGeneratorAdapter#generate(java.lang.String,
     *      org.mikado.imc.events.Event)
     */
    public void generate(String event_id, Event event) {
        protocol.generate(event_id, event);
    }

    /**
     * @see org.mikado.imc.events.EventGeneratorAdapter#getEventManager()
     */
    public EventManager getEventManager() {
        return protocol.getEventManager();
    }

    /**
     * @see org.mikado.imc.protocols.Protocol#setEventManager(org.mikado.imc.events.EventManager)
     */
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);
        protocol.setEventManager(eventManager);
    }

    /**
     * @see org.mikado.imc.protocols.Protocol#getProtocolStack()
     */
    public ProtocolStack getProtocolStack() {
        return protocol.getProtocolStack();
    }

    /**
     * Also sets the stack of the embedded protocol
     * 
     * @see org.mikado.imc.protocols.Protocol#setProtocolStack(org.mikado.imc.protocols.ProtocolStack)
     */
    public void setProtocolStack(ProtocolStack protocolStack) {
        super.setProtocolStack(protocolStack);
        protocol.setProtocolStack(protocolStack);
    }

}
