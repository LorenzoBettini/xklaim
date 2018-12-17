/*
 * Created on Feb 24, 2005
 */
package org.mikado.imc.protocols;

import java.io.IOException;
import java.io.Serializable;

import org.mikado.imc.common.MigrationUnsupported;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.MigratingPacket;

/**
 * Contains a Marshaler and UnMarshaler. These are accessible and modifiable as
 * public fields.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class TransmissionChannel {
    /**
     * The marshaler of this channel.
     */
    public Marshaler marshaler = null;

    /**
     * The unmarshaler of this channel.
     */
    public UnMarshaler unMarshaler = null;

    /**
     * Constructs a TransmissionChannel.
     * 
     * @param marshaler
     * @param unMarshaler
     */
    public TransmissionChannel(Marshaler marshaler, UnMarshaler unMarshaler) {
        this.marshaler = marshaler;
        this.unMarshaler = unMarshaler;
    }

    /**
     * @param marshaler
     */
    public TransmissionChannel(Marshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @param unMarshaler
     */
    public TransmissionChannel(UnMarshaler unMarshaler) {
        this.unMarshaler = unMarshaler;
    }

    /**
     * Constructs an empty TransmissionChannel
     */
    public TransmissionChannel() {

    }

    /**
     * @param b
     * @throws java.io.IOException
     */
    public void write(byte[] b) throws IOException {
        marshaler.write(b);
    }

    /**
     * @param b
     * @param off
     * @param len
     * @throws java.io.IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        marshaler.write(b, off, len);
    }

    /**
     * @param b
     * @throws java.io.IOException
     */
    public void write(int b) throws IOException {
        marshaler.write(b);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeBoolean(boolean v) throws IOException {
        marshaler.writeBoolean(v);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeByte(int v) throws IOException {
        marshaler.writeByte(v);
    }

    /**
     * @param s
     * @throws java.io.IOException
     */
    public void writeBytes(String s) throws IOException {
        marshaler.writeBytes(s);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeChar(int v) throws IOException {
        marshaler.writeChar(v);
    }

    /**
     * @param s
     * @throws java.io.IOException
     */
    public void writeChars(String s) throws IOException {
        marshaler.writeChars(s);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeDouble(double v) throws IOException {
        marshaler.writeDouble(v);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeFloat(float v) throws IOException {
        marshaler.writeFloat(v);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeInt(int v) throws IOException {
        marshaler.writeInt(v);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeLong(long v) throws IOException {
        marshaler.writeLong(v);
    }

    /**
     * @param code
     * @throws IOException
     * @throws MigrationUnsupported
     */
    public void writeMigratingCode(MigratingCode code) throws IOException,
            MigrationUnsupported {
        marshaler.writeMigratingCode(code);
    }

    /**
     * @param packet
     * @throws IOException
     */
    public void writeMigratingPacket(MigratingPacket packet) throws IOException {
        marshaler.writeMigratingPacket(packet);
    }

    /**
     * @param o
     * @throws IOException
     */
    public void writeReference(Serializable o) throws IOException {
        marshaler.writeReference(o);
    }

    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeShort(int v) throws IOException {
        marshaler.writeShort(v);
    }

    /**
     * @param s
     * @throws IOException
     */
    public void writeStringLine(String s) throws IOException {
        marshaler.writeStringLine(s);
    }

    /**
     * @param str
     * @throws java.io.IOException
     */
    public void writeUTF(String str) throws IOException {
        marshaler.writeUTF(str);
    }

    /**
     * @return the read boolean
     * @throws java.io.IOException
     */
    public boolean readBoolean() throws IOException {
        return unMarshaler.readBoolean();
    }

    /**
     * @return the read byte
     * @throws java.io.IOException
     */
    public byte readByte() throws IOException {
        return unMarshaler.readByte();
    }

    /**
     * @return the read char
     * @throws java.io.IOException
     */
    public char readChar() throws IOException {
        return unMarshaler.readChar();
    }

    /**
     * @return the read double
     * @throws java.io.IOException
     */
    public double readDouble() throws IOException {
        return unMarshaler.readDouble();
    }

    /**
     * @return the read float
     * @throws java.io.IOException
     */
    public float readFloat() throws IOException {
        return unMarshaler.readFloat();
    }

    /**
     * @param b
     * @throws java.io.IOException
     */
    public void readFully(byte[] b) throws IOException {
        unMarshaler.readFully(b);
    }

    /**
     * @param b
     * @param off
     * @param len
     * @throws java.io.IOException
     */
    public void readFully(byte[] b, int off, int len) throws IOException {
        unMarshaler.readFully(b, off, len);
    }

    /**
     * @return the read int
     * @throws java.io.IOException
     */
    public int readInt() throws IOException {
        return unMarshaler.readInt();
    }

    /**
     * @return the read String
     * @throws java.io.IOException
     */
    public String readLine() throws IOException {
        return unMarshaler.readLine();
    }

    /**
     * @return the read long
     * @throws java.io.IOException
     */
    public long readLong() throws IOException {
        return unMarshaler.readLong();
    }

    /**
     * @return the read MigratingCode
     * @throws IOException
     * @throws MigrationUnsupported
     */
    public MigratingCode readMigratingCode() throws IOException,
            MigrationUnsupported {
        return unMarshaler.readMigratingCode();
    }

    /**
     * @return the read MigratingPacket
     * @throws IOException
     */
    public MigratingPacket readMigratingPacket() throws IOException {
        return unMarshaler.readMigratingPacket();
    }

    /**
     * @return the read Object
     * @throws IOException
     */
    public Object readReference() throws IOException {
        return unMarshaler.readReference();
    }

    /**
     * @return the read short
     * @throws java.io.IOException
     */
    public short readShort() throws IOException {
        return unMarshaler.readShort();
    }

    /**
     * @return the read String
     * @throws IOException
     */
    public String readStringLine() throws IOException {
        return unMarshaler.readStringLine();
    }

    /**
     * @return the read int
     * @throws java.io.IOException
     */
    public int readUnsignedByte() throws IOException {
        return unMarshaler.readUnsignedByte();
    }

    /**
     * @return the read int
     * @throws java.io.IOException
     */
    public int readUnsignedShort() throws IOException {
        return unMarshaler.readUnsignedShort();
    }

    /**
     * @return the read String
     * @throws java.io.IOException
     */
    public String readUTF() throws IOException {
        return unMarshaler.readUTF();
    }

    /**
     * @see java.io.DataInput#skipBytes(int)
     */
    public int skipBytes(int n) throws IOException {
        return unMarshaler.skipBytes(n);
    }
}
