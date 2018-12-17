/*
 * Created on Oct 24, 2005
 */
package org.mikado.imc.protocols;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * An end point of a ProtocolStack where I/O takes place in/from a buffer.
 * Everything that is written into this buffer is then available for reading.
 * Writing inserts something into the buffer and Reading removes from the same
 * buffer.
 * 
 * A call to prepare discards all the previous contents. A call to up returns an
 * UnMarshaler that reads from the contents written in the buffer. The call to
 * up also clears the buffer. Thus, two subsequent calls to up with no prepare
 * in the middle will deadlock.
 * 
 * Notice that this is thought to be used in tests when the same Thread wants to
 * write and read using the same buffer. It is not thought to be used by
 * concurrent threads. Moreover, when you read from this layer, be sure that
 * something has been written into it, otherwise you might get blocked forever.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class ProtocolLayerSharedBuffer extends ProtocolLayer {
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    /**
     * Clears the underlying buffer.
     * 
     * @param marshaler
     *            ignored
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateMarshaler(org.mikado.imc.protocols.Marshaler)
     */
    @Override
    public Marshaler doCreateMarshaler(Marshaler marshaler) throws ProtocolException {
        byteArrayOutputStream.reset();

        // TODO use a factory to create the Marshaler
        return new IMCMarshaler(byteArrayOutputStream);
    }

    /**
     * Creates an UnMarshaler using the contents written into the buffer. Be
     * sure that something has been written into the buffer otherwise you'll be
     * blocked.
     * 
     * @param unMarshaler
     *            ignored
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateUnMarshaler(org.mikado.imc.protocols.UnMarshaler)
     */
    @Override
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler) throws ProtocolException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray());
        byteArrayOutputStream.reset();

        return new IMCUnMarshaler(byteArrayInputStream);
    }

    /**
     * Returns a byte array representation of the current buffer.
     * 
     * @return byte array representation of the current buffer
     */
    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
    
    /**
     * Replaces the buffer contents with the passed one.
     * 
     * @param buffer
     */
    public void setBuffer(byte[] buffer) {
        byteArrayOutputStream.reset();
        byteArrayOutputStream.write(buffer, 0, buffer.length);
    }
}
