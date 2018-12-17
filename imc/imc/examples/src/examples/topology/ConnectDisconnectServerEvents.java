/*
 * Created on May 24, 2005
 */
package examples.topology;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.events.WaitForEventListener;
import org.mikado.imc.events.WaitForEventSingleListener;
import org.mikado.imc.protocols.EchoProtocolState;
import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.tcp.TcpSessionStarter;
import org.mikado.imc.topology.ConnectionManagementState;
import org.mikado.imc.topology.SessionManager;

import examples.event.BufferedListener;

/**
 * A server that uses ConnectionManagementState for accepting a connection. The
 * initial and the final state share the same SessionManager. Moreover
 * connection/disconnection events are handled.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class ConnectDisconnectServerEvents {
    public static class WaitForEventThread extends Thread {
        WaitForEventListener waitForEventListener;
        
        /**
         * @param waitForEventListener
         */
        public WaitForEventThread(WaitForEventListener waitForEventListener) {
            this.waitForEventListener = waitForEventListener;
        }

        public void run() {
            try {
                System.out.println("wait for event: " + waitForEventListener.waitForEvent());
                System.out.println("wait for event: " + waitForEventListener.waitForEvent());
            } catch (InterruptedException e) {
                
            }
        }
    }

	public static void main(String[] args) throws Exception {
        SessionStarter sessionStarter = new TcpSessionStarter(new IpSessionId(
                "localhost", 9999), null);
        Protocol protocol = new Protocol();
        SessionManager sessionManager = new SessionManager();
        EventManager eventManager = new EventManager();
        sessionManager.setEventManager(eventManager);
        BufferedListener bufferedListener = new BufferedListener();
        WaitForEventListener waitForEventListener = new WaitForEventSingleListener();
        eventManager.addListener(SessionManager.EventClass, bufferedListener);
        eventManager.addListener(SessionManager.EventClass, waitForEventListener);
        new WaitForEventThread(waitForEventListener).start();
        ConnectionManagementState initialState = new ConnectionManagementState(
                "ECHO");
        initialState.setSessionManager(sessionManager);
        protocol.setState(Protocol.START, initialState);
        protocol.setState("ECHO", new EchoProtocolState(Protocol.END));
        ConnectionManagementState finalState = new ConnectionManagementState();
        finalState.setSessionManager(sessionManager);
        protocol.setState(Protocol.END, finalState);

        System.out.println("waiting for connections...");
        Session session = protocol.accept(sessionStarter);
        System.out.println("established session: " + session);

        protocol.start();

        System.out.println("protocol terminated");
        System.out.println("Listener buffer:\n" + bufferedListener.getBuffer());
    }
}
