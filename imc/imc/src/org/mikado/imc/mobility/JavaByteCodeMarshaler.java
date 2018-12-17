package org.mikado.imc.mobility;

import java.io.IOException;

/**
 * An implementation of MigratingCodeMarshaler suitable for Java byte code class
 * loading, starting from a JavaMigratingCode and returning a
 * JavaMigratingPacket.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class JavaByteCodeMarshaler implements MigratingCodeMarshaler {

    /**
     * @see org.mikado.imc.mobility.MigratingCodeMarshaler#marshal(org.mikado.imc.mobility.MigratingCode)
     */
    public MigratingPacket marshal(MigratingCode code) throws IOException {
        return ((JavaMigratingCode) code).make_packet();
    }
}
