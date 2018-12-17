/*
 * Created on Apr 13, 2006
 */
/**
 * 
 */
package org.mikado.imc.protocols;

import java.io.IOException;

/**
 * Keeps track of the session incremental number for input and output packets.
 * 
 * When the number is negative it means that we have to close the communication.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class SessionNumberLayer extends ProtocolLayer {

    /**
     * The incremental number for sent packets
     */
    protected int sendNumber = 0;

    /**
     * The incremental number for received packets
     */
    protected int recNumber = 0;

    /**
     * Removes an integer and checks it against the expected sequence number.
     * 
     * @throws ProtocolException
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateUnMarshaler(org.mikado.imc.protocols.UnMarshaler)
     */
    @Override
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        try {
            int sequence = unMarshaler.readInt();

            if (sequence < 0) {
                /* a negative number means close communication */
                System.err.println(getProtocolStack().getSession()
                        + ": received closing packet");

                /* in turn send the closing packet */
                doClose();

                /* and physically close communication */
                getProtocolStack().close();
                throw new ProtocolException("closed");
            }

            if (sequence != recNumber) {
                throw new ProtocolException("packet number not in sequence: "
                        + sequence + " instead of " + recNumber);
            }

            /* OK, just increment the number of the next expected packet */
            ++recNumber;
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return unMarshaler;
    }

    /**
     * Writes the next sequence number.
     * 
     * @throws ProtocolException
     * @see org.mikado.imc.protocols.ProtocolLayer#doCreateMarshaler(org.mikado.imc.protocols.Marshaler)
     */
    @Override
    public Marshaler doCreateMarshaler(Marshaler marshaler)
            throws ProtocolException {
        try {
            marshaler.writeInt(sendNumber++);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return marshaler;
    }

    /**
     * Sends a closing packet
     * 
     * @see org.mikado.imc.protocols.ProtocolLayer#doClose()
     */
@Override
    public void doClose() throws ProtocolException {
        /* sets the session number to negative (i.e., closing) */
        sendNumber = -1;

        /* this will actually send the closing packet */
        Marshaler marshaler = createMarshaler();
        try {
            marshaler.writeInt(-1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                releaseMarshaler(marshaler);

                Thread thread = new Thread() {
                    public void run() {
                        /* wait for the communication to be closed */
                        try {
                            createUnMarshaler();
                            System.err.print(getProtocolStack().getSession());
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } finally {
                            System.err.println(" received last packet");
                        }
                    }
                };
                thread.start();
                thread.join(1000);
            } catch (ProtocolException e) {
                // ignore it: by now we're closed
            } catch (InterruptedException e) {
                // ignore it: by now we're closed
            }
        }
    }
    /**
     * @return Returns the sendNumber.
     */
    public int getSendNumber() {
        return sendNumber;
    }

    /**
     * @param sendNumber
     *            The sendNumber to set.
     */
    public void setSendNumber(int sendNumber) {
        this.sendNumber = sendNumber;
    }

    /**
     * @return Returns the recNumber.
     */
    public int getRecNumber() {
        return recNumber;
    }

    /**
     * @param recNumber
     *            The recNumber to set.
     */
    public void setRecNumber(int recNumber) {
        this.recNumber = recNumber;
    }
}
