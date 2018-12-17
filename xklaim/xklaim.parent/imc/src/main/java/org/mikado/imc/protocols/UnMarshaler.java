/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.MigratingPacket;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;

/**
 * The generic interface for UnMarshaler. This will be used by ProtocolLayers to
 * read from a generic data source.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public interface UnMarshaler extends DataInput, Closeable, MigratingCodeHandler {
	/**
	 * Reads a string line, terminated by \n (or \r + \n). The terminating \n
	 * and \r should be removed.
	 * 
	 * @return the string line
	 * 
	 * @throws IOException
	 */
	String readStringLine() throws IOException;

	/**
	 * Reads an object.
	 * 
	 * @return The object read.
	 * 
	 * @throws IOException
	 */
	Object readReference() throws IOException;

	/**
	 * Reads an object that has migrated. This object should contain all the
	 * code that it needs to execute on this site.
	 * 
	 * @return The object that has arrived at this site.
	 * 
	 * @throws IOException
	 * 
	 * @see org.mikado.imc.mobility.MigratingCode
	 */
	MigratingCode readMigratingCode() throws IOException, MigrationUnsupported;

	/**
	 * Reads a migrating packet. Typically this method will not be called
	 * directly, but implicitly by readMigratingCode.
	 * 
	 * @return The migrating packet read.
	 * 
	 * @throws IOException
	 */
	MigratingPacket readMigratingPacket() throws IOException;

	/**
	 * Discards all the possibly remaining bytes from the input.
	 * 
	 * @throws IOException
	 */
	void clear() throws IOException;

	/**
	 * Returns the number of bytes that can be read (or skipped over) from this
	 * input stream without blocking by the next caller of a method for this
	 * input stream.
	 * 
	 * @return the number of bytes that can be read from this input stream
	 *         without blocking.
	 * @throws IOException
	 */
	int available() throws IOException;
    
    /**
     * @see java.io.InputStream#mark(int)
     */
    void mark(int readlimit);

    /**
     * @see java.io.InputStream#markSupported()
     */
    boolean markSupported();

    /**
     * @see java.io.InputStream#reset()
     */
    void reset() throws IOException;

    /**
     * @see java.io.InputStream#skip(long)
     */
    long skip(long n) throws IOException;
}
