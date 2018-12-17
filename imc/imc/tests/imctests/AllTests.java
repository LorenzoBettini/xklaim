/*
 * Created on Jan 18, 2005
 *
 */
package imctests;

import imctests.common.ClassCollectorTest;
import imctests.events.EventTest;
import imctests.protocols.EchoStateTest;
import imctests.protocols.ObjectTest;
import imctests.protocols.ProtocolLayerCompositeTest;
import imctests.protocols.ProtocolStackTest;
import imctests.protocols.ProtocolStateTest;
import imctests.protocols.SessionIdTests;
import imctests.protocols.SessionNumberLayerTest;
import imctests.protocols.SessionStarterLocalTest;
import imctests.protocols.SessionStarterTest;
import imctests.protocols.SessionStarterUDPTest;
import imctests.protocols.SessionTests;
import imctests.protocols.TcpIpLayerTest;
import imctests.protocols.UdpTests;
import imctests.topology.ApplicationTest;
import imctests.topology.ConnectDisconnectStateLocalTest;
import imctests.topology.ConnectDisconnectStateTest;
import imctests.topology.NodeLocalTest;
import imctests.topology.NodeProcessTest;
import imctests.topology.NodeTest;
import imctests.topology.NodeUDPTest;
import imctests.topology.ProcessContainerTest;
import imctests.topology.RoutingTableTest;
import imctests.topology.TCPConnectionTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author bettini
 *
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for imctests");
        //$JUnit-BEGIN$
        suite.addTestSuite(SessionIdTests.class);
        suite.addTestSuite(SessionTests.class);
        suite.addTestSuite(ClassCollectorTest.class);
        suite.addTestSuite(EchoStateTest.class);
        suite.addTestSuite(TcpIpLayerTest.class);
        suite.addTestSuite(SessionNumberLayerTest.class);
        suite.addTestSuite(ProtocolLayerCompositeTest.class);
        suite.addTestSuite(UdpTests.class);
        suite.addTestSuite(ProtocolStateTest.class);
        suite.addTestSuite(ProtocolStackTest.class);
        suite.addTestSuite(ObjectTest.class);
        suite.addTestSuite(EventTest.class);
        suite.addTestSuite(RoutingTableTest.class);
        suite.addTestSuite(SessionStarterTest.class);
        suite.addTestSuite(SessionStarterUDPTest.class);
        suite.addTestSuite(SessionStarterLocalTest.class);
        suite.addTestSuite(TCPConnectionTest.class);
        suite.addTestSuite(ConnectDisconnectStateTest.class);
        //suite.addTestSuite(ConnectDisconnectStateUDPTest.class);
        suite.addTestSuite(ConnectDisconnectStateLocalTest.class);
        suite.addTestSuite(ApplicationTest.class);
        suite.addTestSuite(NodeTest.class);
        suite.addTestSuite(NodeUDPTest.class);
        suite.addTestSuite(NodeLocalTest.class);
        suite.addTestSuite(ProcessContainerTest.class);
        suite.addTestSuite(NodeProcessTest.class);
        //$JUnit-END$
        return suite;
    }
}
