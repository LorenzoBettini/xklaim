/*
 * Created on May 17, 2006
 */
package klava.examples.chat;

import klava.KBoolean;
import klava.KString;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpace;
import klava.TupleSpaceVector;
import klava.gui.TupleSpaceButton;
import klava.gui.TupleSpaceKeyboard;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaProcess;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.gui.ExceptionMessageBox;

/**
 * The main class implementing a Chat server
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ChatServer {

    /**
     * Delivers a server message to a client
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatServerMessageDeliver extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        KString messageBody;

        LogicalLocality recipient;

        LogicalLocality clientNick;

        /**
         * @param messageBody
         * @param recipient
         * @param clientNick
         */
        public ChatServerMessageDeliver(KString messageBody,
                LogicalLocality recipient, LogicalLocality clientNick) {
            this.messageBody = messageBody;
            this.recipient = recipient;
            this.clientNick = clientNick;
        }

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            out(new Tuple(serverString, messageBody, clientNick), recipient);
        }

    }

    /**
     * Delivers a chat message to a client
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatMessageDeliver extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        KString messageBody;

        LogicalLocality recipient;

        LogicalLocality sender;

        KBoolean privateMessage;

        /**
         * @param messageBody
         * @param recipient
         * @param sender
         * @param privateMessage
         */
        public ChatMessageDeliver(KString messageBody,
                LogicalLocality recipient, LogicalLocality sender,
                KBoolean privateMessage) {
            this.messageBody = messageBody;
            this.recipient = recipient;
            this.sender = sender;
            this.privateMessage = privateMessage;
        }

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            out(new Tuple(messageString, messageBody, sender, privateMessage),
                    recipient);
        }

    }

    /**
     * Receives messages from a client and dispatch it to all the clients (or to
     * specific clients if the message is a private one).
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatMessageDispatcher extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                KString messageBody = new KString();
                TupleSpace specificRecipients = new TupleSpaceVector();
                LogicalLocality sender = new LogicalLocality();

                in(new Tuple(messageString, messageBody, sender,
                        specificRecipients), self);

                out(new Tuple("MSG " + messageBody + " from " + sender + "\n"),
                        screen);

                if (specificRecipients.length() == 0) {
                    /* send to everyone */
                    Tuple recipient = new Tuple(new LogicalLocality());
                    while (read_nb(recipient, usersList)) {
                        /* make a copy of the logical locality... */
                        eval(new ChatMessageDeliver(messageBody,
                                new LogicalLocality(recipient.getItem(0)
                                        .toString()), sender, new KBoolean(
                                        false)), self);
                        /* ...since this will reset recipient.getItem(0) */
                        recipient.resetOriginalTemplate();
                    }
                } else {
                    /* send to the specified recipients */
                    Tuple recipient = new Tuple(new LogicalLocality());
                    while (specificRecipients.read_nb(recipient)) {
                        eval(new ChatMessageDeliver(messageBody,
                                new LogicalLocality(recipient.getItem(0)
                                        .toString()), sender,
                                new KBoolean(true)), self);
                        recipient.resetOriginalTemplate();
                    }
                }
            }
        }
    }

    /**
     * Catches clients disconnections
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatUnregisterCoordinator extends KlavaNodeCoordinator {

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                PhysicalLocality disconnectedPhyLoc = new PhysicalLocality();
                LogicalLocality disconnectedLogLoc = new LogicalLocality();

                disconnected(disconnectedPhyLoc, disconnectedLogLoc);

                out(new Tuple(disconnectedLogLoc + " left chat\n"), screen);
                in_nb(new Tuple(disconnectedLogLoc), usersList);

                /* notify other clients that a client left the chat */
                LogicalLocality clientLoc = new LogicalLocality();
                Tuple clientTuple = new Tuple(clientLoc);
                while (read_nb(clientTuple, usersList)) {
                    if (!(disconnectedLogLoc.equals(clientLoc))) {
                        /*
                         * notify other clients that someone entered the chat
                         */
                        eval(new ChatServerMessageDeliver(leftString,
                                new LogicalLocality(clientLoc),
                                disconnectedLogLoc));
                        /*
                         * make a copy of the clientLoc, since it will be reset
                         * by resetOriginalTemplate
                         */
                    }

                    clientTuple.resetOriginalTemplate();
                }
            }
        }

    }

    /**
     * Takes care of registering new clients of the chat.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatRegisterCoordinator extends KlavaNodeCoordinator {
        boolean accepting = false;

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                out(new Tuple("accepting clients...\n"), screen);

                PhysicalLocality clientPhysicalLocality = new PhysicalLocality();
                LogicalLocality clientNick = new LogicalLocality();

                if (register(serverPhysicalLocality, clientPhysicalLocality,
                        clientNick)) {
                    out(new Tuple(clientNick + " entered chat\n"), screen);
                    out(new Tuple(clientNick), usersList);

                    /* now send the list of current clients */
                    TupleSpaceVector currentClients = new TupleSpaceVector();
                    LogicalLocality clientLoc = new LogicalLocality();
                    Tuple clientTuple = new Tuple(clientLoc);
                    while (read_nb(clientTuple, usersList)) {
                        currentClients.out(new Tuple(clientLoc));

                        if (!(clientNick.equals(clientLoc))) {
                            /*
                             * notify other clients that someone entered the
                             * chat
                             */
                            eval(new ChatServerMessageDeliver(enteredString,
                                    new LogicalLocality(clientLoc), clientNick));
                            /*
                             * make a copy of the clientLoc, since it will be
                             * reset by resetOriginalTemplate
                             */
                        }

                        clientTuple.resetOriginalTemplate();
                    }
                    out(new Tuple(serverString, currentClients),
                            clientPhysicalLocality);
                }
            }
        }

    }

    /**
     * Handles button "start/stop server".
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class ChatStartStopAcceptCoordinator extends KlavaNodeCoordinator {
        boolean accepting = false;

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                in(new Tuple(TupleSpaceButton.clickedString), serverButton);

                if (accepting) {
                    closeSessions(serverPhysicalLocality);
                    accepting = false;
                    out(new Tuple("Start server"), serverButton);
                    out(new Tuple("server stopped\n"), screen);
                } else {
                    KString serverLoc = new KString();

                    if (!read_nb(new Tuple(TupleSpaceKeyboard.getTextString,
                            serverLoc), serverKeyboard)
                            || serverLoc.length() == 0) {
                        out(new Tuple("unspecified server locality\n"), screen);
                        continue;
                    }

                    try {
                        serverPhysicalLocality = new PhysicalLocality(serverLoc);
                    } catch (KlavaMalformedPhyLocalityException e) {
                        new ExceptionMessageBox(null, e).setVisible(true);
                    }

                    chatRegisterCoordinator = new ChatRegisterCoordinator();
                    eval(chatRegisterCoordinator);
                    accepting = true;
                    out(new Tuple("Stop server"), serverButton);
                    out(new Tuple("server started\n"), screen);
                }
            }
        }

    }

    ChatServerFrame chatServerFrame;

    public static final Locality screen = new LogicalLocality("screen");

    public static final Locality usersList = new LogicalLocality("users");

    public static final Locality serverKeyboard = new LogicalLocality(
            "serverKeyboard");

    public static final Locality serverButton = new LogicalLocality(
            "serverButton");

    ChatRegisterCoordinator chatRegisterCoordinator;

    PhysicalLocality serverPhysicalLocality;

    final KString messageString = new KString("MSG");

    final KString serverString = new KString("SERVER");

    final KString enteredString = new KString("ENTERED");

    final KString leftString = new KString("LEFT");

    /**
     * @throws KlavaException
     * @throws IMCException
     * 
     */
    public ChatServer() throws KlavaException, IMCException {
        chatServerFrame = new ChatServerFrame();

        KlavaNode node = chatServerFrame.getNode();

        /* initialize the GUI */
        node.out(new Tuple(new KString("setText"), "tcp-127.0.0.1:9999"),
                serverKeyboard);

        node.eval(new ChatMessageDispatcher());
        node.addNodeCoordinator(new ChatUnregisterCoordinator());
        node.addNodeCoordinator(new ChatStartStopAcceptCoordinator());

        chatServerFrame.setVisible(true);
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws IMCException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        new ChatServer();
    }

    /**
     * @see klava.examples.chat.ChatServerFrame#getNode()
     */
    public KlavaNode getNode() {
        return chatServerFrame.getNode();
    }

    /**
     * @return Returns the chatServerFrame.
     */
    public ChatServerFrame getChatServerFrame() {
        return chatServerFrame;
    }

}
