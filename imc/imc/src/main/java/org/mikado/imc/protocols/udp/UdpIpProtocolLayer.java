/*
 * Created on 14-feb-2005
 */
package org.mikado.imc.protocols.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * This protocol layer wraps a UDP transmission.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class UdpIpProtocolLayer extends ProtocolLayer {
    /**
     * The wrapped udp socket.
     */
    protected DatagramSocket datagramSocket;

    /**
     * The dispatcher we use in case of server mode.
     */
    protected DatagramDispatcher datagramDispatcher;

    /**
     * The queue for incoming packets if in server mode.
     */
    protected DatagramQueue datagramQueue;

    /**
     * The identifier for the queue for incoming packets if in server mode.
     */
    protected String datagramQueueId;

    /**
     * The socket address of the remote client (if in server mode).
     */
    protected SocketAddress remote;

    /**
     * The buffer for writing.
     */
    protected ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Constructs a udp protocol layer with a specific datagram dispatcher.
     * 
     * @param datagramDispatcher
     */
    public UdpIpProtocolLayer(DatagramDispatcher datagramDispatcher) {
        this.datagramDispatcher = datagramDispatcher;
    }

    /**
     * Constructs a UDP ProtocolLayer
     * 
     * @param datagramDispatcher
     * @param datagramQueue
     * @param socketAddress
     * @throws ProtocolException
     */
    public UdpIpProtocolLayer(DatagramDispatcher datagramDispatcher,
            DatagramQueue datagramQueue, SocketAddress socketAddress) {
        this.datagramDispatcher = datagramDispatcher;
        this.datagramQueue = datagramQueue;
        remote = socketAddress;
    }

    /**
     * Constructs a udp protocol layer with a specific datagram socket.
     * 
     * @param datagramSocket
     * @throws ProtocolException
     */
    public UdpIpProtocolLayer(DatagramSocket datagramSocket) {
        super();
        this.datagramSocket = datagramSocket;
        remote = datagramSocket.getRemoteSocketAddress();
    }

    /**
     * Constructs an unbound udp protocol layer
     */
    public UdpIpProtocolLayer() {
    }

    /**
     * Returns a Marshaler that writes into a byte buffer stream, that will be
     * used later by down to build the DatagramPacket.
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateMarshaler(org.mikado.imc.protocols.Marshaler)
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        Marshaler m = new IMCMarshaler(byteArrayOutputStream);
        return m;
    }

    /**
     * Reads a DatagramPacket and returns an UnMarshaler associated to a byte
     * buffer stream (containing the contents of the read packet).
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateUnMarshaler(org.mikado.imc.protocols.UnMarshaler)
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        byte[] packet = null;
        if (datagramQueue != null) {
            DatagramPacket datagramPacket;
            datagramPacket = datagramQueue.remove();
            packet = datagramPacket.getData();
        } else {
            packet = new byte[1000];
            DatagramPacket datagramPacket = new DatagramPacket(packet,
                    packet.length);
            try {
                datagramSocket.receive(datagramPacket);
                System.err.println("UdpLayer: received packet from "
                        + datagramPacket.getSocketAddress());
                // TODO abstract from the specific unmarshaler
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                packet);
        return new IMCUnMarshaler(byteArrayInputStream);
    }

    /**
     * Closes the wrapped datagram socket. If in server mode, notifies the
     * DatagramDispatcher that we are not using the socket any longer.
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doClose()
     */
    public void doClose() throws ProtocolException {
        if (datagramSocket != null) {
            datagramSocket.close();
        }

        if (datagramQueue != null) {
            // the queue can be nill since we didn't manage to
            // perform a successful accept, and some one else
            // closed us.
            datagramDispatcher.deregister(datagramQueue.getId());
            datagramQueue.close();
        }
    }

    /**
     * Sends a DatagramPacket with the contents of the previously initialized
     * byte buffer stream.
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doReleaseMarshaler(org.mikado.imc.protocols.Marshaler)
     */
    public void doReleaseMarshaler(Marshaler marshaler)
            throws ProtocolException {
        byte[] packet = byteArrayOutputStream.toByteArray();
        try {
            DatagramPacket datagramPacket = null;
            datagramPacket = new DatagramPacket(packet, packet.length, remote);

            if (datagramDispatcher != null) {
                datagramDispatcher.write(datagramPacket);
            } else {
                datagramSocket.send(datagramPacket);
            }
        } catch (SocketException e) {
            throw new ProtocolException(e);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @return Returns the datagramSocket.
     */
    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    /**
     * @param datagramSocket
     *            The datagramSocket to set.
     */
    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    /**
     * @return Returns the datagramQueue.
     */
    final DatagramQueue getDatagramQueue() {
        return datagramQueue;
    }

    /**
     * @param datagramQueue
     *            The datagramQueue to set.
     */
    final void setDatagramQueue(DatagramQueue datagramQueue) {
        this.datagramQueue = datagramQueue;
    }

    /**
     * @return Returns the remote.
     */
    final SocketAddress getRemote() {
        return remote;
    }

    /**
     * @param remote
     *            The remote to set.
     */
    final void setRemote(SocketAddress remote) {
        this.remote = remote;
    }
}
