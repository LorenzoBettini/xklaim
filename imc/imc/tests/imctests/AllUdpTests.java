/*
 * Created on May 24, 2006
 */
package imctests;

import imctests.protocols.SessionStarterUDPTest;
import imctests.topology.ConnectDisconnectStateUDPTest;
import imctests.topology.NodeUDPTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for UDP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class AllUdpTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for imctests");
        //$JUnit-BEGIN$
        suite.addTestSuite(SessionStarterUDPTest.class);
        suite.addTestSuite(ConnectDisconnectStateUDPTest.class);
        suite.addTestSuite(NodeUDPTest.class);
        //$JUnit-END$
        return suite;
    }

}
