/*
 * Created on 16-feb-2005
 */
package examples.udp;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;

import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.LinePrinterProtocolLayer;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStackThread;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.udp.UdpSessionStarter;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class EchoServerUdpFrame extends JFrame {

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.3 $
     */
    public class LineInterceptor extends ProtocolStackThread {
        UnMarshaler unMarshaler;

        public LineInterceptor(ProtocolStack protocolStack,
                UnMarshaler unMarshaler) throws IOException {
            super(protocolStack);
            this.unMarshaler = unMarshaler;
        }

        public void run() {
            while (true) {
                try {
                    protocolStack.createUnMarshaler();
                    String line = unMarshaler.readStringLine();
                    getJTextArea().append(line + "\n");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private static final long serialVersionUID = 1L;

    protected Vector<LineInterceptor> interceptors = new Vector<LineInterceptor>();

    private javax.swing.JPanel jContentPane = null;

    private JScrollPane jScrollPane = null;

    private JPanel jPanel = null;

    private JButton jButton = null;

    private JTextArea jTextArea = null;

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.add(getJButton(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("Close");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Enumeration<LineInterceptor> en = interceptors.elements();

                    while (en.hasMoreElements()) {
                        LineInterceptor lineInterceptor = en.nextElement();
                        try {
                            lineInterceptor.close();
                            lineInterceptor.join();
                        } catch (ProtocolException e1) {
                            e1.printStackTrace();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }

                    System.exit(0);
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jTextArea
     * 
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
        }
        return jTextArea;
    }

    public static void main(String[] args) throws ProtocolException,
            IOException {
        EchoServerUdpFrame echoServerUdpFrame = new EchoServerUdpFrame();
        echoServerUdpFrame.setVisible(true);
        echoServerUdpFrame.listen();
    }

    /**
     * This is the default constructor
     */
    public EchoServerUdpFrame() {
        super();
        initialize();
    }

    public void listen() throws ProtocolException, IOException {
        UdpSessionStarter udpSessionStarter = new UdpSessionStarter(
                new IpSessionId(9999, "udp"), null);
        while (true) {
            LinePrinterProtocolLayer linePrinterProtocolLayer = new LinePrinterProtocolLayer();
            ProtocolStack protocolStack = new ProtocolStack(
                    linePrinterProtocolLayer);
            Session session = protocolStack.accept(udpSessionStarter);
            linePrinterProtocolLayer.setPreprend(session + " - ");

            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(
                    pipedOutputStream);
            UnMarshaler unMarshaler = new IMCUnMarshaler(pipedInputStream);
            linePrinterProtocolLayer.setOut(pipedOutputStream);

            System.out.println("accepted session: " + session);
            LineInterceptor lineInterceptor = new LineInterceptor(
                    protocolStack, unMarshaler);
            interceptors.addElement(lineInterceptor);
            lineInterceptor.start();
        }
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(650, 299);
        this.setContentPane(getJContentPane());
        this.setTitle("UDP Echo Server");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
