/*
 * Created on Mar 14, 2006
 */
package klava.examples.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import org.mikado.imc.common.IMCException;

import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;
import klava.gui.ButtonNode;
import klava.gui.KeyboardNode;
import klava.gui.ListNode;
import klava.gui.ScreenNode;
import klava.gui.TupleSpaceButton;
import klava.gui.TupleSpaceList;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;

/**
 * Shows some GuiNodes in a frame.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class GuiNodeExample extends JFrame {
    /**
     * Waits for button click events and show the selected items from the list
     * in the screen
     * 
     * @author Lorenzo Bettini
     * @version $Revision $
     */
    public class ButtonProcess extends KlavaProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param name
         */
        public ButtonProcess(String name) {
            super(name);
        }

        public ButtonProcess() {
            
        }

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            Tuple click = new Tuple(TupleSpaceButton.clickedString);
            Tuple requestTuple = new Tuple(TupleSpaceList.cmdString,
                    new KString("getSelectedItems"));

            while (true) {
                /* wait for a click event */
                in(click, button);

                /* request selected items to the list */
                out(requestTuple, list);

                /* retrieve the selected items in the list */
                TupleSpace selected = new TupleSpaceVector();
                Tuple selectedItems = new Tuple(TupleSpaceList.cmdString,
                        new KString("getSelectedItems"),
                        selected);
                in(selectedItems, list);

                /* print the selected items in the screen */
                out(new Tuple("selected items: ", selected.toString()), screen);
            }

        }

    }

    /**
     * Collects the tuples inserted in the input field and outs them in the
     * ScreenNode and in the ListNode
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class CollectorProcess extends KlavaProcess {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * 
         */
        public CollectorProcess() {

        }

        /**
         * @param name
         */
        public CollectorProcess(String name) {
            super(name);
        }

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                /* reads a string from the text field */
                Tuple tuple = new Tuple(new KString());
                in(tuple, keyboard);
                out(tuple, list);
                out(new Tuple(tuple + "\n"), screen);
            }

        }

    }

    KlavaNode node = new KlavaNode();

    ScreenNode screenNode;

    KeyboardNode keyboardNode;

    ButtonNode buttonNode;

    ListNode listNode;

    Locality screen;

    Locality list;

    Locality keyboard;

    Locality button;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JPanel jPanel = null;

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
        }
        return jPanel;
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws IMCException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        GuiNodeExample screenNodeExample = new GuiNodeExample();
        screenNodeExample.setDefaultCloseOperation(EXIT_ON_CLOSE);
        screenNodeExample.pack();
        screenNodeExample.setVisible(true);
    }

    /**
     * This is the default constructor
     * 
     * @throws KlavaException
     * @throws IMCException
     */
    public GuiNodeExample() throws KlavaException, IMCException {
        super();
        screenNode = new ScreenNode("ScreenNode example");
        screenNode.out(new Tuple("this is a demonstration of ScreenNode\n\n"));
        screenNode
                .out(new Tuple(
                        "an out of a tuple into this node simply prints it in the TextArea\n"));
        screenNode.out(new Tuple("a tuple", true, 10));
        screenNode.out(new Tuple("another tuple", false, 100));

        screenNode.out(new Tuple("Type something in the below text edit\n"));
        screenNode.out(new Tuple(
                "a process is waiting for tuples from that text edit\n"));
        screenNode.out(new Tuple(
                "and it will out them in the screen node and in the list\n\n"));

        screenNode.out(new Tuple("Press the button under the list\n"));
        screenNode
                .out(new Tuple(
                        "a process will intercept the event through an in(\"CLICKED\")\n"));
        screenNode
                .out(new Tuple(
                        "and will out in the screen node the tuples that are selected in the list\n"));

        /* connects the ScreenNode to the main Node */
        screen = node.newloc(screenNode);

        keyboardNode = new KeyboardNode("input example");

        /* connects the KeyboardNode to the main Node */
        keyboard = node.newloc(keyboardNode);

        listNode = new ListNode("list example");

        /* connects the ListNode to the main Node */
        list = node.newloc(listNode);

        node.addNodeProcess(new CollectorProcess());

        buttonNode = new ButtonNode("show selected items");

        button = node.newloc(buttonNode);

        node.addNodeProcess(new ButtonProcess());

        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(300, 200);
        this.setContentPane(getJContentPane());
        this.setTitle("ScreenNode");
        this.add(screenNode.getPanel(), BorderLayout.CENTER);
        this.add(keyboardNode.getPanel(), BorderLayout.SOUTH);
        this.getJPanel().add(listNode.getPanel(), BorderLayout.CENTER);
        this.getJPanel().add(buttonNode.getPanel(), BorderLayout.SOUTH);
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
            jContentPane.add(getJPanel(), java.awt.BorderLayout.EAST);
        }
        return jContentPane;
    }

}
