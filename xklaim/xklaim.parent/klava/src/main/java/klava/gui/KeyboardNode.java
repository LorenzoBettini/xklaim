/*
 * Created on Mar 14, 2006
 */
package klava.gui;

/**
 * A Node embedding a TupleSpaceKeyboard
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class KeyboardNode extends GuiNode<TupleSpaceKeyboard> {

    /**
     * 
     */
    public KeyboardNode() {
        super(new TupleSpaceKeyboard());
    }

    /**
     * @param title
     */
    public KeyboardNode(String title) {
        super(new TupleSpaceKeyboard(title));
    }
    
    /**
     * @param tupleSpace
     */
    public KeyboardNode(TupleSpaceKeyboard tupleSpace) {
        super(tupleSpace);
    }

}
