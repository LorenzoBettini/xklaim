/*
 * Created on 16-feb-2005
 */
package examples.udp;

import java.io.IOException;
import java.util.Date;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.udp.UdpSessionStarterPure;

/**
 * Simply sends strings using UDP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class UdpStringSender {
    protected String host = "127.0.0.1";

    protected int port = 9999;

    /**
     * Creates a UdpStringSender
     */
    public UdpStringSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Creates a UdpStringSender
     */
    public UdpStringSender() {
    }

    public void send() throws ProtocolException, IOException,
            InterruptedException {
        Session session = (new UdpSessionStarterPure(null, new IpSessionId(host,
                port, "udp"))).connect();
        ProtocolLayer udpIpProtocolLayer = session.getProtocolLayer();

        while (true) {
            String tosend = session + " - " + new Date();
            System.out.println("sending: " + tosend);
            Marshaler marshaler = udpIpProtocolLayer.doCreateMarshaler(null);
            marshaler.writeStringLine(tosend);
            udpIpProtocolLayer.doReleaseMarshaler(marshaler);
            Thread.sleep(3000);
        }
    }

    public static void main(String[] args) throws ProtocolException,
            IOException, InterruptedException {
        UdpStringSender udpStringSender = null;
        if (args.length > 2) {
            udpStringSender = new UdpStringSender(args[0], Integer
                    .parseInt(args[1]));
        } else {
            udpStringSender = new UdpStringSender();
        }
        udpStringSender.send();
    }
}
