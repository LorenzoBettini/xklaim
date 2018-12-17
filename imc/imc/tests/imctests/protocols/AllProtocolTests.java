/*
 * Created on 12-gen-2005
 */
package imctests.protocols;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Runs all the tests concerning protocols.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class AllProtocolTests {
    /**
     * DOCUMENT ME!
     *
     * @param args
     */
    public static void main(String[] args) {
    }

    /**
     * Runs all the tests concerning protocols.
     *
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for imctests.protocols");

        //$JUnit-BEGIN$
        suite.addTestSuite(EchoStateTest.class);
        suite.addTestSuite(TcpIpLayerTest.class);
        suite.addTestSuite(ProtocolStateTest.class);
        suite.addTestSuite(ProtocolStackTest.class);
        suite.addTestSuite(ObjectTest.class);
        suite.addTestSuite(SessionIdTests.class);
        suite.addTestSuite(SessionTests.class);
        suite.addTestSuite(SessionNumberLayerTest.class);
        suite.addTestSuite(ProtocolLayerCompositeTest.class);
        suite.addTestSuite(SessionStarterLocalTest.class);
        suite.addTestSuite(SessionStarterTest.class);
        suite.addTestSuite(SessionStarterUDPTest.class);
        suite.addTestSuite(UdpTests.class);
        //$JUnit-END$
        return suite;
    }
}
