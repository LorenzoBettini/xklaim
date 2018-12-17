package org.mikado.imc.mobility;

import java.io.IOException;


/**
 * The general interface for unmarshaling a MigratingPacket containing both the
 * serialized code and its code
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface MigratingCodeUnMarshaler {
    /**
     * Retrieve the object contained in the packet and possibly dynamically
     * load its code (contained in the packet itself)
     *
     * @param p The packet to unmarshal
     *
     * @return The unmarshalled object
     *
     * @throws ClassNotFoundException whether during the unmarshalling the code
     *         of the object or of some of its part cannot be found
     */
    MigratingCode unmarshal(MigratingPacket p)
        throws InstantiationException, IllegalAccessException, 
            ClassNotFoundException, IOException;
}
