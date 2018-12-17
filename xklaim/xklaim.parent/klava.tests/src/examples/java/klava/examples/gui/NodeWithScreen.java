/*
 * Created on Mar 29, 2006
 */
package klava.examples.gui;

import org.mikado.imc.gui.CloseableFrame;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.gui.ScreenNode;
import klava.topology.KlavaNode;

/**
 * A node embedding a ScreenNode
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class NodeWithScreen extends KlavaNode {
    public static final LogicalLocality screenLoc = new LogicalLocality("screen");

    /**
     * Constructs the node and the embedded screen (whose physical
     * locality is associated with the logical locality "screen"). 
     * 
     * @throws KlavaException
     * 
     */
    public NodeWithScreen(String nodeName) throws KlavaException {
        setNodeName(nodeName);
        ScreenNode screenNode = new ScreenNode(nodeName + "'s screen");
        PhysicalLocality screenPhyLoc = newloc(screenNode);
        addToEnvironment(screenLoc, screenPhyLoc);
        CloseableFrame nodeFrame = new CloseableFrame(this);
        nodeFrame.setTitle(nodeName);
        nodeFrame.add(screenNode.getPanel());
        nodeFrame.setVisible(true);
    }
    
    public static void main(String args[]) throws Exception {
        new NodeWithScreen("NodeWithScreen");
    }
}
