/*
 * Created on Apr 14, 2005
 */
package org.mikado.imc.protocols.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.mikado.imc.protocols.AlreadyBoundSessionStarterException;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionStarter;

/**
 * Deals with sessions over UDP packets. Nothing is added to the sent UDP
 * packets.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class UdpSessionStarterPure extends SessionStarter {

    /**
     * The wrapped udp socket.
     */
    protected DatagramSocket datagramSocket;

    /**
     * The dispatcher we use in case of server mode.
     */
    protected DatagramDispatcher datagramDispatcher;

    /**
     * The global table that associates a SessionId to a DatagramDispatcher.
     */
    protected static DatagramDispatcherTable datagramDispatcherTable = new DatagramDispatcherTable();

    /**
     * Constructs an unbound UdpSessionStarter. This will have to be bound with
     * a bind method before being used.
     */
    public UdpSessionStarterPure() {

    }

    /**
     * @param localSessionId
     * @param remoteSessionId
     */
    public UdpSessionStarterPure(SessionId localSessionId,
            SessionId remoteSessionId) {
        super(localSessionId, remoteSessionId);
    }

    /**
     * Constructs a udp session starter with a specific datagram dispatcher.
     * 
     * @param datagramDispatcher
     */
    public UdpSessionStarterPure(DatagramDispatcher datagramDispatcher) {
        this.datagramDispatcher = datagramDispatcher;
    }

    /**
     * Constructs a udp session starter with a specific datagram socket.
     * 
     * @param datagramSocket
     */
    public UdpSessionStarterPure(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#accept()
     */
    public Session accept() throws ProtocolException {
        if (usedForConnect) {
            if (datagramDispatcher != null) {
                throw new ProtocolException("already bound to port "
                        + datagramDispatcher.getLocalPort());
            } else {
                throw new ProtocolException("already used for" + "connect");
            }
        }

        checkLocalSessionId();

        usedForAccept = true;

        if (datagramDispatcher == null) {
            int retry = 5;
            int pause = 1;

            /*
             * The following block, that creates the socket and the
             * DatagramDispatcher is synchronized with the method close.
             * 
             * If a thread enters here and fails to create the DatagramSocket it
             * releases the lock for one second. In the meantime some one else
             * may close this SessionStarter, and this will be noticed by the
             * method check_close that in case will throw an exception.
             * 
             * If the socket creation succeeds and anyone else trying to closing
             * this SessionStarter will be blocked until the DatagramDispatcher
             * is created. Then the close method will simply close the
             * DatagramDispatcher and the accept will fail with an exception.
             */
            synchronized (this) {

                while (true) {
                    checkClosed();

                    try {
                        datagramDispatcher = datagramDispatcherTable
                                .getDispatcherForAccept(getLocalSessionId());
                        break;
                    } catch (ProtocolException e1) {
                        if (retry-- == 0) {
                            throw new ProtocolException(e1);
                        }

                        System.err.println("UDP socket creation failed "
                                + getLocalSessionId() + ", " + e1.getMessage()
                                + ", retry in " + pause + " second(s) - "
                                + Thread.currentThread().getName());

                        try {
                            wait(pause * 1000);
                        } catch (InterruptedException e2) {
                            // simply return
                            throw new ProtocolException(e2);
                        }
                    }
                }

                if (datagramDispatcher == null)
                    throw new ProtocolException("cannot create UDP socket on "
                            + getLocalSessionId());
            }
        }

        /*
         * while we're making the accepting transaction we don't want
         * interruptions
         */
        synchronized (datagramDispatcher) {
            DatagramQueue datagramQueue = datagramDispatcher.accept();
            DatagramPacket datagramPacket = datagramQueue.remove();
            SocketAddress remote = datagramPacket.getSocketAddress();

            datagramDispatcher.write(new DatagramPacket(new byte[1], 1,
                    remote));

            ProtocolLayer udpIpProtocolLayer = createProtocolLayer(
                    datagramQueue, remote);

            InetAddress remoteAddress = datagramPacket.getAddress();

            /*
             * It seems that if the datagramSocket is not connected, then the
             * local address is empty, thus the following: InetAddress
             * localAddress = datagramSocket.getLocalAddress(); will return an
             * empty adress that will then generate a SessionId 0.0.0.0 which is
             * not good. Then we reuse the address of the ipSessionId that we
             * used to listen for incoming packets.
             */
            String localAddress = IpSessionId.parseSessionId(
                    getLocalSessionId().toString()).getHost();

            try {
                Session session = new Session(udpIpProtocolLayer,
                        new IpSessionId(localAddress, datagramDispatcher
                                .getDatagramSocket().getLocalPort(), "udp"),
                        new IpSessionId((remoteAddress != null ? remoteAddress
                                .getHostAddress() : ""), datagramPacket
                                .getPort(), "udp"));

                System.err.println("accepted session: " + session);

                return session;
            } catch (UnknownHostException e) {
                throw new ProtocolException(e);
            }
        }
    }

    /**
     * @param datagramQueue
     * @param remote
     * @return The created ProtocolLayer
     * @throws ProtocolException
     */
    protected ProtocolLayer createProtocolLayer(DatagramQueue datagramQueue,
            SocketAddress remote) throws ProtocolException {
        UdpIpProtocolLayer udpIpProtocolLayer = new UdpIpProtocolLayer(
                datagramDispatcher, datagramQueue, remote);
        return udpIpProtocolLayer;
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#connect()
     */
    public Session connect() throws ProtocolException {
        if (usedForAccept || datagramSocket != null) {
            throw new ProtocolException("already connected with "
                    + datagramSocket.getInetAddress().getHostAddress() + ":"
                    + datagramSocket.getPort());
        }

        checkRemoteSessionId();
        usedForConnect = true;

        InetSocketAddress remoteAddress = null;
        InetSocketAddress localAddress = null;

        try {
            /*
             * uses the parseAddressForConnect variant to avoid the presence of
             * a wildcard address
             */
            remoteAddress = IpSessionId
                    .parseAddressForConnect(getRemoteSessionId().toString());

            if (getLocalSessionId() != null) {
                localAddress = IpSessionId.parseAddress(getLocalSessionId()
                        .toString());
            }
        } catch (UnknownHostException e2) {
            throw new ProtocolException(e2);
        }

        try {
            /*
             * differently from Socket, DatagramSocket default constructor
             * already binds the socket to an ephemeral local address
             */
            if (localAddress == null) {
                datagramSocket = new DatagramSocket();
            } else {
                datagramSocket = new DatagramSocket(localAddress);
            }

            datagramSocket.connect(remoteAddress);

            int retry = 5;
            int pause = 1;
            boolean success = false;
            int timeout = datagramSocket.getSoTimeout();
            datagramSocket.setSoTimeout(1000);

            // send a first packet
            while (retry-- > 0) {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1],
                        1, remoteAddress);
                try {
                    datagramSocket.send(datagramPacket);
                    // wait for a sort of ack within 1 second
                    datagramSocket.receive(datagramPacket);
                    // restore the original timeout
                    datagramSocket.setSoTimeout(timeout);
                    success = true;
                    break;
                } catch (IOException e2) {
                    System.err.println("UDP connection failed "
                            + getRemoteSessionId() + ", retry in " + pause
                            + " second(s) - "
                            + Thread.currentThread().getName());

                    try {
                        Thread.sleep(pause * 1000);
                    } catch (InterruptedException e1) {
                        // simply return
                        throw new ProtocolException(e1);
                    }
                }
            }

            if (!success)
                throw new ProtocolException("no response from "
                        + getRemoteSessionId());

            ProtocolLayer udpIpProtocolLayer = createProtocolLayer();
            InetAddress inetAddress = datagramSocket.getLocalAddress();

            return new Session(udpIpProtocolLayer, new IpSessionId(inetAddress
                    .getHostAddress(), datagramSocket.getLocalPort(), "udp"),
                    new IpSessionId(
                            remoteAddress.getAddress().getHostAddress(),
                            remoteAddress.getPort(), "udp"));
        } catch (SocketException e) {
            throw new ProtocolException(e);
        } catch (UnknownHostException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @return The created ProtocolLayer
     * @throws ProtocolException
     */
    protected ProtocolLayer createProtocolLayer() throws ProtocolException {
        UdpIpProtocolLayer udpIpProtocolLayer = new UdpIpProtocolLayer(
                datagramSocket);
        return udpIpProtocolLayer;
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#doClose()
     */
    @Override
    protected synchronized void doClose() throws ProtocolException {
        if (datagramDispatcher != null) {
            datagramDispatcherTable.releaseDispatcher(datagramDispatcher);
        }
    }

    /**
     * Binds the underlying socket to the specified SessionId.
     * 
     * @see org.mikado.imc.protocols.SessionStarter#bindForAccept(org.mikado.imc.protocols.SessionId)
     */
    @Override
    public SessionId bindForAccept(SessionId sessionId)
            throws ProtocolException {
        if (usedForAccept)
            throw new AlreadyBoundSessionStarterException(getLocalSessionId()
                    .toString());

        usedForAccept = true;

        if (sessionId == null) {
            /*
             * we let the table create a DatagramDispatcher for a non specified
             * SessionId
             */
            datagramDispatcher = datagramDispatcherTable
                    .getDispatcherForAccept(sessionId);
        } else {
            datagramDispatcher = datagramDispatcherTable
                    .bindSessionId(sessionId);
        }

        /*
         * we make sure to set the SessionId of the dispatcher, which may be
         * different from that passed to bindForAccept, e.g., it might have been
         * null or use logical names instead of IP address
         */
        setLocalSessionId(datagramDispatcher.getSessionId());

        return getLocalSessionId();
    }

    /**
     * @return Returns the datagramDispatcherTable.
     */
    public DatagramDispatcherTable getDatagramDispatcherTable() {
        return datagramDispatcherTable;
    }

    /**
     * @param datagramDispatcherTable
     *            The datagramDispatcherTable to set.
     */
    public void setDatagramDispatcherTable(
            DatagramDispatcherTable datagramDispatcherTable) {
        UdpSessionStarterPure.datagramDispatcherTable = datagramDispatcherTable;
    }

    /**
     * @return Returns the datagramDispatcher.
     */
    public DatagramDispatcher getDatagramDispatcher() {
        return datagramDispatcher;
    }
}
