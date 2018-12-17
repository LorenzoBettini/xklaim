/*
 * Created on Jan 26, 2005
 *
 */
package org.mikado.imc.protocols;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * An IP session identifier, i.e., IP:port
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.9 $
 */
public class IpSessionId extends SessionId {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** The string used for separating an IP from the port */
    private static final String ADDRESS_SEPARATOR = ":";

    /** The host */
    protected String host;

    /** The port */
    protected int port;

    /**
     * Copy-constructs an ip session identifier.
     * 
     * @param ipSessionId
     */
    public IpSessionId(IpSessionId ipSessionId) {
        super(ipSessionId);
        this.host = ipSessionId.host;
        this.port = ipSessionId.port;
    }

    /**
     * Constructs an ip session identifier. The wildcard address is used for
     * host.
     * 
     * @param port
     */
    public IpSessionId(int port) {
        this.host = "0.0.0.0";
        this.port = port;
    }

    /**
     * Constructs an ip session identifier. The wildcard address is used for
     * host.
     * 
     * @param port
     * @param connectionProtocolId
     *            the connection layer identifier as a string
     */
    public IpSessionId(int port, String connectionProtocolId) {
        super(connectionProtocolId);
        this.host = "0.0.0.0";
        this.port = port;
        checkConnectionProtocolId(connectionProtocolId);
    }

    /**
     * Constructs an ip session identifier
     * 
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public IpSessionId(String host, int port) throws UnknownHostException {
        setHost(host);
        this.port = port;
    }

    /**
     * Constructs an ip session identifier
     * 
     * @param host
     * @param port
     * @param connectionProtocolId
     *            the connection layer identifier as a string
     * @throws UnknownHostException
     */
    public IpSessionId(String host, int port, String connectionProtocolId)
            throws UnknownHostException {
        super(connectionProtocolId);
        setHost(host);
        this.port = port;
        checkConnectionProtocolId(connectionProtocolId);
    }

    /**
     * Returns true if the string representation of the passed object is the
     * same of the string representation of this session identifier.
     * 
     * @param o
     *            The object to compare to.
     * 
     * @return true if the string representation of the passed object is the
     * same of the string representation of this session identifier.
     */
    public boolean sameId(Object o) {
        return toString().equals(o.toString());
    }

    /**
     * Returns the host.
     * 
     * @return Returns the host.
     */
    public final String getHost() {
        return host;
    }

    /**
     * Returns the port.
     * 
     * @return Returns the port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Given a string in the shape of IP:port returns an IpSessionId represting
     * the string. The string may also contain the connection protocol
     * identifier terminated with a "-", i.e., a string such as "tcp-IP:port" is
     * valid.
     * 
     * If the port is not specified, then set it to 0.
     * 
     * @param str
     *            The string to parse
     * 
     * @return an IpSessionId representing the passed string
     * @throws SessionIdException
     *             if there's a problem in parsing the passed string
     */
    public static IpSessionId parseSessionId(String str)
            throws SessionIdException {
        String id = str;

        int proto_index = str.indexOf(PROTO_SEPARATOR);
        if (proto_index == -1) {
            proto_index = 0;
        } else {
            id = str.substring(proto_index + 1);
        }

        String connectionProtocolId = str.substring(0, proto_index);

        int index = id.indexOf(ADDRESS_SEPARATOR);

        String host = "";
        int port = 0;

        if (index >= 0) {
            host = id.substring(0, index);
            try {
                port = Integer.parseInt(id.substring(index + 1));
                if (port < 0)
                    port = 0;
            } catch (NumberFormatException e1) {
                throw new SessionIdException(e1);
            }
        } else {
            host = id;
        }

        try {
            return new IpSessionId(
                    InetAddress.getByName(host).getHostAddress(), port,
                    connectionProtocolId);
        } catch (Exception e) {
            throw new SessionIdException(e);
        }
    }

    /**
     * Given a string in the shape of IP:port returns a socket address.
     * 
     * If ":" is not present, it is assumed to represent the host (and for port
     * we assume it as 0, i.e., ask the system for an ephemeral port upon
     * binding). The same holds if after ":" nothing is specified (e.g., "host"
     * and "host:" both specify only the host and no port).
     * 
     * If you need to specify only the port number, and no host, you have to use
     * the ":" before the port anyway, e.g., ":9999" specifies only the port
     * number and the wildcard host address "0.0.0.0".
     * 
     * The string may also contain the connection protocol identifier terminated
     * with a "-", i.e., a string such as "tcp-IP:port" is valid.
     * 
     * @param str
     *            The string to parse
     * 
     * @return The socket address.
     * 
     * @throws UnknownHostException
     */
    public static InetSocketAddress parseAddress(String str)
            throws UnknownHostException {
        int proto_index = str.indexOf(PROTO_SEPARATOR);
        if (proto_index != -1) {
            return parseAddress(str.substring(proto_index + 1));
        }

        int index = str.indexOf(ADDRESS_SEPARATOR);
        String host = "";
        int port = 0; // 0 means let the system pick up an ephemeral port

        if (index != -1) {
            host = str.substring(0, index);

            if ((index + 1) < str.length())
                port = Integer.parseInt(str.substring(index + 1));
            /* otherwise port is empty */
        } else {
            /* only the host was specified */
            host = str;
        }

        if (host.length() > 0) {
            return new InetSocketAddress(InetAddress.getByName(host)
                    .getHostAddress(), port);
        } else {
            return new InetSocketAddress(port);
        }
    }

    /**
     * Just like parseAddress, but if the wild card host "0.0.0.0" is obtained,
     * then it assumes the localhost address "127.0.0.1". Thus, for instance,
     * ":9999" generates the address "127.0.0.1:9999".
     * 
     * This is useful when this address is used for connecting (where the
     * wildcard address should not be used).
     * 
     * @param str
     * @return the InetSocketAddress
     * @throws UnknownHostException
     */
    public static InetSocketAddress parseAddressForConnect(String str)
            throws UnknownHostException {
        InetSocketAddress inetSocketAddress = parseAddress(str);

        if (inetSocketAddress.getAddress().isAnyLocalAddress())
            inetSocketAddress = new InetSocketAddress("127.0.0.1",
                    inetSocketAddress.getPort());

        return inetSocketAddress;
    }

    /**
     * Returns the IP:port in the shape of a string.
     * 
     * @return the string representation
     */
    public String toString() {
        return getConnectionProtocolId() + PROTO_SEPARATOR + getHost()
                + ADDRESS_SEPARATOR + getPort();
    }

    /**
     * Creates an InetSocketAddress starting from this IpSessionId.
     * 
     * @return the created InetSocketAddress
     * @throws UnknownHostException
     */
    public InetSocketAddress getSocketAddress() throws UnknownHostException {
        if (host.length() > 0) {
            return new InetSocketAddress(InetAddress.getByName(host)
                    .getHostAddress(), port);
        } else {
            return new InetSocketAddress(port);
        }
    }

    /**
     * Sets the host address (always in the shape of a numeric address).
     * 
     * @param host
     *            The host to set.
     * @throws UnknownHostException
     */
    public void setHost(String host) throws UnknownHostException {
        this.host = InetAddress.getByName(host).getHostAddress();
    }

    /**
     * Ensures that the connectionProtocolId is not empty. In case, set it to
     * "tcp"
     * 
     * @param connectionProtocolId
     *            The connectionProtocolId to set.
     */
    public void checkConnectionProtocolId(String connectionProtocolId) {
        if (connectionProtocolId.length() == 0)
            this.connectionProtocolId = "tcp";
        else
            this.connectionProtocolId = connectionProtocolId;
    }
}
