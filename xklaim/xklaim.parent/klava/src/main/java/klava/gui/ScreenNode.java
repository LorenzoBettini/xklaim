/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import javax.swing.JTextArea;

/**
 * A Node embedding a TupleSpaceScreen
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ScreenNode extends GuiNode<TupleSpaceScreen> {

    /**
     * 
     */
    public ScreenNode() {
        super(new TupleSpaceScreen());
    }

    /**
     * @param title
     */
    public ScreenNode(String title) {
        super(new TupleSpaceScreen(title));
    }

    /**
     * @param tupleSpace
     */
    public ScreenNode(TupleSpaceScreen tupleSpace) {
        super(tupleSpace);
    }

    /**
     * @return The javax.swing.JTextArea associated with this node
     */
    public JTextArea getJTextArea() {
        return guiTupleSpace.getJTextArea();
    }
}
