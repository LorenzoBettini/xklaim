/*
 * Created on Jan 11, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.proto.ExecutionEngine;

import org.mikado.imc.common.IMCException;


/**
 * Implementation of ExecutionEngine that simply uses a klava node as the
 * execution engine for processes.
 * 
 * @author Lorenzo Bettini
 */
public class NodeExecutionEngine implements ExecutionEngine {
    /**
     * The node that we use as the execution engine.
     */
    protected KlavaNode klavaNode;
    
    /**
     * Whether to accept remote processes (default is true).
     */
    protected boolean acceptRemoteProcesses = true;

    /**
     * @param klavaNode
     */
    public NodeExecutionEngine(KlavaNode klavaNode) {
        this.klavaNode = klavaNode;
    }

    /**
     * @see klava.proto.ExecutionEngine#runProcess(klava.topology.KlavaProcess)
     */
    public void runProcess(KlavaProcess klavaProcess) throws KlavaException {
        if (! acceptRemoteProcesses)
            throw new KlavaException("we don't accept remote processes");
        
        try {
            klavaNode.addNodeProcess(klavaProcess);
        } catch (IMCException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * @see klava.proto.ExecutionEngine#acceptRemoteProcesses(boolean)
     */
    public void acceptRemoteProcesses(boolean accept) {
        acceptRemoteProcesses = accept;
    }

}
