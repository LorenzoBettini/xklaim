/*
 * Created on Feb 15, 2005
 */
package org.mikado.imc.protocols.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;

/**
 * Continuously Reads packets from a DatagramSocket and dispatch them according
 * to the sender address. The reading stops when we perform "close" on this
 * object.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class DatagramDispatcher extends Thread {
    /**
     * Associates a sender address with a datagram queue.
     */
    protected HashMap<String, DatagramQueue> queues = new HashMap<String, DatagramQueue>();

    /**
     * Datagram packets coming from addresses not yet associated.
     */
    protected DatagramQueue news = new DatagramQueue("news");

    /**
     * addresses not yet associated.
     */
    Set<String> senders = Collections.synchronizedSet(new HashSet<String>());

    /**
     * The DatagramSocket used to read packets.
     */
    protected DatagramSocket datagramSocket;

    /**
     * The SessionId associated with this dispatcher
     */
    protected SessionId sessionId;

    /**
     * The table we belong to
     */
    protected DatagramDispatcherTable datagramDispatcherTable;

    /**
     * Whether accepting is stopped.
     */
    private boolean notAccepting = false;

    /**
     * Creates a DatagramDispatcher with an associated DatagramSocket.
     * 
     * @param datagramSocket
     */
    public DatagramDispatcher(DatagramSocket datagramSocket,
            SessionId sessionId, DatagramDispatcherTable datagramDispatcherTable) {
        this.datagramSocket = datagramSocket;
        this.sessionId = sessionId;
        this.datagramDispatcherTable = datagramDispatcherTable;
    }

    /**
     * Reads and returns a packet from the socket.
     * 
     * @return the packet read from the socket
     * @throws IOException
     */
    protected DatagramPacket retrieve() throws IOException {
        // TODO: generalize the size
        byte[] packet = new byte[1000];
        DatagramPacket datagramPacket = new DatagramPacket(packet,
                packet.length);
        datagramSocket.receive(datagramPacket);
        return datagramPacket;
    }

    /**
     * Builds a string key from the DatagramPacket. The key is built by using
     * the address and port of the sender of this packet.
     * 
     * @param datagramPacket
     * @return the key string
     */
    public static String buildKey(DatagramPacket datagramPacket) {
        return datagramPacket.getAddress().getHostAddress() + ":"
                + datagramPacket.getPort();
    }

    /**
     * Reads packets from the socket and inserts them in the correct queue
     * according to the sender. If the sender is not known yet, then inserts it
     * in the queue of new senders, that will be used by the method accept. The
     * method returns when someone else calss the method close on this object.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (true) {
                DatagramPacket datagramPacket = retrieve();
                String key = buildKey(datagramPacket);
                // System.err.println("Dispatcher: read packet from " + key);
                synchronized (this) {
                    DatagramQueue datagramQueue = queues.get(key);
                    if (datagramQueue == null) {
                        if (!senders.contains(key)) {
                            if (!notAccepting) {
                                senders.add(key);
                                news.insert(datagramPacket);
                                notifyAll();
                            }
                        }
                    } else {
                        byte data[] = datagramPacket.getData();
                        if (datagramPacket.getLength() == 1 && data[0] == 0) {
                            /*
                             * it's another connection packet for someone that's
                             * been already accepted (this may be due to the
                             * fact that the sender didn't get a response in
                             * time and tried to send another connection packet
                             */
                            System.err.println("discarding empty packet from "
                                    + key);
                            // TODO: find a better way
                        } else {
                            datagramQueue.insert(datagramPacket);
                        }
                    }
                }
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
            try {
                close();
            } catch (ProtocolException e1) {
            }
        }
    }

    /**
     * Waits for a packet coming from a yet not known sender, and returns the
     * queue associated with the new sender.
     * 
     * @return the DatagramQueue corresponding to the session
     * @throws ProtocolException
     */
    synchronized public DatagramQueue accept() throws ProtocolException {
        if (notAccepting)
            throw new ProtocolException("datagram socket closed");

        try {
            DatagramPacket datagramPacket = null;
            while (news.empty())
                wait();
            datagramPacket = news.remove();
            String key = buildKey(datagramPacket);
            senders.remove(key);
            DatagramQueue datagramQueue = new DatagramQueue(key);
            queues.put(key, datagramQueue);
            datagramQueue.insert(datagramPacket);
            return datagramQueue;
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Sends a packet through the DatagramSocket.
     * 
     * @param datagramPacket
     * @throws ProtocolException
     */
    public void write(DatagramPacket datagramPacket) throws ProtocolException {
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * The connection that corresponds to the specified id is considered closed.
     * If new connections are not accepted, and there are no more open
     * connections, then this dispatched is closed.
     * 
     * @param id
     * @throws ProtocolException
     */
    synchronized public void deregister(String id) throws ProtocolException {
        if (id == null)
            throw new ProtocolException("null queue identifier");

        queues.remove(id);
        System.err.println("removed queue " + id);
        if (notAccepting && queues.isEmpty())
            close();
    }

    /**
     * Closes the DatagramSocket and closes all the queues (including the queue
     * with yet unknown senders). This will make all those waiting for packet
     * (or for new senders) to receive a ProtocolException.
     * 
     * @throws ProtocolException
     */
    synchronized public void close() throws ProtocolException {
        closeSocket();

        clearNews();

        Iterator<DatagramQueue> iterator = queues.values().iterator();
        while (iterator.hasNext()) {
            DatagramQueue datagramQueue = iterator.next();
            datagramQueue.close();
        }
        
        queues.clear();
    }

    /**
     * Closes the DatagramSocket and deregister this DatagramDispatcher from the
     * DatagramDispatcherTable
     */
    synchronized private void closeSocket() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();

            /*
             * since we closed the socket we must also deregister ourselves
             * since we won't be used anymore
             */
            datagramDispatcherTable.removeDispatcher(this);
        }
    }

    /**
     * Stops accepting new connection requests. However, if there are existing
     * connections, the DatagramSocket is not closed, so the already existing
     * connections are still ip.
     * 
     * @throws ProtocolException
     */
    synchronized public void stopAccept() throws ProtocolException {
        notAccepting = true;
        if (queues.isEmpty()) {
            close();
        } else {
            clearNews();
        }
    }

    /**
     * Starts accepting new connection requests.
     * 
     * @throws ProtocolException
     */
    synchronized public void startAccept() throws ProtocolException {
        notAccepting = false;

        /* make sure the queue is ready for new requests */
        news.clear();
        senders.clear();
    }

    /**
     * Clears the list used for accepting connections, and inserts the End tag.
     * 
     * @throws ProtocolException
     */
    synchronized private void clearNews() throws ProtocolException {
        /*
         * we must make sure that all pending connection requests are removed
         */
        news.close();
        senders.clear();
        
        /* those waiting on the news */
        notifyAll();
    }

    /**
     * Returns the port on which we are listening.
     * 
     * @return the port on which we are listening.
     */
    public int getLocalPort() {
        return datagramSocket.getLocalPort();
    }

    /**
     * Returns the datagram socket.
     * 
     * @return Returns the datagramSocket.
     */
    public final DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    /**
     * @return Returns the sessionId.
     */
    SessionId getSessionId() {
        return sessionId;
    }

    /**
     * @return Returns the notAccepting.
     */
    synchronized boolean isNotAccepting() {
        return notAccepting;
    }
}
