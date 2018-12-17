/*
 * Created on Nov 11, 2005
 */
package org.mikado.imc.protocols.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;

/**
 * A pair of ProtocolLayers in pipe. What is written in the first layer is
 * available for reading from the other layer, and viceversa.
 * 
 * As suggesed in the Java documentation for piped stream: <i>"Attempting to use
 * both objects from a single thread is not recommended, as it may deadlock the
 * thread."</i>
 * 
 * This can be for testing purposes, including at least two threads.
 * 
 * It is also used by LocalSessionStarter, that creates a session using local
 * pipes.
 * 
 * Notice that for the implementation of pipes we did NOT use PipedInputStream
 * and PipedOutputStream from java.io, since if the thread that writes into the
 * pipe dies, the other thread receives a broken pipe exception, even if the
 * pipe could still be used (see <a
 * href=http://forum.java.sun.com/thread.jspa?threadID=606530&amp;messageID=3296245>this
 * discussion</a> and
 * <a href=http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4028322>this bug report</a>).
 * Since IMC is thought to be used with many threads this cannot be acceptable,
 * especially when dealing with sessions.
 * 
 * Thus we resorted java.nio.Pipe, which seems to work fine.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ProtocolLayerPipe {
    private ProtocolLayer protocolLayer1;

    private ProtocolLayer protocolLayer2;

    /**
     * @throws IOException
     * 
     */
    public ProtocolLayerPipe() throws IOException {
        Pipe pipe1 = Pipe.open();
        Pipe pipe2 = Pipe.open();

        OutputStream pipedOutputStream1 = Channels
                .newOutputStream(pipe1.sink());
        OutputStream pipedOutputStream2 = Channels
                .newOutputStream(pipe2.sink());
        InputStream pipedInputStream1 = Channels.newInputStream(pipe2.source());
        InputStream pipedInputStream2 = Channels.newInputStream(pipe1.source());

        protocolLayer1 = new ProtocolLayerEndPoint(new IMCUnMarshaler(
                pipedInputStream1), new IMCMarshaler(pipedOutputStream1));
        protocolLayer2 = new ProtocolLayerEndPoint(new IMCUnMarshaler(
                pipedInputStream2), new IMCMarshaler(pipedOutputStream2));
    }

    /**
     * @return Returns the protocolLayer1.
     */
    public final ProtocolLayer getProtocolLayer1() {
        return protocolLayer1;
    }

    /**
     * @return Returns the protocolLayer2.
     */
    public final ProtocolLayer getProtocolLayer2() {
        return protocolLayer2;
    }

}
