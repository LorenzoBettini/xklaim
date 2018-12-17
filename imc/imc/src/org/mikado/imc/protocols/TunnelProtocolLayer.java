/*
 * Created on Jan 10, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * <p>
 * This is a specialized ProtocolLayer that should be used to tunnel a protocol
 * layer into another one.
 * </p>
 * 
 * <p>
 * For instance, if you have a ProtocolLayer p, you can tunnel p into a
 * TunnelProtocolLayer, which provides specific means to make the tunneling
 * transparent both to the protocol layer and to the programmer.  If you have
 * a TunnelProtocolLayer t, you can tunnel p through t, by simply calling:
 * <pre>
 * p.insertNext(t)
 * </pre>
 * 
 * </p>
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class TunnelProtocolLayer extends ProtocolLayer {
    /** What we write into this marshaler will be read by the tunneled layer */
    protected Marshaler tunneledMarshaler;

    /** What the tunneled layer writes, will be read through this unmarshaler */
    protected UnMarshaler tunneledUnMarshaler;

    /** The unmarshaler we should return in doUp */
    protected UnMarshaler newUnMarshaler;

    /** The marshaler we should return in doPrepare */
    protected Marshaler newMarshaler;

    /**
     * Constucts a TunnelProtocolLayer
     *
     * @throws ProtocolException
     */
    public TunnelProtocolLayer() throws ProtocolException {
        initTunnels();
    }

    /**
     * Initializes the tunneled marshalers and unmarshalers
     *
     * @throws ProtocolException
     */
    protected void initTunnels() throws ProtocolException {
        try {
            // TODO: do not hardcode IMCMarshaler and IMCUnMarshaler
            PipedInputStream pipe_in = new PipedInputStream();
            PipedOutputStream pipe_out = new PipedOutputStream();

            pipe_in.connect(pipe_out);

            newUnMarshaler = new IMCUnMarshaler(pipe_in);
            tunneledMarshaler = new IMCMarshaler(pipe_out);

            // now everything we write into tunneledMarshaler will
            // be read by the tunneled layer
            pipe_in = new PipedInputStream();
            pipe_out = new PipedOutputStream();

            pipe_in.connect(pipe_out);

            newMarshaler = new IMCMarshaler(pipe_out);
            tunneledUnMarshaler = new IMCUnMarshaler(pipe_in);

            // now everything the tunneled layer writes will be
            // read through tunneledUnMarshaler
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}
