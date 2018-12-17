/*
 * Created on Apr 14, 2005
 */
package imctests.protocols;

import java.net.UnknownHostException;

import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;

/**
 * Tests for SessionStarter.  This class uses local pipes.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class SessionStarterLocalTest extends SessionStarterTest {

	public static void main(String[] args) {
	}

	/**
	 * @see SessionStarterTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * @see SessionStarterTest#tearDown()
	 */
	protected void tearDown() throws Exception {
        System.err.println("SessionStarterLocalTest::tearDown");
		super.tearDown();
		System.err.println("SessionStarterLocalTest::tearDown");
	}

	/**
	 * Constructor for SessionStarterUDPTest.
	 * @param arg0
	 */
	public SessionStarterLocalTest() {
		connectionProtocolId = "pipe";
	}

    protected SessionId createSessionId(int port) throws UnknownHostException {
        return new SessionId(connectionProtocolId, "" + port);
    }

    /**
     * This test makes no sense for local pipes, since if the port is not
     * specified the session ids would be the same
     * 
     * @see imctests.protocols.SessionStarterTest#testTwoBindForAcceptNullPort()
     */
    @Override
    public void testTwoBindForAcceptNullPort() throws UnknownHostException, ProtocolException, InterruptedException {
        
    }
    
    
}
