/*
 * Created on Feb 17, 2005
 */
package imctests.topology;

import org.mikado.imc.protocols.SessionId;

/**
 * The same as ConnectDisconnectStateTest but using Local pipes instead of TCP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class ConnectDisconnectStateLocalTest extends ConnectDisconnectStateTest {

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
    public ConnectDisconnectStateLocalTest() {
        connectionProtocolId = "pipe";
    }

    protected SessionId createSessionId(int port) {
        return new SessionId("pipe", "" + port);
    }
}
