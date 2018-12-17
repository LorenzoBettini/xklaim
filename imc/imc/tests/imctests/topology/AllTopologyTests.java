/*
 * Created on Jan 5, 2005
 *
 */
package imctests.topology;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Runs all the tests concerning topology.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class AllTopologyTests {
    /**
     *
     *
     * @param args 
     */
    public static void main(String[] args) {
    }

    /**
     * Runs all the tests concerning topology.
     *
     * @return  the TestSuite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for imctests.topology");

        //$JUnit-BEGIN$
        suite.addTestSuite(RoutingTableTest.class);
        suite.addTestSuite(TCPConnectionTest.class);
        suite.addTestSuite(ConnectDisconnectStateTest.class);
        suite.addTestSuite(ConnectDisconnectStateUDPTest.class);
        suite.addTestSuite(ConnectDisconnectStateLocalTest.class);
        suite.addTestSuite(ApplicationTest.class);
        suite.addTestSuite(NodeTest.class);
        suite.addTestSuite(NodeUDPTest.class);
        suite.addTestSuite(NodeLocalTest.class);
        suite.addTestSuite(ProcessContainerTest.class);
        //$JUnit-END$
        return suite;
    }
}
