package org.mikado.imc.mobility;

import org.mikado.imc.log.MessagePrinters;

/**
 * Interface for receivers of code, i.e., that take care of
 * deserializing the code after configuring a NodeClassLoader
 *
 * @author $author$
 * @version $Revision: 1.4 $
 */
public interface MigratingCodeRecover {
    /**
     * recover an object (deserialize it and load its classes) from a
     * JavaMigratingPacket
     *
     * @return the recovered object
     *
     * @throws ClassNotFoundException
     * @throws java.io.IOException
     * @throws SecurityException
     */
    JavaMigratingCode recover()
        throws ClassNotFoundException, java.io.IOException, SecurityException;

    /**
     * set the packet where the object is stored
     *
     * @param pack the packet containing the object to deserialize
     */
    void set_packet(JavaMigratingPacket pack);

    /**
     * set the message printer for this MigratingCodeRecover
     *
     * @param p the message printer
     */
    void setMessagePrinters(MessagePrinters p);
}
