/*
 * Created on Feb 15, 2005
 */
package org.mikado.imc.protocols.udp;

import java.net.DatagramPacket;
import java.util.concurrent.LinkedBlockingQueue;

import org.mikado.imc.protocols.ProtocolException;

/**
 * A queue for datagram packets. When you try to remove a packet from this
 * queue, if it is empty, you're block until one is available.
 * 
 * The queue can be closed, in that case who's waiting for the queue to contain
 * something will receive an exception.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class DatagramQueue {
    /**
     * The queue for the packets.
     */
    protected LinkedBlockingQueue<DatagramPacket> queue = new LinkedBlockingQueue<DatagramPacket>();

    /**
     * This will be used to notify that the socket is closed.
     */
    protected final DatagramPacket End;

    /**
     * The identifier associated to this queue.
     */
    protected String id;

    /**
     * Constructs a datagram queue.
     */
    public DatagramQueue(String id) {
        this.id = id;
        End = new DatagramPacket(new byte[1], 1);
    }

    /**
     * Inserts a DatagramPacket into the queue.
     * 
     * @param datagramPacket
     * @throws InterruptedException
     */
    public void insert(DatagramPacket datagramPacket)
            throws InterruptedException {
        queue.put(datagramPacket);
    }

    /**
     * Removes a DatagramPacket from the queue. If the queue is empty this
     * operation blocks. If the queue contains the End DatagramPacket then it
     * throws a ProtocolException, to notify that the queue is "closed".
     * 
     * @return the removed DatagramPacket
     * @throws ProtocolException
     *             If the queue is closed or if we receive an
     *             InterruptedException.
     */
    public DatagramPacket remove() throws ProtocolException {
        try {
            DatagramPacket datagramPacket = queue.take();
            if (datagramPacket == End) {
                throw new ProtocolException("datagram queue closed "
                        + (id != null ? id : ""));
            }
            return datagramPacket;
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Whether the queue is empty.
     * 
     * @return Whether the queue is empty
     */
    public boolean empty() {
        return queue.isEmpty();
    }

    /**
     * Clears the queue and "closes" it (i.e., it inserts the End
     * DatagramPacket)
     */
    public void close() {
        queue.clear();
        queue.offer(End);
    }

    /**
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * @see java.util.concurrent.LinkedBlockingQueue#clear()
     */
    public void clear() {
        queue.clear();
    }
}
