/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import javax.swing.JButton;

/**
 * A Node embedding a TupleSpaceButton
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ButtonNode extends GuiNode<TupleSpaceButton> {

    /**
     * 
     */
    public ButtonNode() {
        super(new TupleSpaceButton());
    }

    /**
     * @param title
     */
    public ButtonNode(String title) {
        super(new TupleSpaceButton(title));
    }
    
    /**
     * @param tupleSpace
     */
    public ButtonNode(TupleSpaceButton tupleSpace) {
        super(tupleSpace);
    }

    /**
     * @return The JButton associated with this Node.
     */
    public JButton getJButton() {
        return guiTupleSpace.getJButton();
    }
}
