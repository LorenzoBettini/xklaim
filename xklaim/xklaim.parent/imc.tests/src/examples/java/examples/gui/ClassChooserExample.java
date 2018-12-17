/*
 * Created on 6-apr-2006
 */
/**
 * 
 */
package examples.gui;

import java.awt.BorderLayout;
import java.lang.reflect.Constructor;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mikado.imc.common.ClassEntry;
import org.mikado.imc.common.IMCException;
import org.mikado.imc.gui.ClassChooserFilteredDialog;
import org.mikado.imc.gui.DesktopFrame;
import org.mikado.imc.gui.ExceptionMessageBox;
import org.mikado.imc.gui.NodeDesktopFrame;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.NodeProcess;

/**
 * Uses the ClassChooser dialog
 * 
 * @author bettini
 * @version $Revision: 1.4 $
 */
public class ClassChooserExample extends DesktopFrame {
    /**
     * @author bettini
     * @version $Revision: 1.4 $
     */
    public static class NodeWithFrame extends NodeDesktopFrame {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param node
         */
        public NodeWithFrame(Node node) {
            super(node);
        }

    }

	/**
     * @author bettini
     * @version $Revision: 1.4 $
     */
    public static class HelloWorldProcess extends NodeProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see org.mikado.imc.topology.NodeProcess#execute()
         */
        @Override
        public void execute() throws IMCException {
            SystemOutPrint("hello world\n");
        }

    }

    /**
     * @author bettini
     * @version $Revision: 1.4 $
     */
    public static class DateProcess extends NodeProcess {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see org.mikado.imc.topology.NodeProcess#execute()
         */
        @Override
        public void execute() throws IMCException {
            while (true) {
                SystemOutPrint(new Date() + "\n");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

    }

    /**
     * @author bettini
     * @version $Revision: 1.4 $
     */
    public class MyNode extends Node {

        /**
         * 
         */
        public MyNode() {
            super("My Node");
        }

        /**
         * Prints on the text area
         * 
         * @see org.mikado.imc.topology.Node#SystemErrPrint(java.lang.String)
         */
        @Override
        public void SystemErrPrint(String s) {
            getMainTextArea().append(s);
        }

        /**
         * Prints on the text area
         * 
         * @see org.mikado.imc.topology.Node#SystemOutPrint(java.lang.String)
         */
        @Override
        public void SystemOutPrint(String s) {
            getMainTextArea().append(s);
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JMenuItem openNodeMenuItem = null;
    
    private JMenuItem openNodeFrameMenuItem = null;

    private JMenuItem openProcessMenuItem = null;

    private JScrollPane jScrollPane = null;

    private JTextArea mainTextArea = null;

    protected ClassChooserFilteredDialog nodeChooser;
    
    protected ClassChooserFilteredDialog nodeFrameChooser;

    protected ClassChooserFilteredDialog processChooser;

    protected MyNode myNode = new MyNode();

    /**
     * This method initializes openNodeMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenNodeMenuItem() {
        if (openNodeMenuItem == null) {
            openNodeMenuItem = new JMenuItem();
            openNodeMenuItem.setText("Open Node");
            openNodeMenuItem
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            nodeChooser.setVisible(true);
                            ClassEntry node = nodeChooser.getChosenEntry();
                            if (node != null) {
                                try {
                                    Class.forName(
                                            node.getFullyQualifiedClassName())
                                            .newInstance();
                                    setStatusBarMessage("added " + node);
                                } catch (Exception e1) {
                                    showException(e1);
                                }
                            }
                        }
                    });
        }
        return openNodeMenuItem;
    }

    /**
     * This method initializes openNodeMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenNodeFrameMenuItem() {
        if (openNodeFrameMenuItem == null) {
            openNodeFrameMenuItem = new JMenuItem();
            openNodeFrameMenuItem.setText("Open NodeFrame");
            openNodeFrameMenuItem
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            nodeFrameChooser.setVisible(true);
                            ClassEntry node = nodeFrameChooser.getChosenEntry();
                            if (node != null) {
                                try {
                                    Class<?> parameters[] = { Node.class };
                                    Class<?> c = Class.forName(
                                            node.getFullyQualifiedClassName());
                                    Constructor<?> cons = c.getConstructor(parameters);
                                    Object args[] = { new Node() };
                                    addFrame((JFrame)cons.newInstance(args));
                                    setStatusBarMessage("added " + node);
                                } catch (Exception e1) {
                                    showException(e1);
                                }
                            }
                        }
                    });
        }
        return openNodeFrameMenuItem;
    }

    
    /**
     * This method initializes openProcessMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenProcessMenuItem() {
        if (openProcessMenuItem == null) {
            openProcessMenuItem = new JMenuItem();
            openProcessMenuItem.setText("Open Process");
            openProcessMenuItem
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            processChooser.setVisible(true);
                            ClassEntry process = processChooser
                                    .getChosenEntry();
                            if (process != null) {
                                try {
                                    myNode
                                            .addNodeProcess((NodeProcess) Class
                                                    .forName(
                                                            process
                                                                    .getFullyQualifiedClassName())
                                                    .newInstance());
                                    setStatusBarMessage("added " + process);
                                } catch (Exception e1) {
                                    (new ExceptionMessageBox(null, e1)).setVisible(true);
                                }
                            }
                        }
                    });
        }
        return openProcessMenuItem;
    }

    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getMainTextArea());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea
     * 
     * @return javax.swing.JTextArea
     */
    private JTextArea getMainTextArea() {
        if (mainTextArea == null) {
            mainTextArea = new JTextArea();
            mainTextArea.setRows(10);
            mainTextArea.setColumns(40);
        }
        return mainTextArea;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassChooserExample classChooserExample = new ClassChooserExample();
        classChooserExample.setVisible(true);
    }

    /**
     * This is the default constructor
     */
    public ClassChooserExample() {
        super();
        nodeChooser = new ClassChooserFilteredDialog(Node.class.getName(), this,
                "Choose a Node", true);
        nodeFrameChooser = new ClassChooserFilteredDialog(NodeDesktopFrame.class.getName(), this,
                "Choose a NodeFrame", true);
        processChooser = new ClassChooserFilteredDialog(NodeProcess.class.getName(), this,
                "Choose a Process", true);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(620, 480);
        this.setJMenuBar(getJJMenuBar());
        this.setTitle("ClassChooser example");
        
        JMenu fileMenu = getFileMenu();
        fileMenu.insert(getOpenNodeMenuItem(), 0);
        fileMenu.insert(getOpenNodeFrameMenuItem(), 0);
        fileMenu.insert(getOpenProcessMenuItem(), 0);
        
        addFrame(getJContentPane());
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
            jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * 
     */
    @Override
    protected void close() {
        try {
            myNode.close();
        } catch (IMCException e1) {
            e1.printStackTrace();
        }
        dispose();
    }

} // @jve:decl-index=0:visual-constraint="10,10"
