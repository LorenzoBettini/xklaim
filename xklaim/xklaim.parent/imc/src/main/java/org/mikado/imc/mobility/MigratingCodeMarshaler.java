package org.mikado.imc.mobility;

import java.io.IOException;


/**
 * The general interface for marshaling a MigratingPacket containing both the
 * serialized code and its code
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface MigratingCodeMarshaler {
    /**
     * Create a MigratingPacket containing the object and possibly its code
     *
     * @param code the object to marshall into a MigratingPacket
     *
     * @return The MigratingPacket containing the object and possibly its code
     *
     * @throws IOException
     */
    MigratingPacket marshal(MigratingCode code) throws IOException;
}
