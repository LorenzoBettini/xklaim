/*
 * Created on Mar 14, 2006
 */
package klava.gui;

import javax.swing.JList;

/**
 * A Node embedding a TupleSpaceList
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ListNode extends GuiNode<TupleSpaceList> {

    /**
     * 
     */
    public ListNode() {
        super(new TupleSpaceList());
    }

    /**
     * @param title
     */
    public ListNode(String title) {
        super(new TupleSpaceList(title));
    }
    
    /**
     * @param tupleSpace
     */
    public ListNode(TupleSpaceList tupleSpace) {
        super(tupleSpace);
    }

    /**
     * @see org.mikado.imc.gui.DefaultListPanel#getJList()
     */
    public JList<?> getJList() {
        return guiTupleSpace.getJList();
    }
}
