/*
 * Created on Jan 28, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Implements a send-and-wait-for-notification pattern. The send method will
 * send something and wait; the wakeup method will be called by another thread
 * and will read the notification and wakeup the thread that is waiting on the
 * send method.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public abstract class SendAndWaitForNotification {
    /** The identifier for this object. */
    String id;

    /** The structure containing waiting objects. */
    WaitingForNotification waiting;

    /**
     * Possible exception received during the send/receive operation.
     */
    protected ProtocolException protocolException = null;
    
    /**
     * To avoid spurious notifies
     */
    protected boolean wakenUp = false;

    /**
     * Creates a new SendAndWaitForNotification object.
     * 
     * @param id
     *            The identifier for this object
     * @param waiting
     *            The shared structure containing waiting objects
     */
    public SendAndWaitForNotification(String id, WaitingForNotification waiting) {
        this.id = id;
        this.waiting = waiting;
    }

    /**
     * Sends something and then enters the wait state. The actual sending is
     * performed by the abstract method doSend that must be implemented by
     * derived classes.
     * 
     * @throws ProtocolException
     */
    public synchronized final void send() throws ProtocolException {
        /*
         * we have to synchronize on the waiting set, otherwise we might receive
         * a notification before we actually inserted ourselves in the waiting
         * set.
         */
        synchronized (waiting) {
            doSend();
            waiting.goWaiting(this);
        }

        /*
         * The fact that we will not receive a notify before we entered the wait
         * is state is guaranteed by the fact that send and wakeup are
         * synchronized.
         */
        try {
            wakenUp = false;
            
            // to avoid spurious notifies
            while (!wakenUp)
                wait();

            if (protocolException != null) {
                throw protocolException;
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * The actual implementation of the sending. Upon returning from this method
     * the thread will enter the waiting state.
     * 
     * @throws ProtocolException
     */
    protected abstract void doSend() throws ProtocolException;

    /**
     * The method called from another thread to perform the reading of
     * notification. This method will call the abstract method
     * doReceiveNotification and when this returns it will wake up the thread
     * that was waiting on the send method.
     * 
     * @param unMarshaler
     *            The unmarshaler to receive the notification
     * 
     * @throws ProtocolException
     */
    public synchronized final void wakeup(UnMarshaler unMarshaler)
            throws ProtocolException {
        doReceiveNotification(unMarshaler);
        wakenUp = true;
        notifyAll();
    }

    /**
     * The method called from another thread to notify the waiting thread that
     * there was an exception.
     * 
     * @param protocolException
     */
    public synchronized final void notifyException(
            ProtocolException protocolException) {
        this.protocolException = protocolException;
        wakenUp = true;
        notifyAll();
    }

    /**
     * <p>
     * The actual implementation of reading the notification. Upon returning
     * from this method the thread that is in the waiting state in the send
     * method will be notified.
     * </p>
     * 
     * <p>
     * Since we are reading a notification, unless the information of contained
     * in the notification are part of a further message, the method
     * createUnMarshaler() should not be called here (since it has already been
     * called by the state that has just notified us).
     * </p>
     * 
     * @param unMarshaler
     *            The unmarshaler to receive the notification
     * 
     * @throws ProtocolException
     */
    protected abstract void doReceiveNotification(UnMarshaler unMarshaler)
            throws ProtocolException;

    /**
     * Returns the id.
     * 
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }
}
