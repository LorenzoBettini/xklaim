/*
 * Created on Jan 8, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Reads a line and writes it to a specified OutputStream (by default
 * System.out).  This is basically a read-only protocol layer, since it never
 * writes anything in the protocol stack: it only reads something from the
 * protocol stack.
 *
 * @author $author$
 * @version $Revision: 1.4 $
 */
public class LinePrinterProtocolLayer extends ProtocolLayer {
    /** the output stream where we write the lines we read */
    protected OutputStream out;
    
    /**
     * A string to prepend to each line
     */
    protected String preprend = "";

    /**
     * Constructs a LinePrinterProtocolLayer it writes the read lines to the
     * standard output
     */
    public LinePrinterProtocolLayer() {
        this(System.out);
    }

    /**
     * Constructs a LinePrinterProtocolLayer
     *
     * @param out where to write the read lines
     */
    public LinePrinterProtocolLayer(OutputStream out) {
        this.out = out;
    }

    /**
     * Reads a line and writes it to the specified output stream
     *
     * @param unMarshaler
     *
     * @return the UnMarshaler after reading
     *
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
        throws ProtocolException {
        print("reading a line");

        try {
            String line = unMarshaler.readStringLine() + "\r\n";
            if (preprend.length() > 0) {
                out.write(preprend.getBytes());
            }
            out.write(line.getBytes());
            out.flush();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return unMarshaler;
    }

    /**
     * Print a string to standard output.
     *
     * @param s
     */
    void print(String s) {
        System.out.println(getClass().getSimpleName() + ": " + s);
    }

    /**
     * @return Returns the preprend.
     */
    public final String getPreprend() {
        return preprend;
    }
    
    /**
     * @param preprend The preprend to set.
     */
    public final void setPreprend(String preprend) {
        this.preprend = preprend;
    }
    
    /**
     * @return Returns the out.
     */
    public final OutputStream getOut() {
        return out;
    }
    
    /**
     * @param out The out to set.
     */
    public final void setOut(OutputStream out) {
        this.out = out;
    }
}
