/*
 * Created on 31-mag-2006
 */
package klava.examples.chat;

import klava.KlavaException;

import org.mikado.imc.common.IMCException;

/**
 * A Chat example application: start the server and some clients.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ChatExample {
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 25;

    public ChatServer chatServer;

    public ChatClient chatClient1;

    public ChatClient chatClient2;

    public ChatClient chatClient3;

    /**
     * @throws IMCException
     * @throws KlavaException
     * 
     */
    public ChatExample() throws KlavaException, IMCException {
        chatServer = new ChatServer();

        /* on the right of the server window */
        chatClient1 = new ChatClient("tcp-127.0.0.1:9999", "guest");
        chatClient1.getChatClientFrame().setLocation(
                chatServer.getChatServerFrame().getWidth() + OFFSET_X, 0);

        /* below the server window */
        chatClient2 = new ChatClient("tcp-127.0.0.1:9999", "foo");
        chatClient2.getChatClientFrame().setLocation(0,
                chatServer.getChatServerFrame().getHeight() + OFFSET_Y);

        /* below the client1 and on the right of client2 */
        chatClient3 = new ChatClient("tcp-127.0.0.1:9999", "bar");
        chatClient3.getChatClientFrame().setLocation(
                chatServer.getChatServerFrame().getWidth() + OFFSET_X,
                chatServer.getChatServerFrame().getHeight() + OFFSET_Y);
    }

    /**
     * @param args
     * @throws IMCException
     * @throws KlavaException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        new ChatExample();
    }

}
