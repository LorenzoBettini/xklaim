/*
 * Created on Apr 5, 2006
 */
package imctests.topology;

import java.util.Vector;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventCollector;
import org.mikado.imc.events.NodeEvent;
import org.mikado.imc.events.AddRemoveEvent.EventType;
import org.mikado.imc.topology.Application;
import org.mikado.imc.topology.Node;

import junit.framework.TestCase;

/**
 * Tests for the Application class
 * 
 * @author bettini
 * @version $Revision: 1.2 $
 */
public class ApplicationTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNodeEvents() throws IMCException, InterruptedException {
        Application application = new Application();

        Node node1 = new Node("node1");
        Node node2 = new Node("node2");

        EventCollector eventCollector = new EventCollector();
        application.addListener(NodeEvent.NodeEventId, eventCollector);

        application.addNode(node1);
        node1.close();

        /* we must have received two events */
        eventCollector.waitForEventNumber(2);
        Vector<Event> events = eventCollector.getCollectedEvents();

        assertTrue(((NodeEvent) events.elementAt(0)).eventType == EventType.ADDED);
        assertEquals(((NodeEvent) events.elementAt(0)).element.getNodeName(),
                node1.getNodeName());
        assertTrue(((NodeEvent) events.elementAt(1)).eventType == EventType.REMOVED);
        assertEquals(((NodeEvent) events.elementAt(1)).element.getNodeName(),
                node1.getNodeName());
        
        application.addNode(node2);
        node2.close();

        /* we must have received four events by now */
        eventCollector.waitForEventNumber(4);
        events = eventCollector.getCollectedEvents();

        assertTrue(((NodeEvent) events.elementAt(2)).eventType == EventType.ADDED);
        assertEquals(((NodeEvent) events.elementAt(2)).element.getNodeName(),
                node2.getNodeName());
        assertTrue(((NodeEvent) events.elementAt(3)).eventType == EventType.REMOVED);
        assertEquals(((NodeEvent) events.elementAt(3)).element.getNodeName(),
                node2.getNodeName());
    }
}
