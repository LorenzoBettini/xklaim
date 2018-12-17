/*
 * Created on Feb 22, 2006
 */
package org.mikado.imc.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel with a JList (already set with a DefaultListModel
 * into a scrollable pane).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class DefaultListPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JList jList = null;
    
    private DefaultListModel listModel = new DefaultListModel();
    
    private JScrollPane jScrollPane = null;
    
    public DefaultListPanel() {
        initialize();
    }
    
    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jList    
     *  
     * @return javax.swing.JList    
     */
    public JList getJList() {
        if (jList == null) {
            jList = new JList();
            jList.setModel(listModel);
        }
        return jList;
    }
    
    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * @see javax.swing.JList#getSelectedValues()
     */
    public Object[] getSelectedValues() {
        return jList.getSelectedValues();
    }

    /**
     * @see javax.swing.DefaultListModel#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        listModel.add(index, element);
    }

    /**
     * @see javax.swing.DefaultListModel#addElement(java.lang.Object)
     */
    public void addElement(Object obj) {
        listModel.addElement(obj);
    }

    /**
     * @see javax.swing.DefaultListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return listModel.getElementAt(index);
    }

    /**
     * @see javax.swing.DefaultListModel#removeAllElements()
     */
    public void removeAllElements() {
        listModel.removeAllElements();
    }

    /**
     * @see javax.swing.DefaultListModel#removeElementAt(int)
     */
    public void removeElementAt(int index) {
        listModel.removeElementAt(index);
    }

    /**
     * @see javax.swing.DefaultListModel#elements()
     */
    public Enumeration<?> elements() {
        return listModel.elements();
    }

    /**
     * @see javax.swing.JList#getSelectedIndex()
     */
    public int getSelectedIndex() {
        return jList.getSelectedIndex();
    }

    /**
     * @see javax.swing.JList#getSelectedIndices()
     */
    public int[] getSelectedIndices() {
        return jList.getSelectedIndices();
    }

    /**
     * @see javax.swing.JList#getSelectedValue()
     */
    public Object getSelectedValue() {
        return jList.getSelectedValue();
    }

    /**
     * @see javax.swing.DefaultListModel#contains(java.lang.Object)
     */
    public boolean contains(Object elem) {
        return listModel.contains(elem);
    }

    /**
     * @see javax.swing.DefaultListModel#removeElement(java.lang.Object)
     */
    public boolean removeElement(Object obj) {
        return listModel.removeElement(obj);
    }

    /**
     * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
     */
    public void addMouseListener(MouseListener l) {
        jList.addMouseListener(l);
    }

    /**
     * @see java.awt.Component#removeMouseListener(java.awt.event.MouseListener)
     */
    public void removeMouseListener(MouseListener l) {
        jList.removeMouseListener(l);
    }

    /**
     * @see javax.swing.JList#setSelectedIndices(int[])
     */
    public void setSelectedIndices(int[] indices) {
        jList.setSelectedIndices(indices);
    }

}
