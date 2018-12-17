/*
 * Created on Mar 14, 2006
 */
package klava.examples.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import klava.Tuple;
import klava.gui.TupleSpaceList;
import klava.gui.TupleSpaceScreen;
import klava.gui.TupleSpaceKeyboard;

/**
 * Uses graphical widgets with TupleSpace interfaces
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TupleSpaceExamples extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private TupleSpaceList tupleSpaceList = null;

    private TupleSpaceScreen tupleSpaceScreen = null;

    private TupleSpaceKeyboard tupleSpaceKeyboard = null;

    /**
     * @throws HeadlessException
     */
    public TupleSpaceExamples() throws HeadlessException {
        super();

        initialize();
    }

    /**
     * @param gc
     */
    public TupleSpaceExamples(GraphicsConfiguration gc) {
        super(gc);

        initialize();
    }

    /**
     * @param title
     * @throws HeadlessException
     */
    public TupleSpaceExamples(String title) throws HeadlessException {
        super(title);

        initialize();
    }

    /**
     * @param title
     * @param gc
     */
    public TupleSpaceExamples(String title, GraphicsConfiguration gc) {
        super(title, gc);

        initialize();
    }

    /**
     * This method initializes tupleSpaceList
     * 
     * @return klava.gui.TupleSpaceList
     */
    private TupleSpaceList getTupleSpaceList() {
        if (tupleSpaceList == null) {
            tupleSpaceList = new TupleSpaceList();
            tupleSpaceList.setTitle("list");
            
            tupleSpaceList.out(new Tuple("some data"));
            tupleSpaceList.out(new Tuple("some other data"));
            tupleSpaceList.out(new Tuple("foo"));
            tupleSpaceList.out(new Tuple("bar"));
        }
        return tupleSpaceList;
    }

    /**
     * This method initializes tupleSpaceScreen
     * 
     * @return klava.gui.TupleSpaceScreen
     */
    private TupleSpaceScreen getTupleSpaceScreen() {
        if (tupleSpaceScreen == null) {
            tupleSpaceScreen = new TupleSpaceScreen();
            tupleSpaceScreen.setTitle("screen");

            tupleSpaceScreen.out(new Tuple(
                    "try to type something in the text edit below\n"));
            tupleSpaceScreen.out(new Tuple(
                    "either a string or using the tuple syntax\n"));

        }
        return tupleSpaceScreen;
    }

    /**
     * This method initializes tupleSpaceKeyboard
     * 
     * @return klava.gui.TupleSpaceKeyboard
     */
    private TupleSpaceKeyboard getTupleSpaceKeyboard() {
        if (tupleSpaceKeyboard == null) {
            tupleSpaceKeyboard = new TupleSpaceKeyboard();
            tupleSpaceKeyboard.setTitle("input");
        }
        return tupleSpaceKeyboard;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TupleSpaceExamples tupleSpaceExamples = new TupleSpaceExamples();
        tupleSpaceExamples.setDefaultCloseOperation(EXIT_ON_CLOSE);
        tupleSpaceExamples.pack();
        tupleSpaceExamples.setVisible(true);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(300, 200);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getTupleSpaceList(), java.awt.BorderLayout.EAST);
            jContentPane.add(getTupleSpaceScreen(),
                    java.awt.BorderLayout.CENTER);
            jContentPane.add(getTupleSpaceKeyboard(),
                    java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

}
