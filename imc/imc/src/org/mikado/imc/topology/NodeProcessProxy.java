/*
 * Created on Jan 29, 2005
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.SessionId;

/**
 * A proxy for an actual node. This is used to restrict node accessibility to
 * node processes.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public class NodeProcessProxy {
    /** The wrapped node. */
    protected Node node;

    /**
     * Creates a new NodeProcessProxy object.
     * 
     * @param node
     */
    public NodeProcessProxy(Node node) {
        this.node = node;
    }

    /**
     * @see org.mikado.imc.topology.Node#getNodeStack(NodeLocation)
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return node.getNodeStack(nodeLocation);
    }

    /**
     * @return the EventManager
     */
    public EventManager getEventManager() {
        return node.getEventManager();
    }

    /**
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        node.setEventManager(eventManager);
    }

    /**
     * @param nodeProcess
     * @throws IMCException
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        node.addNodeProcess(nodeProcess);
    }

    /**
     * @param nodeProcess
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        node.executeNodeProcess(nodeProcess);
    }

    /**
     * @see org.mikado.imc.topology.Node#isLocal(NodeLocation)
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return node.isLocal(nodeLocation);
    }

    /**
     * @see org.mikado.imc.topology.Node#removeNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    void removeNodeProcess(NodeProcess nodeProcess) {
        node.removeNodeProcess(nodeProcess);
    }

    /**
     * @see org.mikado.imc.topology.Node#SystemErrPrint(java.lang.String)
     */
    public void SystemErrPrint(String s) {
        node.SystemErrPrint(s);
    }

    /**
     * @see org.mikado.imc.topology.Node#SystemOutPrint(java.lang.String)
     */
    public void SystemOutPrint(String s) {
        node.SystemOutPrint(s);
    }

    /**
     * @see org.mikado.imc.topology.Node#closeSessions(org.mikado.imc.protocols.SessionId)
     */
    public void closeSessions(SessionId sessionId) {
        node.closeSessions(sessionId);
    }
}
