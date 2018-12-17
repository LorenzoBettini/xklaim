/*
 * Created on Jan 25, 2006
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.NodeEvent;

/**
 * Contains references to nodes in an application.
 * 
 * The nodes that are inserted here are supposed to remove themselves from the
 * collection when they finished the execution.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodeContainer extends CloseableContainer<Node> {
    /**
     * @param element
     */
    @Override
    protected void doAddElement(Node element) {
        elements.put(element.getNodeName(), element);
        generate(NodeEvent.NodeEventId, new NodeEvent(this,
                NodeEvent.EventType.ADDED, element));
    }

    /**
     * @param element
     */
    @Override
    protected void doRemoveElement(Node element) {
        elements.remove(element.getNodeName());
        generate(NodeEvent.NodeEventId, new NodeEvent(this,
                NodeEvent.EventType.REMOVED, element));
    }

    /**
     * @param node
     * @throws IMCException
     */
    @Override
    protected void doCloseElement(Node node) throws IMCException {
        try {
            node.close();
        } finally {
            generate(NodeEvent.NodeEventId, new NodeEvent(this,
                    NodeEvent.EventType.REMOVED, node));
        }

    }
}
