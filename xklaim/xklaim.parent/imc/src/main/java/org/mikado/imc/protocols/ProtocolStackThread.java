/*
 * Created on Jan 9, 2005
 */
package org.mikado.imc.protocols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple thread that, given a ProtocolStack, continuosly performs a read
 * operation from the stack.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class ProtocolStackThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolStackThread.class);

    /** the protocol stack used to read */
    protected ProtocolStack protocolStack;

    /**
     * Creates a new ProtocolStackThread object.
     *
     * @param protocolStack 
     */
    public ProtocolStackThread(ProtocolStack protocolStack) {
        this(protocolStack, "ProtoclStackThread");
    }

    /**
     * Creates a new ProtocolStackThread object.
     *
     * @param protocolStack 
     * @param name
     */
    public ProtocolStackThread(ProtocolStack protocolStack, String name) {
        super(name);
        this.protocolStack = protocolStack;
    }

    /**
     * Continuosly performs read from the stack
     */
    public void run() {
        while (true) {
            try {
                protocolStack.createUnMarshaler();
            } catch (ProtocolException e) {
                LOGGER.error("protocol stack error", e);

                return;
            }
        }
    }
    
    public void close() throws ProtocolException {
        protocolStack.close();
    }
}
