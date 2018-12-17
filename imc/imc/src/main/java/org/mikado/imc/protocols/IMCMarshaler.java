/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.mobility.MigratingCodeMarshaler;
import org.mikado.imc.mobility.MigratingPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * The default implementation of Marshaler.  An Marshaler is used by
 * ProtocolLayers to write into a generic data output.  This default
 * implementation is based on DataOutputStream
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class IMCMarshaler extends DataOutputStream implements Marshaler {
    /**
     * The factory used to create marshalers and unmarshalers for migrating
     * code.
     */
    protected MigratingCodeFactory migratingCodeFactory;

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream.
     *
     * @param out
     */
    public IMCMarshaler(OutputStream out) {
        super(out);
    }

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream.  It also sets the factory to create
     * MigratingCodeMarshaler and MigratingCodeUnMarshaler.
     *
     * @param out
     * @param migratingCodeFactory DOCUMENT ME!
     */
    public IMCMarshaler(OutputStream out,
        MigratingCodeFactory migratingCodeFactory) {
        super(out);
        this.migratingCodeFactory = migratingCodeFactory;
    }

    /* (non-Javadoc)
     * @see org.mikado.imc.protocols.apis.Marshaler#writeStringLine(java.lang.String)
     */
    public void writeStringLine(String s) throws IOException {
        writeBytes(s + "\r\n");
    }

    /**
     * Writes an object using Java serialization as a byte array.
     *
     * @param o The object to write.
     *
     * @throws IOException
     */
    public void writeReference(Serializable o) throws IOException {
        if (o == null) {
            throw new IOException("null reference");
        }

        // if we serialize it in a byte array first, we can catch possible
        // exception.  If we write directly into this stream it seems that if
        // a fatal exception is raised (e.g., NotSerializable exception) we are blocked.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(o);
        os.flush();

        byte[] bytes = out.toByteArray();
        write(bytes, 0, bytes.length);

        /*
        ObjectOutputStream os = new ObjectOutputStream(this);
        os.writeObject(o);
        os.flush();
        */
    }

    /**
     * Writes a migrating code object. Such object is intended to be sent
     * together with its code.
     *
     * @param code The migrating object.
     *
     * @throws IOException
     *
     * @see org.mikado.imc.mobility.MigratingCode
     */
    public void writeMigratingCode(MigratingCode code)
        throws IOException, MigrationUnsupported {
        if (migratingCodeFactory == null)
      		throw new MigrationUnsupported("MigratingCodeFactory is not set");
      	
      	MigratingCodeMarshaler marshaler = migratingCodeFactory.createMigratingCodeMarshaler();
  		MigratingPacket packet = marshaler.marshal(code);
		writeMigratingPacket(packet);		
    }

    /**
     * Writes a migrating packet (containing migrating code). Usually this
     * method will not be called directly: it will be called implicitly by
     * writeMigratingCode.  This implementation simply writes the
     * packet through writeReference.
     *
     * @param packet The migrating packet.
     *
     * @throws IOException
     */
    public void writeMigratingPacket(MigratingPacket packet)
        throws IOException {
        writeReference(packet);
    }

    /**
     * Returns the factory used to create marshalers and unmarshalers for
     * migrating code.
     *
     * @return The factory used to create marshalers and unmarshalers for
     *         migrating code.
     */
    public final MigratingCodeFactory getMigratingCodeFactory() {
        return migratingCodeFactory;
    }

    /**
     * Sets the factory used to create marshalers and unmarshalers for
     * migrating code.
     *
     * @param migratingCodeFactory The factory used to create marshalers and
     *        unmarshalers for migrating code.
     */
    public final void setMigratingCodeFactory(
        MigratingCodeFactory migratingCodeFactory) {
        this.migratingCodeFactory = migratingCodeFactory;
    }
}
