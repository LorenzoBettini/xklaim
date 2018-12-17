/*
 * Created on Jan 7, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

/**
 * A specialization of ProtocolLayer that is intended to be used as the last
 * layer of a protocol: it actually flush the output stream.
 * 
 * It relies on an UnMarshaler and Marshaler that are passed at the constructor.
 * 
 * @author $author$
 * @version $Revision: 1.4 $
 */
public class ProtocolLayerEndPoint extends ProtocolLayer {
    /** the unmarshaler to read from */
    protected UnMarshaler unmarshaler;

    /** the marshaler to write to */
    protected Marshaler marshaler;

    /**
     * Construct a Protocol Layer
     * 
     * @param unmarshaler
     * @param marshaler
     */
    public ProtocolLayerEndPoint(UnMarshaler unmarshaler, Marshaler marshaler) {
        this.unmarshaler = unmarshaler;
        this.marshaler = marshaler;
    }

    /**
     * Creates a new ProtocolLayerEndPoint object.
     */
    public ProtocolLayerEndPoint() {
    }

    /**
     * Simply returns the unmarshaler.
     * 
     * @param unMarshaler
     *            ignored
     * 
     * @return the previously set unmarshaler
     * 
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        return getUnmarshaler();
    }

    /**
     * Simply returns the marshaler.
     * 
     * @param marshaler
     *            ignored
     * 
     * @return the previously set marshaler
     * 
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        return getMarshaler();
    }

    /**
     * Simply closes the marshaler and unmarshaler
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doClose()
     */
    @Override
    public void doClose() throws ProtocolException {
        try {
            marshaler.close();
            unmarshaler.close();
        } catch (IOException e) {
            // ignore it
        }
    }

    /**
     * get/set method
     * 
     * @return Returns the marshaler.
     */
    public Marshaler getMarshaler() {
        return marshaler;
    }

    /**
     * get/set method
     * 
     * @param marshaler
     *            The marshaler to set.
     */
    public void setMarshaler(Marshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * get/set method
     * 
     * @return Returns the unmarshaler.
     */
    public UnMarshaler getUnmarshaler() {
        return unmarshaler;
    }

    /**
     * get/set method
     * 
     * @param unmarshaler
     *            The unmarshaler to set.
     */
    public void setUnmarshaler(UnMarshaler unmarshaler) {
        this.unmarshaler = unmarshaler;
    }
}
