/*
 * Created on May 27, 2005
 */
package org.mikado.imc.events;

import java.util.Vector;

import org.mikado.imc.events.SessionEvent.SessionEventType;
import org.mikado.imc.protocols.Session;

/**
 * EventListener for SessionEvents. It stores connection and disconnection
 * events into two separates Vectors as strings.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class SessionEventListener implements EventListener {
	Vector<String> connections = new Vector<String>();

	Vector<String> disconnections = new Vector<String>();

	/**
	 * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
	 */
	public synchronized void notify(Event event) {
		if (!(event instanceof SessionEvent))
			return;

		SessionEvent connectionEvent = (SessionEvent) event;

		Session sessionId = connectionEvent.getSession();
		if (connectionEvent.type == SessionEventType.CONNECTION)
			connections.addElement(sessionId.toString());
		else if (connectionEvent.type == SessionEventType.DISCONNECTION)
			disconnections.addElement(sessionId.toString());
	}

	/**
	 * @return Returns the connections.
	 */
	public final Vector<String> getConnections() {
		return connections;
	}

	/**
	 * @return Returns the disconnections.
	 */
	public final Vector<String> getDisconnections() {
		return disconnections;
	}
}