/*
 * Created on Feb 23, 2006
 */
package org.mikado.imc.events;

import org.mikado.imc.topology.Node;

/**
 * An event representing the addition/removal of a node.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodeEvent extends AddRemoveEvent<Node> {
    public static final String NodeEventId = "NodeEvent";

    /**
     * @param source
     * @param nodeEventType
     * @param node
     */
    public NodeEvent(Object source, EventType nodeEventType, Node node) {
        super(source, nodeEventType, node);
    }
}
