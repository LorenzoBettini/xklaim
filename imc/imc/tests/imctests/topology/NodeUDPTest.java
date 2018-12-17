package imctests.topology;


/**
 * Node tests using UDP instead of TCP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class NodeUDPTest extends NodeTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @see imctests.topology.NodeTest#getConnectionProtocolId()
     */
    @Override
    protected String getConnectionProtocolId() {
        return "udp";
    }

}
