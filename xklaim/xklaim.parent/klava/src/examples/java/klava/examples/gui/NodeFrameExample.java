/*
 * Created on Mar 2, 2006
 */
package klava.examples.gui;

import org.mikado.imc.gui.AcceptEstablishSessionPanel;
import org.mikado.imc.gui.AcceptSessionPanel;
import org.mikado.imc.gui.EstablishSessionPanel;

import klava.gui.NodeFrame;
import klava.topology.KlavaNode;

/**
 * Shows a NodeFrame
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class NodeFrameExample extends NodeFrame {
    /** */
    private static final long serialVersionUID = 1L;

    public NodeFrameExample(KlavaNode node) {
        super(node);
    }

    /**
     * Initiliazes some fields of the AcceptEstablishSessionPanel
     * 
     * @see klava.gui.NodeFrame#createAcceptEstablishSessionPanel()
     */
    @Override
    public AcceptEstablishSessionPanel createAcceptEstablishSessionPanel() {
        AcceptEstablishSessionPanel acceptEstablishSessionPanel = super
                .createAcceptEstablishSessionPanel();

        AcceptSessionPanel acceptSessionPanel = acceptEstablishSessionPanel
                .getAcceptSessionPanel();
        acceptSessionPanel.setProtocolIdText("tcp");
        acceptSessionPanel.setAddressText("localhost:9999");

        EstablishSessionPanel establishSessionPanel = acceptEstablishSessionPanel
                .getEstablishSessionPanel();
        establishSessionPanel.setProtocolIdText("tcp");
        establishSessionPanel.setAddressText("localhost:9999");

        return acceptEstablishSessionPanel;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        NodeFrame nodeFrame = new NodeFrame(new KlavaNode());
        nodeFrame.setSize(500, 400);
        nodeFrame.setVisible(true);
    }

}
