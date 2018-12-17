/*
 * Created on Jan 29, 2005
 */
package org.mikado.imc.topology;

import java.io.IOException;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;


/**
 * This states only reads a string line, inteprets it as an identifier of
 * someone else waiting for a notification, and wakes it up.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class NotifyProtocolState extends ProtocolStateSimple {
    /**
     * The structure containing those who are waiting.
     */
    protected WaitingForNotification waitingForNotification;

    /**
     * Creates a new NotifyProtocolState object.
     *
     * @param waitingForNotification 
     */
    public NotifyProtocolState(WaitingForNotification waitingForNotification) {
        this.waitingForNotification = waitingForNotification;
    }

    /**
     * Creates a new NotifyProtocolState object.
     *
     * @param next_state 
     * @param waitingForNotification The structure containing those who are waiting.
     */
    public NotifyProtocolState(String next_state,
        WaitingForNotification waitingForNotification) {
        super(next_state);
        this.waitingForNotification = waitingForNotification;
    }

    /**
     * Reads a string line, inteprets it as an identifier of
     * someone else waiting for a notification, and wakes it up.
     * If an exception is raised during this operation, it
     * notifies the exception to all the waiting objects.
     * 
     * @param param 
     *
     * @throws ProtocolException 
     */
    public void enter(Object param, TransmissionChannel transmissionChannel) throws ProtocolException {
    	try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
    		String id = unMarshaler.readStringLine();
    		waitingForNotification.wakeUp(id, unMarshaler);
    	} catch (ProtocolException pe) {
    		waitingForNotification.notifyException(pe);
    		throw pe;
    	} catch (IOException e) {
    		ProtocolException protocolException = new ProtocolException(e);
    		waitingForNotification.notifyException(protocolException);
    		throw protocolException;
    	}
    }
}
