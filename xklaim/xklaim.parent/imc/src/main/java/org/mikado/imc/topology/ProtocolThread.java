/*
 * Created on Jan 12, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;

/**
 * A NodeProcess that executes a run of a protocol.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ProtocolThread extends NodeProcess {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /** The protocol this thread will execute. */
    protected Protocol protocol;

    /**
     * Creates a Thread with an associated protool.
     * 
     * @param protocol
     */
    public ProtocolThread(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Executes the protocol, if the protocol generates a
     * ProtocolException, it exits.
     * 
     * @see org.mikado.imc.topology.NodeProcess#execute()
     */
    @Override
    public void execute() throws IMCException {
        try {
            protocol.start();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } finally {
            try {
                protocol.close();
            } catch (ProtocolException e) {
                /* ignore it */
            }
        }
    }

    /**
     * Closes the protocol that is being executed.
     * 
     * @see org.mikado.imc.topology.NodeProcess#close()
     */
    @Override
    public void close() throws IMCException {
        super.close();
        
        if (protocol != null)
            protocol.close();
    }
}
