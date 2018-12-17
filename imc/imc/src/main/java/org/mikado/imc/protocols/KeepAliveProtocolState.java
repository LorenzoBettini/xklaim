/*
 * Created on Jan 26, 2005
 *
 */
package org.mikado.imc.protocols;

/**
 * A simple state that only must keep the connection alive.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class KeepAliveProtocolState extends ProtocolStateSimple {
    /**
     * This keep alive state is dead
     */
    private boolean dead = false;

    /**
     * Creates a new KeepAliveProtocolState object.
     */
    public KeepAliveProtocolState() {
        super(Protocol.END);
    }

    /**
     * Creates a new KeepAliveProtocolState object.
     * 
     * @param next_state
     */
    public KeepAliveProtocolState(String next_state) {
        super(next_state);
    }

    /**
     * Simply keeps the connection up.
     * 
     * @param param
     *            ignored
     * 
     * @throws ProtocolException
     */
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        synchronized (this) {
            while (!dead) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Stops the keeping alive.
     */
    public void die() {
        synchronized (this) {
            dead = true;
            notifyAll();
        }
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolState#close()
     */
    public void close() throws ProtocolException {
        die();

        super.close();
    }
}
