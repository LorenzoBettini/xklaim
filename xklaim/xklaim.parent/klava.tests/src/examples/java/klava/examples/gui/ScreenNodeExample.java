/*
 * Created on Mar 14, 2006
 */
package klava.examples.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import klava.Tuple;
import klava.gui.ScreenNode;

/**
 * Shows a ScreenNode in a frame.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ScreenNodeExample extends JFrame {
    ScreenNode screenNode;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ScreenNodeExample screenNodeExample = new ScreenNodeExample();
        screenNodeExample.setDefaultCloseOperation(EXIT_ON_CLOSE);
        screenNodeExample.pack();
        screenNodeExample.setVisible(true);
    }

    /**
     * This is the default constructor
     */
    public ScreenNodeExample() {
        super();
        screenNode = new ScreenNode("ScreenNode example");
        screenNode.out(new Tuple("this is a demonstration of ScreenNode\n\n"));
        screenNode
                .out(new Tuple(
                        "an out of a tuple into this node simply prints it in the TextArea\n"));
        screenNode.out(new Tuple("a tuple", true, 10));
        screenNode.out(new Tuple("another tuple", false, 100));
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
        }
        return jContentPane;
    }

}
