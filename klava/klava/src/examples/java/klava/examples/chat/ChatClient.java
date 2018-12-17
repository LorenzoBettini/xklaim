/*
 * Created on May 17, 2006
 */
package klava.examples.chat;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.gui.ExceptionMessageBox;

import klava.KBoolean;
import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpaceVector;
import klava.gui.TupleSpaceButton;
import klava.gui.TupleSpaceKeyboard;
import klava.gui.TupleSpaceList;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaProcess;

/**
 * The main class implementing a Chat client
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ChatClient {

    /**
     * Reads a message inserted by the user and sends it to the server. It also
     * checks if some recipients are selected in the user list (in that case
     * it's a private message and thus sends the list of the recipients too,
     * otherwise sends an empty recipient list)
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class ChatMessageSender extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                KString messageBody = new KString();
                in(new Tuple(messageBody), messageKeyboard);

                /* check whether some users are selected in the list */
                TupleSpaceVector selectedRecipients = new TupleSpaceVector();
                out(new Tuple(TupleSpaceList.cmdString,
                        TupleSpaceList.getSelectedItemsString), usersList);
                in(new Tuple(TupleSpaceList.cmdString,
                        TupleSpaceList.getSelectedItemsString,
                        selectedRecipients), usersList);

                /* we must convert strings to LogicalLocalities */
                KString clientName = new KString();
                Tuple clientNames = new Tuple(clientName);
                while (selectedRecipients.in_nb(clientNames)) {
                    selectedRecipients.out(new Tuple(new LogicalLocality(
                            clientName)));
                    clientNames.resetOriginalTemplate();
                }

                out(new Tuple(messageString, messageBody, myNickName,
                        selectedRecipients), serverPhysicalLocality);
            }
        }

    }

    /**
     * Receives a chat message and show it on the screen
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class ChatMessageReceiver extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                KString messageBody = new KString();
                LogicalLocality sender = new LogicalLocality();
                KBoolean privateMessage = new KBoolean();

                in(
                        new Tuple(messageString, messageBody, sender,
                                privateMessage), self);
                out(new Tuple((privateMessage.booleanValue() ? "PRIV " : "")
                        + "(" + sender + "): " + messageBody + "\n"), screen);
            }
        }
    }

    /**
     * Receives a server message and show it on the screen and updates other
     * structures (i.e., user list)
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class ChatServerMessageReceiver extends KlavaProcess {

        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @see klava.topology.KlavaProcess#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                KString messageBody = new KString();
                LogicalLocality client = new LogicalLocality();

                in(new Tuple(serverString, messageBody, client), self);
                out(new Tuple("SERVER: " + client + " " + messageBody
                        + " chat\n"), screen);
                if (messageBody.equals(enteredString)) {
                    if (!read_nb(new Tuple(client), usersList)) {
                        out(new Tuple(client), usersList);
                    }
                } else { // it's a LEFT message
                    in_nb(new Tuple(client), usersList);
                }
            }
        }
    }

    /**
     * Waits for the "Enter Chat" button to be pressed and enters the chat
     * (specified by the server locality) with the specified nick name, using
     * subscribe operation.
     * 
     * If already part of the chat, if the button is pressed, then leaves the
     * chat (with unsubscribe operation).
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class ChatSubscribeCoordinator extends KlavaNodeCoordinator {

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            while (true) {
                /* wait for the button to be pressed */
                in(new Tuple(TupleSpaceButton.clickedString), serverButton);

                KString serverLocString = new KString();
                KString nickName = new KString();

                try {
                    if (serverPhysicalLocality == null) {
                        /* check that the user specified all the required info */
                        if (!(read_nb(new Tuple(
                                TupleSpaceKeyboard.getTextString,
                                serverLocString), serverKeyboard)
                                && read_nb(new Tuple(
                                        TupleSpaceKeyboard.getTextString,
                                        nickName), nickKeyboard)
                                && serverLocString.length() > 0 && nickName
                                .length() > 0)) {
                            out(
                                    new Tuple(
                                            "you must specify server locality and nickname\n"),
                                    screen);
                            continue;
                        }

                        serverPhysicalLocality = new PhysicalLocality(
                                serverLocString);
                        myNickName = new LogicalLocality(nickName);

                        out(new Tuple("entering chat...\n"), screen);
                        if (!subscribe(serverPhysicalLocality, myNickName)) {
                            out(new Tuple("entering chat failed\n"), screen);
                            continue;
                        }

                        out(new Tuple("entered chat " + serverPhysicalLocality
                                + "\n"), screen);

                        /* change button label */
                        out(new Tuple("Leave Chat"), serverButton);

                        /* get the current clients (timeout: 5 seconds) */
                        TupleSpaceVector currentClients = new TupleSpaceVector();
                        if (in_t(new Tuple(serverString, currentClients), self,
                                5000)) {
                            LogicalLocality clientLoc = new LogicalLocality();
                            Tuple clientTuple = new Tuple(clientLoc);
                            while (currentClients.read_nb(clientTuple)) {
                                out(new Tuple(new LogicalLocality(clientLoc)),
                                        usersList);
                                clientTuple.resetOriginalTemplate();
                            }
                        }
                        
                        eval(new ChatDisconnectedCoordinator());
                    } else {
                        unsubscribe(serverPhysicalLocality, myNickName);
                        out(new Tuple("left chat " + serverPhysicalLocality
                                + "\n"), screen);

                        /* reset button label */
                        out(new Tuple("Enter Chat"), serverButton);

                        /* clear client list */
                        out(new Tuple(TupleSpaceList.cmdString,
                                TupleSpaceList.removeAllString), usersList);

                        serverPhysicalLocality = null;
                    }
                } catch (KlavaException e) {
                    new ExceptionMessageBox(null, e).setVisible(true);
                }
            }
        }

    }

    /**
     * Intercepts disconnections from server
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public class ChatDisconnectedCoordinator extends KlavaNodeCoordinator {

        /**
         * @see klava.topology.KlavaNodeCoordinator#executeProcess()
         */
        @Override
        public void executeProcess() throws KlavaException {
            PhysicalLocality disconnectedPhyLoc = new PhysicalLocality();

            disconnected(disconnectedPhyLoc);

            out(new Tuple("disconnected from chat " + disconnectedPhyLoc
                            + "\n"), screen);

            /* clear client list */
            out(new Tuple(TupleSpaceList.cmdString,
                    TupleSpaceList.removeAllString), usersList);
            
            serverPhysicalLocality = null;
            
            /* reset button label */
            out(new Tuple("Enter Chat"), serverButton);
        }

    }

    ChatClientFrame chatClientFrame;

    Locality screen = new LogicalLocality("screen");

    Locality usersList = new LogicalLocality("users");

    Locality serverKeyboard = new LogicalLocality("serverKeyboard");

    Locality nickKeyboard = new LogicalLocality("nickKeyboard");

    Locality messageKeyboard = new LogicalLocality("messageKeyboard");

    Locality serverButton = new LogicalLocality("serverButton");

    final KString messageString = new KString("MSG");

    final KString serverString = new KString("SERVER");

    final KString enteredString = new KString("ENTERED");

    final KString leftString = new KString("LEFT");

    PhysicalLocality serverPhysicalLocality;

    LogicalLocality myNickName;

    /**
     * @param serverLoc
     *            The default PhysicalLocality of the server
     * @param nick
     *            The default nick name
     * @throws KlavaException
     * @throws IMCException
     * 
     */
    public ChatClient(String serverLoc, String nick) throws KlavaException,
            IMCException {
        chatClientFrame = new ChatClientFrame();

        KlavaNode node = chatClientFrame.getNode();

        /* initialize the GUI */
        node.out(new Tuple(new KString("setText"), serverLoc), serverKeyboard);
        node.out(new Tuple(new KString("setText"), nick), nickKeyboard);

        node.eval(new ChatMessageSender());
        node.eval(new ChatMessageReceiver());
        node.eval(new ChatServerMessageReceiver());
        node.addNodeCoordinator(new ChatSubscribeCoordinator());

        chatClientFrame.setVisible(true);
    }

    /**
     * @param args
     * @throws KlavaException
     * @throws IMCException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        new ChatClient("tcp-127.0.0.1:9999", "guest");
    }

    /**
     * @return Returns the chatClientFrame.
     */
    public ChatClientFrame getChatClientFrame() {
        return chatClientFrame;
    }

}
