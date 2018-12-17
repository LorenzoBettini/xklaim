/*
 * Created on Jan 7, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;

/**
 * Represent a protocol stack layer manager.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.8 $
 */
public abstract class ProtocolLayer {
    /** The stack this layer belongs to */
    protected ProtocolStack protocolStack;

    /**
     * Constructs a ProtocolLayer
     */
    public ProtocolLayer() {
    }

    /**
     * Actual implementation of creating/modifying an UnMarshaler in this
     * ProtocolLayer. In a specialized ProtocolLayer this method should remove
     * some header information from the passed UnMarshaler and return a possibly
     * modified UnMarshaler.
     * 
     * The default implementation is simply to return the passed UnMarshaler.
     * 
     * @param unMarshaler
     *            The UnMarshaler returned by the next layer in the stack
     * 
     * @return The new UnMarshaler
     * 
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        return unMarshaler;
    }
    
    /**
     * Actual implementation of releasing an UnMarshaler in this layer. In a
     * specialized ProtocolLayer this method should remove some header information
     * from the passed UnMarshaler and releasing the Marshaler.
     * 
     * The default is empty.
     * 
     * @param unMarshaler
     *            The UnMarshaler.
     * 
     * @throws ProtocolException
     */
    public void doReleaseUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
    }

    /**
     * Actual implementation of releasing a Marshaler in this layer. In a
     * specialized ProtocolLayer this method should add some footer information
     * in the passed Marshaler and releasing the Marshaler (e.g., flushing it).
     * 
     * The default is to simply flush the Marshaler.
     * 
     * @param marshaler
     *            The Marshaler to flush.
     * 
     * @throws ProtocolException
     */
    public void doReleaseMarshaler(Marshaler marshaler)
            throws ProtocolException {
        try {
            marshaler.flush();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Actual implementation of creating/modifying a Marshaler in this
     * ProtocolLayer. In a specialized ProtocolLayer this method should add some
     * header information in the passed Marshaler and return a possibly modified
     * Marshaler.
     * 
     * The default implementation is simply to return the passed Marshaler.
     * 
     * @param marshaler
     *            The Marshaler returned by the next layer in the stack.
     * 
     * @return The new Marshaler
     * 
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        return marshaler;
    }

    /**
     * This method should be redefined if the specific protocol layer must
     * perform closing operations. The default implementation does nothing. <br>
     * This method will be invoked during the close() method call chain. Keep in
     * mind that, if during the close method call chain, which starts from the
     * highest layer, a ProtocolException is raised, then the call chain will
     * stop.
     * 
     * @throws ProtocolException
     */
    public void doClose() throws ProtocolException {
    }

    /**
     * @return Returns the protocolStack.
     */
    public final ProtocolStack getProtocolStack() {
        return protocolStack;
    }

    /**
     * @param protocolStack
     *            The protocolStack to set.
     * @throws ProtocolException 
     */
    public void setProtocolStack(ProtocolStack protocolStack) throws ProtocolException {
        this.protocolStack = protocolStack;
    }

    /**
     * Creates an UnMarshaler by calling createUnMarshaler on the underlying
     * ProtocolStack.
     * 
     * @return the created UnMarshaler
     * @throws ProtocolException
     */
    protected UnMarshaler createUnMarshaler() throws ProtocolException {
        return protocolStack.createUnMarshaler(this);
    }

    /**
     * Creates a Marshaler by calling createMarshaler on the underlying
     * ProtocolStack.
     * 
     * @return the created Marshaler
     * @throws ProtocolException
     */
    protected Marshaler createMarshaler() throws ProtocolException {
        return protocolStack.createMarshaler(this);
    }

    /**
     * Flushes and release the Marshaler by calling releaseMarshaler on the
     * underlying stack.
     * 
     * @param marshaler
     * @throws ProtocolException
     */
    protected void releaseMarshaler(Marshaler marshaler)
            throws ProtocolException {
        protocolStack.releaseMarshaler(marshaler, this);
    }
    
    /**
     * Flushes and release the UnMarshaler by calling releaseUnMarshaler on the
     * underlying stack.
     * 
     * @param unMarshaler
     * @throws ProtocolException
     */
    protected void releaseUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        protocolStack.releaseUnMarshaler(unMarshaler, this);
    }
}
