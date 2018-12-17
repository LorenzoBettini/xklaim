/*
 * Created on Feb 17, 2005
 */
package imctests.topology;

/**
 * The same as ConnectDisconnectStateTest but using UDP instead of TCP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ConnectDisconnectStateUDPTest extends ConnectDisconnectStateTest {

    /**
     * @see ConnectDisconnectStateTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @see ConnectDisconnectStateTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for ConnectDisconnectStateUDPTest.
     */
    public ConnectDisconnectStateUDPTest() {
        connectionProtocolId = "udp";
    }

}
