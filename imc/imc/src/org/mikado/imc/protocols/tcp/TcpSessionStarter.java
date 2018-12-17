/*
 * Created on Apr 14, 2005
 */
package org.mikado.imc.protocols.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.mikado.imc.protocols.AlreadyBoundSessionStarterException;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionIdBindException;
import org.mikado.imc.protocols.SessionStarter;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 * 
 * Deal with sessions over Tcp sockets.
 */
public class TcpSessionStarter extends SessionStarter {
    /**
     * Used for accepting an incoming connection.
     */
    protected ServerSocket serverSocket;

    /**
     * Used for establishing a connection.
     */
    protected Socket socket;

    /**
     * Constructs an unbound TcpSessionStarter. This will have to be bound with
     * a bind method before being used.
     */
    public TcpSessionStarter() {

    }

    /**
     * @param localSessionId
     * @param remoteSessionId
     */
    public TcpSessionStarter(SessionId localSessionId, SessionId remoteSessionId) {
        super(localSessionId, remoteSessionId);
    }

    /**
     * Constructs a session starter for establishing a session.
     * 
     * @param socket
     */
    public TcpSessionStarter(Socket socket) {
        this.socket = socket;
    }

    /**
     * Constructs a session starter for accepting a session.
     * 
     * @param serverSocket
     */
    public TcpSessionStarter(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * 
     * @see org.mikado.imc.protocols.SessionStarter#accept()
     */
    public Session accept() throws ProtocolException {
        /*
         * while we're trying to create the ServerSocket someone else may close
         * this SessionStarter; after this block we must check if closed is true
         * and in that case, we must release the serverSocket.
         */
        synchronized (this) {
            if (serverSocket == null) {
                if (socket != null)
                    throw new ProtocolException(
                            "already used for establishing a session.");

                checkLocalSessionId();

                InetSocketAddress inetSocketAddress = null;

                // first check that the specified id is valid
                try {
                    inetSocketAddress = IpSessionId
                            .parseAddress(getLocalSessionId().toString());
                } catch (UnknownHostException e) {
                    throw new ProtocolException(e);
                } catch (ClassCastException e) {
                    throw new ProtocolException(e);
                }

                int retry = 5;
                int pause = 1;

                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                } catch (IOException e) {
                    throw new ProtocolException("on server socket creation");
                }

                while (retry > 0) {
                    try {
                        serverSocket.bind(inetSocketAddress);
                    } catch (IOException e1) {
                        if (retry-- == 0) {
                            throw new ProtocolException(e1);
                        }

                        System.err.println("TCP server socket creation failed "
                                + getLocalSessionId() + ", retry in " + pause
                                + " second(s) - "
                                + Thread.currentThread().getName());

                        try {
                            Thread.sleep(pause * 1000);
                        } catch (InterruptedException e2) {
                            // simply return
                            throw new ProtocolException(e2);
                        }
                    }

                    if (serverSocket.isBound())
                        break;
                }
            }

            if (isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new ProtocolException(e);
                }
            }
        }

        TcpIpProtocolLayer tcpIpProtocolLayer = null;

        try {
            Socket incoming = serverSocket.accept();
            tcpIpProtocolLayer = new TcpIpProtocolLayer(incoming);

            return new Session(tcpIpProtocolLayer, new IpSessionId(incoming
                    .getLocalAddress().getHostAddress(), incoming
                    .getLocalPort()), new IpSessionId(incoming.getInetAddress()
                    .getHostAddress(), incoming.getPort()));
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#connect()
     */
    public Session connect() throws ProtocolException {
        if (socket == null) {
            if (serverSocket != null)
                throw new ProtocolException(
                        "already used for accepting a session.");

            checkRemoteSessionId();

            InetSocketAddress remoteAddress = null;
            InetSocketAddress localAddress = null;

            try {
                remoteAddress = IpSessionId
                        .parseAddressForConnect(getRemoteSessionId().toString());

                if (getLocalSessionId() != null) {
                    localAddress = IpSessionId.parseAddress(getLocalSessionId()
                            .toString());
                }
            } catch (UnknownHostException e2) {
                throw new ProtocolException(e2);
            }

            int retry = 5;
            int pause = 1;
            boolean success = false;

            while (retry-- > 0) {
                try {

                    /* create an unbound and non connected socket */
                    socket = new Socket();

                    socket.setReuseAddress(true);

                    /*
                     * bind it to a local address (if the address is null, the
                     * system will pick up an ephemeral port and a valid local
                     * address to bind the socket.
                     */
                    socket.bind(localAddress);

                    /* and connect it to the remote end */
                    socket.connect(remoteAddress);

                    success = true;

                    break;
                } catch (UnknownHostException e) {
                    throw new ProtocolException(e);
                } catch (ClassCastException e) {
                    throw new ProtocolException(e);
                } catch (IOException e) {
                    System.err.println("connection failed "
                            + getRemoteSessionId() + ", " + e.getMessage()
                            + ", retry in " + pause + " second(s)- "
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
                throw new ProtocolException("cannot connect to "
                        + getRemoteSessionId());
        }

        TcpIpProtocolLayer tcpIpProtocolLayer = null;

        try {
            tcpIpProtocolLayer = new TcpIpProtocolLayer(socket);

            return new Session(tcpIpProtocolLayer,
                    new IpSessionId(socket.getLocalAddress().getHostAddress(),
                            socket.getLocalPort()), new IpSessionId(socket
                            .getInetAddress().getHostAddress(), socket
                            .getPort()));
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#doClose()
     */
    @Override
    protected synchronized void doClose() throws ProtocolException {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }

    /**
     * Binds the underlying ServerSocket to the specified SessionId (or to a
     * port chosen by the system if the SessionId is null).
     * 
     * @see org.mikado.imc.protocols.SessionStarter#bindForAccept(org.mikado.imc.protocols.SessionId)
     */
    @Override
    public SessionId bindForAccept(SessionId sessionId)
            throws ProtocolException {
        if (getLocalSessionId() != null)
            throw new AlreadyBoundSessionStarterException(getLocalSessionId()
                    .toString());

        try {
            if (sessionId != null) {
                InetSocketAddress inetSocketAddress = IpSessionId
                        .parseAddress(sessionId.toString());

                if (serverSocket == null)
                    serverSocket = new ServerSocket();

                try {
                    serverSocket.bind(inetSocketAddress);
                } catch (BindException e) {
                    throw new SessionIdBindException(sessionId);
                }
            } else {
                if (serverSocket == null)
                    serverSocket = new ServerSocket();

                /* let the system choose an address */
                serverSocket.bind(null);
            }

            setLocalSessionId(new IpSessionId(serverSocket.getInetAddress()
                    .getHostAddress(), serverSocket.getLocalPort()));

            return getLocalSessionId();
        } catch (UnknownHostException e) {
            throw new ProtocolException(e);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

}
