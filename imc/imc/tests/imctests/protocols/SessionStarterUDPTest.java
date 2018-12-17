/*
 * Created on Apr 14, 2005
 */
package imctests.protocols;

/**
 * Tests for SessionStarter.  This class uses UDP.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class SessionStarterUDPTest extends SessionStarterTest {

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
		super.tearDown();
		// Thread.sleep(2000);
	}

	/**
	 * Constructor for SessionStarterUDPTest.
	 * @param arg0
	 */
	public SessionStarterUDPTest() {
		connectionProtocolId = "udp";
	}

}
