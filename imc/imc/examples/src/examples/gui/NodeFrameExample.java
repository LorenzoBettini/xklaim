/*
 * Created on 10-apr-2006
 */
/**
 * 
 */
package examples.gui;

import org.mikado.imc.gui.AcceptEstablishSessionPanel;
import org.mikado.imc.gui.AcceptSessionPanel;
import org.mikado.imc.gui.EstablishSessionPanel;
import org.mikado.imc.gui.NodeDesktopFrame;
import org.mikado.imc.topology.Node;

import examples.gui.ConnectionMonitor.EchoAcceptProtocolFactory;
import examples.gui.ConnectionMonitor.EchoProtocol;


;

/**
 * Customizes the accept and establish session panels of a NodeFrame.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodeFrameExample extends NodeDesktopFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param node
     */
    public NodeFrameExample(Node node) {
        super(node);
    }

    public NodeFrameExample() {
        this(new Node("Node example"));
    }

    /**
     * Initializes the fields of the AcceptEstablishSessionPanel
     * 
     * @see org.mikado.imc.gui.NodeDesktopFrame#createAcceptEstablishSessionPanel()
     */
    @Override
    public AcceptEstablishSessionPanel createAcceptEstablishSessionPanel() {
        AcceptEstablishSessionPanel acceptEstablishSessionPanel = super
                .createAcceptEstablishSessionPanel();

        AcceptSessionPanel acceptSessionPanel = acceptEstablishSessionPanel
                .getAcceptSessionPanel();
        acceptSessionPanel.setProtocolIdText("tcp");
        acceptSessionPanel.setAddressText("localhost:9999");
        acceptSessionPanel.setProtocolText(EchoAcceptProtocolFactory.class
                .getName());

        EstablishSessionPanel establishSessionPanel = acceptEstablishSessionPanel
                .getEstablishSessionPanel();
        establishSessionPanel.setProtocolIdText("tcp");
        establishSessionPanel.setAddressText("localhost:9999");
        establishSessionPanel.setProtocolText(EchoProtocol.class.getName());

        return acceptEstablishSessionPanel;
    }
    
    public static void main(String args[]) {
        NodeFrameExample nodeFrameExample = new NodeFrameExample();
        nodeFrameExample.setSize(500, 400);
        nodeFrameExample.setVisible(true);
    }
}
