package imctests.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.SessionId;

/**
 * Node tests using local pipes instead of TCP
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class NodeLocalTest extends NodeTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @see imctests.topology.NodeTest#createSessionId(int)
     */
    @Override
    protected SessionId createSessionId(int port) throws IMCException {
        return new SessionId("pipe", port + "");
    }
    
    /**
     * @see imctests.topology.NodeTest#getConnectionProtocolId()
     */
    @Override
    protected String getConnectionProtocolId() {
        return "pipe";
    }
}
