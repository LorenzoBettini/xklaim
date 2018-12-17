package klava;

/**
 * Some utilities for network addresses
 */

import java.net.*;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.SessionIdException;

public class NetUtils {
    /**
     * if set use localhost address even when connected to Internet
     */
    static boolean use_localhost_address = false;

    /**
     * if it's a local host then try to obtain the real IP address
     */
    public static String getNodeIPAddress(InetAddress address) {
        String addr = address.getHostAddress();
        if (addr.equals("127.0.0.1") && !use_localhost_address)
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
            }

        addr = address.getHostAddress();
        int index = addr.indexOf('/');
        if (index > -1)
            addr = addr.substring(index + 1);

        return addr;
    }

    /**
     * if it's a local host then try to obtain the real IP address
     */
    public static String getNodeIPAddress(String addr) {
        if (addr.equals("127.0.0.1") && !use_localhost_address)
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
            }

        return addr;
    }

    public static String getLocalIPAddress() {
        if (use_localhost_address)
            return "127.0.0.1";

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }

        return null;
    }

    /*
     * return an address of the form IP:port
     */
    public static String createNodeAddress(InetAddress address, int port) {
        return createNodeAddress(getNodeIPAddress(address), port);
    }

    /*
     * return an address of the form IP:port
     */
    public static String createNodeAddress(String address, int port) {
        try {
            return getNodeIPAddress(InetAddress.getByName(address)) + ":"
                    + port;
        } catch (UnknownHostException ue) {
            ue.printStackTrace();
            return address + ":" + port;
        }
    }

    /*
     * given an address:port string return an address of the form IP:port
     */
    public static String createNodeAddress(String address) throws KlavaMalformedPhyLocalityException {
        return createNodeAddress(retrieveHostFromLocality(address),
                retrieveConnectionPortFromLocality(address));
    }

    /*
     * return an address of the form IP:port using the local address
     */
    public static String createLocalNodeAddress(int port) {
        return createNodeAddress(getLocalIPAddress(), port);
    }

    /*
     * return a physical locality of the form IP:port using the local address
     */
    public static PhysicalLocality createLocalPhyLocAddress(int port) {
        try {
            return new PhysicalLocality(createLocalNodeAddress(port));
        } catch (KlavaMalformedPhyLocalityException e) {
            e.printStackTrace(); // should never come here
            return null;
        }
    }

    /*
     * if it's a local host then try to obtain the real IP address
     */
    public static InetAddress getNodeAddress(InetAddress address) {
        String addr = address.getHostAddress();
        if (addr.equals("127.0.0.1") && !use_localhost_address)
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
            }

        return address;
    }

    public static String retrieveHostFromLocality(PhysicalLocality loc) throws KlavaMalformedPhyLocalityException {
        return retrieveHostFromLocality(loc.toString());
    }

    public static int retrieveConnectionPortFromLocality(PhysicalLocality loc) {
        return retrieveConnectionPortFromLocality(loc.toString());
    }

    public static String retrieveHostFromLocality(String locality) throws KlavaMalformedPhyLocalityException {
        IpSessionId ipSessionId;
        try {
            ipSessionId = IpSessionId.parseSessionId(locality);
        } catch (SessionIdException e) {
            throw new KlavaMalformedPhyLocalityException(locality);
        }

        return ipSessionId.getHost();
    }

    public static int retrieveConnectionPortFromLocality(String locality) {
        int index = locality.lastIndexOf(':');

        if (index == -1)
            return -1;

        return Integer.parseInt(locality.substring(index + 1));
    }

    public static String canonizeLocality(String l) throws KlavaMalformedPhyLocalityException {
        return retrieveHostFromLocality(l) + "_"
                + retrieveConnectionPortFromLocality(l);
    }

    public static String canonizeLocality(Locality locality) throws KlavaMalformedPhyLocalityException {
        return canonizeLocality(locality + "");
    }

    // build a physical locality even if the string is only a port number:
    // in this case it builds a local physical locality with that port
    public static PhysicalLocality buildMyPhyLoc(String loc)
            throws KlavaMalformedPhyLocalityException {
        int port = retrieveConnectionPortFromLocality(loc);
        if (port != -1)
            return new PhysicalLocality(loc);
        else {
            // OK assume only the port number was specified and build a
            // physical locality with the local address
            try {
                port = Integer.parseInt(loc);
                return NetUtils.createLocalPhyLocAddress(port);
            } catch (NumberFormatException ne) {
                ne.printStackTrace();
            }
        }

        // if we here just build the physical locality and leave the
        // possible exception to the caller
        return new PhysicalLocality(loc);
    }
}