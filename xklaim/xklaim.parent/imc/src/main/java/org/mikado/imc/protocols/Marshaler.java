/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.MigratingPacket;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;


/**
 * The generic interface for Marshaler. This will be used by ProtocolLayers to
 * write into a generic data output.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public interface Marshaler extends DataOutput, Closeable, Flushable, MigratingCodeHandler {
    /**
     * Writes a string terminated with the sequence CR + LF
     *
     * @param s The string to write
     *
     * @throws IOException
     */
    void writeStringLine(String s) throws IOException;

    /**
     * Writes an object.
     *
     * @param o The object to write
     *
     * @throws IOException
     */
    void writeReference(Serializable o) throws IOException;

    /**
     * Writes a migrating code object. Such object is intended to be sent
     * together with its code.
     *
     * @param code The migrating object.
     *
     * @throws IOException
     *
     * @see MigratingCode
     */
    void writeMigratingCode(MigratingCode code) throws IOException, MigrationUnsupported;

    /**
     * Writes a migrating packet (containing migrating code). Usually this
     * method will not be called directly: it will be called implicitly by
     * writeMigratingCode.
     *
     * @param packet The migrating packet.
     *
     * @throws IOException
     */
    void writeMigratingPacket(MigratingPacket packet) throws IOException;
}
