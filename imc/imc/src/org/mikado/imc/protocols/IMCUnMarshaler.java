/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.mobility.MigratingCodeUnMarshaler;
import org.mikado.imc.mobility.MigratingPacket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * The default implementation of UnMarshaler. An UnMarshaler is used by
 * ProtocolLayers to read from a generic data source. This default
 * implementation is based on DataInputStream, and reimplements the readLine
 * method as readStringLine, which is deprecated in the default DataInputStream
 * Java class.
 * 
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class IMCUnMarshaler extends DataInputStream implements UnMarshaler {
	/**
	 * The factory used to create marshalers and unmarshalers for migrating
	 * code.
	 */
	protected MigratingCodeFactory migratingCodeFactory;

	/**
	 * Creates an unmarshaler that uses the specified underlying InputStream. It
	 * automatically builds a BufferedInputStream on the passed stream.
	 * 
	 * @param in
	 */
	public IMCUnMarshaler(InputStream in) {
		super(new BufferedInputStream(in));
	}

	/**
	 * Creates an unmarshaler that uses the specified underlying InputStream. It
	 * automatically builds a BufferedInputStream on the passed stream. It also
	 * sets the factory to create MigratingCodeMarshaler and
	 * MigratingCodeUnMarshaler.
	 * 
	 * @param in
	 * @param migratingCodeFactory
	 *            TODO
	 */
	public IMCUnMarshaler(InputStream in,
			MigratingCodeFactory migratingCodeFactory) {
		super(new BufferedInputStream(in));
		this.migratingCodeFactory = migratingCodeFactory;
	}

	/**
	 * Reads a string line (terminated by a \n or \r\n).
	 * 
	 * @return The read line, without the terminating newline character(s)
	 * 
	 * @throws IOException
	 */
	public String readStringLine() throws IOException {
		StringBuffer buffer = new StringBuffer();

		byte b;

		while (true) {
			b = readByte();

			/*
			 * System.out.println("read byte: " + (
			 * Character.isLetterOrDigit((int)b) ? (char)b : b));
			 * System.out.println("available bytes: " + available());
			 */
			if ((b == '\n')) {
				break;
			}

			if (b != '\r') {
				buffer.append((char) b);
			}
		}

		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mikado.imc.protocols.UnMarshaler#clear()
	 */
	public void clear() throws IOException {
		skip(available());
	}

	/**
	 * Reads an object using Java serialization.
	 * 
	 * @return The object read.
	 * 
	 * @throws IOException
	 */
	public Object readReference() throws IOException {
		ObjectInputStream is = new ObjectInputStream(this);

		try {
			return is.readObject();
		} catch (ClassNotFoundException e1) {
			throw new IOException(e1.getMessage());
		}
	}

	/**
	 * Reads an object that has migrated. This object should contain all the
	 * code that it needs to execute on this site.
	 * 
	 * @return The object that has arrived at this site.
	 * 
	 * @throws IOException
	 * @throws MigrationUnsupported
	 *             DOCUMENT ME!
	 * 
	 * @see org.mikado.imc.mobility.MigratingCode
	 */
	public MigratingCode readMigratingCode() throws IOException,
			MigrationUnsupported {
		MigratingPacket packet = readMigratingPacket();

		if (migratingCodeFactory == null)
			throw new MigrationUnsupported("MigratingCodeFactory is not set");

		MigratingCodeUnMarshaler unMarshaler = migratingCodeFactory
				.createMigratingCodeUnMarshaler();

		try {
			return unMarshaler.unmarshal(packet);
		} catch (InstantiationException e) {
			throw new MigrationUnsupported(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new MigrationUnsupported(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new MigrationUnsupported(e.getMessage(), e);
		} catch (IOException e) {
			throw new MigrationUnsupported(e.getMessage(), e);
		}
	}

	/**
	 * Reads a migrating packet. Typically this method will not be called
	 * directly, but implicitly by readMigratingCode. This implementation
	 * simply relies on readReference.
	 * 
	 * @return The migrating packet read.
	 * 
	 * @throws IOException
	 */
	public MigratingPacket readMigratingPacket() throws IOException {
		return (MigratingPacket) readReference();
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
	 * Sets the factory used to create marshalers and unmarshalers for migrating
	 * code.
	 * 
	 * @param migratingCodeFactory
	 *            The factory used to create marshalers and unmarshalers for
	 *            migrating code.
	 */
	public final void setMigratingCodeFactory(
			MigratingCodeFactory migratingCodeFactory) {
		this.migratingCodeFactory = migratingCodeFactory;
	}
}
