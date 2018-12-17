/*
 * Created on Sep 26, 2005
 */
package klava.proto;

import java.io.Serializable;

import klava.PhysicalLocality;


/**
 * Generic packet exchanged among klava nodes.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class NodePacket implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Locality of the sender
     */
    public PhysicalLocality Source; 

    /**
     * Locality of the target
     */
    public PhysicalLocality Dest;
    
    /**
     * The name of the process issuing this packet
     */
    public String processName = new String();

    /**
     * @param dest
     * @param source
     */
    public NodePacket(PhysicalLocality dest, PhysicalLocality source) {
        Dest = dest;
        Source = source;
    }
    
    public NodePacket() {
        
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodePacket) {
            NodePacket nodePacket = (NodePacket) obj;
            return Source.equals(nodePacket.Source) &&
                Dest.equals(nodePacket.Dest) &&
                processName.equals(nodePacket.processName);
        }

        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PACKET FROM: " + Source + ", TO: " + Dest +
            (processName.length() > 0 ?
                    ", PROCESS: " + processName : "");
    }     
}
