/*
 * Created on 15-gen-2005
 */
package examples.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.PrintEventListener;
import org.mikado.imc.gui.AcceptEstablishSessionPanel;
import org.mikado.imc.gui.AcceptSessionPanel;
import org.mikado.imc.gui.EstablishSessionPanel;
import org.mikado.imc.gui.SessionListPanel;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolFactory;
import org.mikado.imc.topology.Node;
import org.mikado.imc.topology.SessionManager;

/**
 * A simple application that monitors incoming and outgoing connections.
 * 
 * This uses two different event managers for handling accepted sessions and
 * established sessions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ConnectionMonitor extends JFrame {
    /**
     * Simple example protocol factory that creates an EchoProtocol
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public static class EchoAcceptProtocolFactory implements ProtocolFactory {

        public Protocol createProtocol() throws ProtocolException {
            return new EchoProtocol();
        }

    }

    /**
     * Simple example protocol that performs an EchoProtocolState
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public static class EchoProtocol extends Protocol {

        /**
         * 
         */
        public EchoProtocol() {
            super(new EchoProtocolState());
        }

    }

    private static final long serialVersionUID = 3257288024110019385L;

    private javax.swing.JPanel jContentPane = null;

    private SessionListPanel connectionList = null;

    private EstablishSessionPanel connectionPanel = null;

    private AcceptSessionPanel acceptSessionPanel = null;

    private JPanel jPanel = null;

    private SessionListPanel connectionOutList = null;

    private Node node;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JPanel sessionPanel = null;

    /**
     * @param node
     */
    public ConnectionMonitor(Node node) {
        super("Node");
        this.node = node;
        initialize();
    }

    public void start() throws IMCException {
        node.getEventManager().addListener(SessionManager.EventClass,
                new PrintEventListener());

        pack();
        setVisible(true);
        System.out.println("connection server started");
    }

    /**
     * This method initializes connectionList
     * 
     * @return org.mikado.imc.gui.SessionListPanel
     */
    private SessionListPanel getConnectionList() {
        if (connectionList == null) {
            /*
             * it replaces the original EventManager in the SessionManager for
             * accepted sessions.
             */
            EventManager eventManager = new EventManager();
            node.getSessionManagers().incomingSessionManager
                    .setEventManager(eventManager);
            connectionList = new SessionListPanel("Accepted sessions",
                    eventManager);
        }

        return connectionList;
    }

    /**
     * This method initializes connectionPanel
     * 
     * @return org.mikado.imc.gui.EstablishSessionPanel
     */
    private EstablishSessionPanel getConnectionPanel() {
        if (connectionPanel == null) {
            connectionPanel = new EstablishSessionPanel(node);
            connectionPanel.setProtocolIdText("tcp");
            connectionPanel.setAddressText("localhost:9999");
            connectionPanel.setProtocolText(EchoProtocol.class.getName());
        }

        return connectionPanel;
    }

    /**
     * This method initializes acceptSessionPanel
     * 
     * @return org.mikado.imc.gui.EstablishSessionPanel
     */
    private AcceptSessionPanel getAcceptSessionPanel() {
        if (acceptSessionPanel == null) {
            acceptSessionPanel = new AcceptSessionPanel(node);
            acceptSessionPanel.setProtocolIdText("tcp");
            acceptSessionPanel.setAddressText("localhost:9999");
            acceptSessionPanel.setProtocolText(EchoAcceptProtocolFactory.class
                    .getName());
        }

        return acceptSessionPanel;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridLayout gridLayout4 = new GridLayout();
            jPanel = new JPanel();
            jPanel.setLayout(gridLayout4);
            gridLayout4.setRows(1);
            gridLayout4.setColumns(2);
            gridLayout4.setHgap(10);
            jPanel.add(getJPanel1(), null);
            jPanel.add(getJPanel2(), null);
        }

        return jPanel;
    }

    /**
     * This method initializes connectionList1
     * 
     * @return org.mikado.imc.gui.SessionListPanel
     */
    private SessionListPanel getConnectionOutList() {
        if (connectionOutList == null) {
            /*
             * we don't need to use a different EventManager for established
             * sessions, since the SessionManager for incoming sessions has
             * already been replaced its EventManager: now the EventManager of
             * the node will see only established sessions events.
             */
            connectionOutList = new SessionListPanel("Established sessions");
            connectionOutList.setEventManager(node.getEventManager());
        }

        return connectionOutList;
    }

    /**
     * This method initializes jPanel1
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(new BorderLayout());
            jPanel1.add(getConnectionList(), java.awt.BorderLayout.CENTER);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new BorderLayout());
            jPanel2.add(getConnectionOutList(), java.awt.BorderLayout.CENTER);
        }
        return jPanel2;
    }

    /**
     * This method initializes sessionPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getSessionPanel() {
        if (sessionPanel == null) {
            sessionPanel = new AcceptEstablishSessionPanel(
                    getConnectionPanel(), getAcceptSessionPanel());
        }
        return sessionPanel;
    }

    /**
     * Runs the monitor.
     * 
     * @param args
     *            The listening port (default is 9999)
     * @throws IMCException
     */
    public static void main(String[] args) throws IMCException {
        Node node = new Node();
        ConnectionMonitor connectionMonitor = new ConnectionMonitor(node);
        connectionMonitor.setDefaultCloseOperation(EXIT_ON_CLOSE);
        connectionMonitor.start();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setContentPane(getJContentPane());
        this.setTitle("Connection Monitor");
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getSessionPanel(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
