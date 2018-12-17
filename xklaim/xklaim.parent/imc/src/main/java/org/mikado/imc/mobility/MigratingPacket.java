package org.mikado.imc.mobility;

/**
 * The abstract class representing a packet containing a migrating object
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class MigratingPacket implements java.io.Serializable {
    private static final long serialVersionUID = 3762817077920215864L;
	/** The serialized version of the object */
    byte[] obj_bytes;

    /**
     * Creates a new MigratingPacket object.
     *
     * @param b the bytes representing this packet
     */
    public MigratingPacket(byte[] b) {
        obj_bytes = b;
    }

    /**
     * Return the bytes contained in this packet
     *
     * @return the bytes contained in this packet
     */
    public byte[] getObjectBytes() {
        return obj_bytes;
    }
}
