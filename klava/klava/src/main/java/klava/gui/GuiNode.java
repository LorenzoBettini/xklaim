/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import javax.swing.JPanel;

import klava.TupleSpace;
import klava.topology.KlavaNode;

/**
 * A Node embedding a graphical Panel (returned by getPanel())
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class GuiNode<TupleSpaceType extends TupleSpace> extends KlavaNode {
    /** The reference to the tuple space as the actual type */
    TupleSpaceType guiTupleSpace;
    
    /**
     * @param tupleSpace
     */
    public GuiNode(TupleSpaceType tupleSpace) {
        super(tupleSpace);
        guiTupleSpace = tupleSpace;
    }

    /**
     * By default it returns the embedded TupleSpace after converting it to a
     * JPanel.
     * 
     * If your panel is not your TupleSpace then you should redefined this
     * method appropriately.
     * 
     * @return The JPanel associated with this node
     */
    public JPanel getPanel() {
        return (JPanel) getTupleSpace();
    }
}
