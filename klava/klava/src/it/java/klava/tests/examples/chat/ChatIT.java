/*
 * Created on Jun 1, 2006
 */
package klava.tests.examples.chat;

import junit.framework.TestCase;
import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.Tuple;
import klava.examples.chat.ChatExample;
import klava.examples.chat.ChatServer;
import klava.gui.ButtonNode;
import klava.gui.KeyboardNode;
import klava.gui.ListNode;
import klava.gui.ScreenNode;

/**
 * Tests for the Chat example.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ChatIT extends TestCase {
    ChatExample chatExample;

    protected void setUp() throws Exception {
        super.setUp();
        chatExample = new ChatExample();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testChat() throws KlavaException, InterruptedException {
        /* this should make the server start accepting connections */
        chatExample.chatServer.getChatServerFrame().serverButtonNode.out(
                new Tuple(new KString("CLICKED")), ChatServer.serverButton);

        Thread.sleep(3000);

        /* check that the server started accepting clients */
        String text = chatExample.chatServer.getChatServerFrame().screenNode
                .getJTextArea().getText();
        assertTrue(text.indexOf("server started") > 0);

        /* check that the button label has changed */
        assertEquals("Stop server",
                chatExample.chatServer.getChatServerFrame().serverButtonNode
                        .getJButton().getText());

        ButtonNode buttonNode = chatExample.chatClient1.getChatClientFrame().serverButtonNode;
        ScreenNode screenNode = chatExample.chatClient1.getChatClientFrame().screenNode;

        /* subscribe all clients */
        susbscribeClient(buttonNode, screenNode);
        susbscribeClient(
                chatExample.chatClient2.getChatClientFrame().serverButtonNode,
                chatExample.chatClient2.getChatClientFrame().screenNode);
        susbscribeClient(
                chatExample.chatClient3.getChatClientFrame().serverButtonNode,
                chatExample.chatClient3.getChatClientFrame().screenNode);

        KeyboardNode keyboardNode = chatExample.chatClient1
                .getChatClientFrame().messageKeyboardNode;
        String clientName = "guest";
        String privateString = "";

        ScreenNode screenNode2 = chatExample.chatClient2.getChatClientFrame().screenNode;
        ScreenNode screenNode3 = chatExample.chatClient3.getChatClientFrame().screenNode;

        sendMessage(screenNode, keyboardNode, clientName, privateString,
                screenNode2, screenNode3);
        sendMessage(
                screenNode2,
                chatExample.chatClient2.getChatClientFrame().messageKeyboardNode,
                "foo", privateString, screenNode, screenNode3);
        sendMessage(
                screenNode3,
                chatExample.chatClient3.getChatClientFrame().messageKeyboardNode,
                "bar", privateString, screenNode2, screenNode);

        /* now check private messages */

        int indices[] = { 0, 1 };
        privateString = "PRIV ";
        ListNode listNode = chatExample.chatClient1.getChatClientFrame().usersListNode;

        /* client1 sends only to client2 (and itself) */
        sendPrivateMessage(screenNode, keyboardNode, clientName, privateString,
                screenNode2, screenNode3, indices, listNode);

        /* client2 sends only to client1 (and itself) */
        sendPrivateMessage(screenNode2, chatExample.chatClient2
                .getChatClientFrame().messageKeyboardNode, "foo",
                privateString, screenNode, screenNode3, indices,
                chatExample.chatClient2.getChatClientFrame().usersListNode);

        /* client3 sends only to client1 and client2 (but not itself) */
        sendPrivateMessage(screenNode, chatExample.chatClient3
                .getChatClientFrame().messageKeyboardNode, "bar",
                privateString, screenNode2, screenNode3, indices,
                chatExample.chatClient3.getChatClientFrame().usersListNode);

        Thread.sleep(3000);

        /* client 1 exits chat */

        /* this should make the client exit the chat */
        buttonNode.out(new Tuple(new KString("CLICKED")),
                ChatServer.serverButton);

        Thread.sleep(3000);

        /* check that the user list is clear now */
        assertEquals(0, listNode.getTupleSpace().length());

        /* check that the server unregistered client */
        text = chatExample.chatServer.getChatServerFrame().screenNode
                .getJTextArea().getText();
        assertTrue(text.indexOf("guest left chat") > 0);

        /* check that the client exited the chat */
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf("left chat") > -1);

        clientName = "guest";

        /* check that the other client know about it */
        text = screenNode2.getJTextArea().getText();
        assertTrue(text.indexOf(clientName + " LEFT chat") > -1);
        assertFalse(chatExample.chatClient2.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("guest"))));
        assertTrue(chatExample.chatClient2.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("foo"))));
        assertTrue(chatExample.chatClient2.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("bar"))));
        assertFalse(chatExample.chatClient3.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("guest"))));
        assertTrue(chatExample.chatClient3.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("foo"))));
        assertTrue(chatExample.chatClient3.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("bar"))));

        text = screenNode3.getJTextArea().getText();
        assertTrue(text.indexOf(clientName + " LEFT chat") > -1);

        Thread.sleep(3000);

        /* now client 1 enters the chat again */
        susbscribeClient(buttonNode, screenNode);

        Thread.sleep(3000);

        /* check that the other client know about it */
        text = screenNode2.getJTextArea().getText();
        assertTrue(text.indexOf(clientName + " ENTERED chat") > -1);

        text = screenNode3.getJTextArea().getText();
        assertTrue(text.indexOf(clientName + " ENTERED chat") > -1);

        assertTrue(chatExample.chatClient2.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("guest"))));
        assertTrue(chatExample.chatClient3.getChatClientFrame().usersListNode
                .read_nb(new Tuple(new LogicalLocality("guest"))));

        Thread.sleep(3000);

        /* now stop the server */
        chatExample.chatServer.getChatServerFrame().serverButtonNode.out(
                new Tuple(new KString("CLICKED")), ChatServer.serverButton);

        Thread.sleep(3000);

        /* check that the server stopped accepting clients */
        text = chatExample.chatServer.getChatServerFrame().screenNode
                .getJTextArea().getText();
        assertTrue(text.indexOf("server stopped") > 0);

        Thread.sleep(3000);

        /* check that the clients detected this */
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf("disconnected from chat") > 0);
        text = screenNode2.getJTextArea().getText();
        assertTrue(text.indexOf("disconnected from chat") > 0);
        text = screenNode3.getJTextArea().getText();
        assertTrue(text.indexOf("disconnected from chat") > 0);

        assertEquals(0,
                chatExample.chatClient1.getChatClientFrame().usersListNode
                        .getTupleSpace().length());
        assertEquals(0,
                chatExample.chatClient2.getChatClientFrame().usersListNode
                        .getTupleSpace().length());
        assertEquals(0,
                chatExample.chatClient3.getChatClientFrame().usersListNode
                        .getTupleSpace().length());

        Thread.sleep(3000);
    }

    /**
     * Send a private message and check that only the right recipients get the
     * message.
     * 
     * @param screenNode
     * @param keyboardNode
     * @param clientName
     * @param privateString
     * @param screenNode2
     * @param screenNode3
     * @param indices
     * @param listNode
     * @throws InterruptedException
     */
    private void sendPrivateMessage(ScreenNode screenNode,
            KeyboardNode keyboardNode, String clientName, String privateString,
            ScreenNode screenNode2, ScreenNode screenNode3, int[] indices,
            ListNode listNode) throws InterruptedException {
        listNode.getJList().setSelectedIndices(indices);

        keyboardNode.out(new Tuple(new KString("private message from "
                + clientName)));

        Thread.sleep(3000);

        checkReceivedPrivateMessage(screenNode, clientName, privateString);

        checkReceivedPrivateMessage(screenNode2, clientName, privateString);

        /* this must not have received the message */
        checkNotReceivedPrivateMessage(screenNode3, clientName, privateString);
    }

    /**
     * @param screenNode
     * @param clientName
     * @param privateString
     */
    private void checkReceivedPrivateMessage(ScreenNode screenNode,
            String clientName, String privateString) {
        String text;
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf(privateString + "(" + clientName + "): "
                + "private message from " + clientName) > -1);
    }

    /**
     * @param screenNode
     * @param clientName
     * @param privateString
     */
    private void checkNotReceivedPrivateMessage(ScreenNode screenNode,
            String clientName, String privateString) {
        String text;
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf(privateString + "(" + clientName + "): "
                + "private message from " + clientName) == -1);
    }

    /**
     * send a message from a client, and check the other clients get it,
     * including the sender client itself
     * 
     * @param screenNode
     * @param keyboardNode
     * @param clientName
     * @param privateString
     * @param screenNode2
     * @param screenNode3
     * @throws InterruptedException
     */
    private void sendMessage(ScreenNode screenNode, KeyboardNode keyboardNode,
            String clientName, String privateString, ScreenNode screenNode2,
            ScreenNode screenNode3) throws InterruptedException {
        keyboardNode.out(new Tuple(new KString("message from " + clientName)));

        Thread.sleep(3000);

        checkReceivedMessage(screenNode, clientName, privateString);

        checkReceivedMessage(screenNode2, clientName, privateString);

        checkReceivedMessage(screenNode3, clientName, privateString);
    }

    /**
     * @param screenNode
     * @param clientName
     * @param privateString
     */
    private void checkReceivedMessage(ScreenNode screenNode, String clientName,
            String privateString) {
        String text;
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf(privateString + "(" + clientName + "): "
                + "message from " + clientName) > -1);
    }

    /**
     * @param buttonNode
     * @param screenNode
     * @throws KlavaException
     * @throws InterruptedException
     */
    private void susbscribeClient(ButtonNode buttonNode, ScreenNode screenNode)
            throws KlavaException, InterruptedException {
        String text;
        /* this should make the client entering the chat */
        buttonNode.out(new Tuple(new KString("CLICKED")),
                ChatServer.serverButton);

        Thread.sleep(3000);

        /* check that the server accepted client */
        text = chatExample.chatServer.getChatServerFrame().screenNode
                .getJTextArea().getText();
        assertTrue(text.indexOf("guest entered chat") > 0);

        /* check that the client entered the chat */
        text = screenNode.getJTextArea().getText();
        assertTrue(text.indexOf("entering chat") > -1);
        assertTrue(text.indexOf("entered chat") > -1);
    }
}
