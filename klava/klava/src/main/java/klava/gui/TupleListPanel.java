/*
 * Created on Feb 21, 2006
 */
package klava.gui;

import javax.swing.JList;

import klava.Tuple;
import klava.TupleSpace;
import klava.events.TupleEvent;

import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventListener;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.gui.PanelWithTitle;

/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.7 $
 */
public class TupleListPanel extends PanelWithTitle implements EventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private TupleList tupleList;

    /**
     * 
     */
    public TupleListPanel() {

    }

    /**
     * 
     */
    public TupleListPanel(TupleSpace tupleSpace) {
        super("tuple space");
        addTupleListPanel(tupleSpace);
    }

    /**
     * @param title
     */
    public TupleListPanel(String title, TupleSpace tupleSpace) {
        super(title);
        addTupleListPanel(tupleSpace);
    }

    /**
     * @param title
     */
    public TupleListPanel(String title) {
        super(title);
    }

    /**
     * Creates a graphical tuple space list panel and register for tuple events
     * (coming from the passed TupleSpace).
     * 
     * It uses the EventManager of the TupleSpace, provided it is set (otherwise
     * it will not listen for tuple events).
     * 
     * The EventManager can then be updated with the method setEventManager().
     * 
     * @param tupleSpace
     */
    protected void addTupleListPanel(TupleSpace tupleSpace) {
        tupleList = new TupleList(tupleSpace);
        addMainPanel(tupleList);
        EventManager eventManager = tupleSpace.getEventManager();
        if (eventManager != null) {
            setEventManager(eventManager);
        }
    }

    /**
     * @see org.mikado.imc.gui.PanelWithTitle#setEventManager(org.mikado.imc.events.EventManager)
     */
    @Override
    public void setEventManager(EventManager eventManager) {
        super.setEventManager(eventManager);
        
        /* now register for events */
        if (eventManager != null)
            eventManager.addListener(TupleEvent.EventId, this);
    }

    /**
     * @see klava.gui.TupleList#addTuple(klava.Tuple)
     */
    public void addTuple(Tuple tuple) {
        tupleList.addTuple(tuple);
    }

    /**
     * @see klava.gui.TupleList#removeTuple(klava.Tuple)
     */
    public void removeTuple(Tuple tuple) {
        tupleList.removeTuple(tuple);
    }

    /**
     * @see klava.gui.TupleList#getSelectedValues()
     */
    public Object[] getSelectedValues() {
        return tupleList.getSelectedValues();
    }

    /**
     * @see klava.gui.TupleList#notify(org.mikado.imc.events.Event)
     */
    public void notify(Event event) {
        tupleList.notify(event);
    }

    /**
     * @see org.mikado.imc.gui.DefaultListPanel#getSelectedValue()
     */
    public Object getSelectedValue() {
        return tupleList.getSelectedValue();
    }

    /**
     * @see org.mikado.imc.gui.DefaultListPanel#setSelectedIndices(int[])
     */
    public void setSelectedIndices(int[] indices) {
        tupleList.setSelectedIndices(indices);
    }

    /**
     * @see org.mikado.imc.gui.DefaultListPanel#getJList()
     */
    public JList getJList() {
        return tupleList.getJList();
    }
}
