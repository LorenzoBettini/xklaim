/*
 * Created on May 15, 2006
 */
package klava.examples.chat;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.gui.ButtonNode;
import klava.gui.KeyboardNode;
import klava.gui.ListNode;
import klava.gui.ScreenNode;
import klava.topology.KlavaNode;

import javax.swing.BoxLayout;

import org.mikado.imc.gui.CloseableFrame;

/**
 * The chat server frame.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ChatServerFrame extends CloseableFrame {

    /** */
    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    
    /** The node implementing the ChatClient */
    KlavaNode node = new KlavaNode();
    
    public ScreenNode screenNode;

    /** To interact with the chat server */
    public KeyboardNode serverKeyboardNode;
    
    /** To enter */
    public ButtonNode serverButtonNode;

    public ListNode usersListNode;

    public static final LogicalLocality screen = new LogicalLocality("screen");

    public static final LogicalLocality usersList = new LogicalLocality("users");

    public static final LogicalLocality serverKeyboard = new LogicalLocality("serverKeyboard");
    
    public static final LogicalLocality nickKeyboard = new LogicalLocality("nickKeyboard");
    
    public static final LogicalLocality messageKeyboard = new LogicalLocality("messageKeyboard");

    public static final LogicalLocality serverButton = new LogicalLocality("serverButton");
    
    private JPanel inputPanel = null;
    private JPanel serverPanel = null;

    /**
     * This method initializes inputPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getInputPanel() {
        if (inputPanel == null) {
            inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(getInputPanel(), BoxLayout.Y_AXIS));
            inputPanel.add(getServerPanel(), null);
        }
        return inputPanel;
    }

    /**
     * This method initializes serverPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getServerPanel() {
        if (serverPanel == null) {
            serverPanel = new JPanel();
            serverPanel.setLayout(new BoxLayout(getServerPanel(), BoxLayout.X_AXIS));
            serverPanel.add(serverKeyboardNode.getPanel());
            JPanel buttonPanel = serverButtonNode.getPanel();
            buttonPanel.setAlignmentY(TOP_ALIGNMENT);
            serverPanel.add(buttonPanel);
        }
        return serverPanel;
    }

    /**
     * @param args
     * @throws KlavaException 
     */
    public static void main(String[] args) throws KlavaException {
        new ChatServerFrame();
    }

    /**
     * This is the default constructor
     * @throws KlavaException 
     */
    public ChatServerFrame() throws KlavaException {
        super();
        
        /* when we close the frame we close the node */
        setCloseable(node);
        
        /* initialize and connect GUI nodes */
        serverKeyboardNode = new KeyboardNode("server locality");
        node.addToEnvironment(serverKeyboard, node.newloc(serverKeyboardNode));
        
        serverButtonNode = new ButtonNode("Start server");
        node.addToEnvironment(serverButton, node.newloc(serverButtonNode));
        
        screenNode = new ScreenNode("messages");
        node.addToEnvironment(screen, node.newloc(screenNode));
        
        usersListNode = new ListNode("users");
        node.addToEnvironment(usersList, node.newloc(usersListNode));
        
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(574, 314);
        this.setContentPane(getJContentPane());
        this.setTitle("Chat Server");
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
            jContentPane.add(getInputPanel(), BorderLayout.SOUTH);
            jContentPane.add(screenNode.getPanel(), BorderLayout.CENTER);
            jContentPane.add(usersListNode.getPanel(), BorderLayout.EAST);
        }
        return jContentPane;
    }

    /**
     * @return Returns the node.
     */
    public KlavaNode getNode() {
        return node;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
