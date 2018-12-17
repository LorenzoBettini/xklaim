/**
 * 
 */
package examples.gui;


import org.mikado.imc.gui.NodeDesktopFrame;
import org.mikado.imc.gui.ProcessListPanel;
import org.mikado.imc.gui.SessionListPanel;
import org.mikado.imc.topology.Node;

/**
 * Shows a NodeFrame with some initialized values.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class NodeMonitor {

    /**
     * @param args
     */
    public static void main(String[] args) {
        NodeDesktopFrame nodeFrame = new NodeFrameExample(new Node());
        nodeFrame.addFrame(new SessionListPanel(nodeFrame.getNode().getEventManager()));
        nodeFrame.addFrame(new ProcessListPanel(nodeFrame.getNode().getEventManager()));
        nodeFrame.addFrame(nodeFrame.createAcceptEstablishSessionPanel());
        nodeFrame.setSize(500, 400);
        nodeFrame.setVisible(true);
    }

}
