/*
 * Created on Jan 11, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.protocols.SessionId;


/**
 * A server waiting for incoming connections.
 *
 * @author $author$
 * @version $Revision: 1.7 $
 */
public class ConnectionServerThread extends Thread {
    /**
     * The ConnectionServer used for accepting new sessions.
     */
    protected ConnectionServer connectionServer;

    /**
     * If set, the protocol that it will execute (on a separate thread) upon
     * receiving a connetion.
     */
    protected ProtocolFactory protocolFactory;

    /**
     * Constructs a ConnectionServer. The passed protocol factory will be used
     * to create a protocol that manage an incoming connection (each incoming
     * connection is managed through a different protocol through a separate
     * thread).  The protocol protocol will be updated with a new start state
     * (for dealing with connection protocol) and a new end state (for dealing
     * with disconnection protocol).
     *
     * @param sessionManager
     * @param protocolFactory
     * @param sessionId
     *
     * @throws ProtocolException
     */
    public ConnectionServerThread(SessionManager sessionManager,
        ProtocolFactory protocolFactory, SessionId sessionId) throws ProtocolException {
        super("ConnectionServerThread-" + sessionId.toString());
        connectionServer = new ConnectionServer(sessionManager, sessionId);
        this.protocolFactory = protocolFactory;        
    }

    /**
     * Stops listening for incoming connections.
     *
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        connectionServer.close();
    }

    /**
     * The run method that continuosly listens for incoming connections.
     */
    public void run() {
        try {
            while (true) {
                if (protocolFactory != null) {
                    Protocol protocol = protocolFactory.createProtocol();
                    new ProtocolThread(connectionServer.accept(protocol)).start();
                } else {
                    throw new ProtocolException("protocol factory is not set");
                }
            }
        } catch (ProtocolException e1) {
            e1.printStackTrace();

            return;
        }
    }

    /**
     * Returns the protocol factory used to create protocols to execute for
     * incoming connections.
     *
     * @return Returns the protocol factory.
     */
    public final ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    /**
     * Sets the protocol factory used to create protocols to execute for
     * incoming connections.
     *
     * @param protocolFactory The protocol factory to set.
     */
    public final void setProtocolFactory(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    /**
     * Simply starts a ConnectionServer listening on port 9999.
     *
     * @param args
     *
     * @throws ProtocolException
     */
    public static void main(String[] args) throws ProtocolException {
        ConnectionServerThread connectionServer = 
            new ConnectionServerThread(new SessionManager(),
                new ProtocolFactory() {
                public Protocol createProtocol() throws ProtocolException {
                    Protocol protocol = new Protocol();
                    protocol.setState(Protocol.START,
                        new EchoProtocolState());

                    return protocol;
                }
            }, new IpSessionId(9999));
        connectionServer.start();
        System.out.println("connection server started");
    }
}
