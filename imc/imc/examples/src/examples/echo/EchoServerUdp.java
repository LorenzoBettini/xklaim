/*
 * Created on 16-feb-2005
 */
package examples.echo;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.LinePrinterProtocolLayer;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStackThread;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.udp.UdpSessionStarter;

/**
 * Uses a UdpIpProtocolLayer to read packets and write them on the screen.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EchoServerUdp {
    public EchoServerUdp() {
    }

    public void listen() throws ProtocolException {
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(
                new IpSessionId(9999, "udp"), null);
        while (true) {
            LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer();
            ProtocolStack protocolStack = new ProtocolStack(
                    linePrinterProtocolLayer);
            Session session = protocolStack.accept(udpSessionStarter);
            linePrinterProtocolLayer.setPreprend(session + " - ");

            System.out.println("accepted session: " + session);
            new ProtocolStackThread(protocolStack).start();
        }
    }

    public static void main(String args[]) throws Exception {
        EchoServerUdp echoServerUdp = new EchoServerUdp();
        System.out.println("echo server listening...");
        echoServerUdp.listen();
    }
}
