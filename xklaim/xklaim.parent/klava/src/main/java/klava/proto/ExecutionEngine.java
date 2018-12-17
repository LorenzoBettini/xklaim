/*
 * Created on Jan 11, 2006
 */
package klava.proto;

import klava.KlavaException;
import klava.topology.KlavaProcess;

/**
 * Represents an execution engine for processes
 * 
 * @author Lorenzo Bettini
 */
public interface ExecutionEngine {
    /**
     * Executes the passed process into this execution engine.
     * 
     * @param klavaProcess
     * @throws KlavaException
     */
    void runProcess(KlavaProcess klavaProcess) throws KlavaException;
    
    /**
     * Whether this engine should run also remote processes.
     * 
     * @param accept
     */
    void acceptRemoteProcesses(boolean accept);
}
