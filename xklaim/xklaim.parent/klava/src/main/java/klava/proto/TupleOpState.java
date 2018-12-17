/*
 * Created on Sep 26, 2005
 */
package klava.proto;

import java.io.IOException;

import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;


/**
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class TupleOpState extends ProtocolStateSimple {
    public static final String OPERATION_S = "OPERATION";

    public static final String FROM_S = "FROM";

    public static final String TO_S = "TO";

    public static final String PROCESS_S = "PROCESS";

    public static final String BLOCKING_S = "BLOCKING";

    public static final String TIMEOUT_S = "TIMEOUT";

    /**
     * Whether this state is used for reading or writing
     */
    boolean doRead;

    /**
     * The TuplePacket on which this state operates.
     */
    TuplePacket tuplePacket = new TuplePacket();

    /**
     * The manager of a received TuplePacket.
     */
    TupleOpManager tupleOpManager = null;
    
    /**
     * This is used to dispatch and retrieve mobile code.
     * 
     * TODO is a static method really the best choice?
     */
    protected static MigratingCodeFactory migratingCodeFactory;
    
    /**
     * Sets the passed TupleOpManager. In this case this state will be used for
     * reading TuplePackets.
     * 
     * @param tupleOpManager
     */
    public TupleOpState(TupleOpManager tupleOpManager) {
        this.tupleOpManager = tupleOpManager;
        this.doRead = true;
    }

    /**
     * Constructs a TupleOpState specifying whether it is used for reading or
     * writing.
     * 
     * @param doRead
     */
    public TupleOpState(boolean doRead) {
        this.doRead = doRead;
    }

    /**
     * 
     */
    public TupleOpState() {
        super();
    }

    /**
     * @param next_state
     */
    public TupleOpState(String next_state) {
        super(next_state);
    }

    /**
     * On closing we make sure to close the TupleOpManager.
     * 
     * @see org.mikado.imc.protocols.ProtocolSwitchState#closed()
     */
    @Override
    public void closed() throws ProtocolException {
        super.closed();
        try {
            tupleOpManager.close();
        } catch (IMCException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        try {
            if (!doRead) {
                writePacket(transmissionChannel);
            } else {
                // make sure to use a different TuplePacket for each
                // read operation, otherwise we might overwrite a packet
                // not yet handled
                setTuplePacket(new TuplePacket());
                readPacket(param, transmissionChannel);

                if (tupleOpManager != null) {
                    SessionId ourSessionId = null;
                    
                    /* try to retrieve our session idenitifer */
                    ProtocolStack protocolStack = getProtocolStack();
                    if (protocolStack != null) {
                        Session session = protocolStack.getSession();
                        if (session == null)
                            throw new ProtocolException("null session");
                        
                        ourSessionId = session.getLocalEnd();
                    }
                    
                    tupleOpManager.handle(tuplePacket, ourSessionId);
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        } catch (KlavaMalformedPhyLocalityException e) {
            throw new ProtocolException(e);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Writes a TuplePacket by using the passed TransmissionChannel
     * 
     * @param transmissionChannel
     * @throws IOException
     * @throws ProtocolException
     */
    void writePacket(TransmissionChannel transmissionChannel)
            throws IOException, ProtocolException {
        Marshaler marshaler = getMarshaler(transmissionChannel);

        writePacket(marshaler, tuplePacket);
    }
    
    /**
     * Writes a TuplePacket by using the passed ProtocolStack.
     * It also releases the Marshaler created through this ProtocolStack.
     * 
     * @param protocolStack
     * @param tuplePacket The TuplePacket to write
     * @throws IOException
     * @throws ProtocolException
     */
    static public void writePacket(ProtocolStack protocolStack, TuplePacket tuplePacket)
            throws IOException, ProtocolException {
        Marshaler marshaler = protocolStack.createMarshaler();

        writePacket(marshaler, tuplePacket);
        
        protocolStack.releaseMarshaler(marshaler);
    }

    /**
     * Writes a TuplePacket by using the passed Marshaler
     * 
     * @param marshaler
     * @param tuplePacket The TuplePacket to write
     * @throws IOException
     * @throws ProtocolException
     */
    static void writePacket(Marshaler marshaler, TuplePacket tuplePacket)
            throws IOException, ProtocolException {
        marshaler.writeStringLine(OPERATION_S);
        marshaler.writeStringLine(tuplePacket.operation);

        marshaler.writeStringLine(FROM_S);
        marshaler.writeStringLine(tuplePacket.Source.toString());

        marshaler.writeStringLine(TO_S);
        marshaler.writeStringLine(tuplePacket.Dest.toString());

        marshaler.writeStringLine(PROCESS_S);
        marshaler.writeStringLine(tuplePacket.processName);

        marshaler.writeStringLine(BLOCKING_S);
        marshaler.writeStringLine("" + tuplePacket.blocking);

        marshaler.writeStringLine(TIMEOUT_S);
        marshaler.writeStringLine("" + tuplePacket.timeout);

        TupleState tupleState = new TupleState();
        tupleState.setDoRead(false);
        tupleState.setTuple(tuplePacket.tuple);
        tupleState.setMigratingCodeFactory(migratingCodeFactory);

        tupleState.enter(null, new TransmissionChannel(marshaler));
    }

    /**
     * Reads a TuplePacket from the passed TransmissionChannel
     * 
     * @param param
     *            The possible string already containing the header string,
     *            which should be OPERATION
     * @param transmissionChannel
     * 
     * @throws IOException
     * @throws ProtocolException
     * @throws KlavaMalformedPhyLocalityException
     */
    void readPacket(Object param, TransmissionChannel transmissionChannel)
            throws IOException, ProtocolException,
            KlavaMalformedPhyLocalityException {
        String op;
        UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);

        if (param != null)
            op = param.toString();
        else
            op = unMarshaler.readStringLine();

        if (!op.equals(OPERATION_S)) {
            throw new WrongStringProtocolException(OPERATION_S, op);
        }

        tuplePacket.operation = unMarshaler.readStringLine();

        String from = unMarshaler.readStringLine();
        if (!from.equals(FROM_S)) {
            throw new WrongStringProtocolException(FROM_S, from);
        }

        tuplePacket.Source = new PhysicalLocality(unMarshaler.readStringLine());

        String to = unMarshaler.readStringLine();
        if (!to.equals(TO_S)) {
            throw new WrongStringProtocolException(TO_S, to);
        }

        tuplePacket.Dest = new PhysicalLocality(unMarshaler.readStringLine());

        String process = unMarshaler.readStringLine();
        if (!process.equals(PROCESS_S)) {
            throw new WrongStringProtocolException(PROCESS_S, process);
        }

        tuplePacket.processName = unMarshaler.readStringLine();

        String blocking = unMarshaler.readStringLine();
        if (!blocking.equals(BLOCKING_S)) {
            throw new WrongStringProtocolException(BLOCKING_S, blocking);
        }

        tuplePacket.blocking = Boolean.parseBoolean(unMarshaler
                .readStringLine());

        String timeout = unMarshaler.readStringLine();
        if (!timeout.equals(TIMEOUT_S)) {
            throw new WrongStringProtocolException(TIMEOUT_S, timeout);
        }

        try {
            timeout = unMarshaler.readStringLine();
            tuplePacket.timeout = Long.parseLong(timeout);
        } catch (NumberFormatException e) {
            throw new ProtocolException(e.getMessage() + " " + timeout);
        }

        TupleState tupleState = new TupleState();
        tupleState.setDoRead(true);
        tupleState.setMigratingCodeFactory(migratingCodeFactory);

        tupleState.enter(null, transmissionChannel);

        tuplePacket.tuple = tupleState.getTuple();
    }

    /**
     * @return Returns the doRead.
     */
    public final boolean isDoRead() {
        return doRead;
    }

    /**
     * @param doRead
     *            The doRead to set.
     */
    public final void setDoRead(boolean doRead) {
        this.doRead = doRead;
    }

    /**
     * @return Returns the tuplePacket.
     */
    public final TuplePacket getTuplePacket() {
        return tuplePacket;
    }

    /**
     * @param tuplePacket
     *            The tuplePacket to set.
     */
    public final void setTuplePacket(TuplePacket tuplePacket) {
        this.tuplePacket = tuplePacket;
    }

    /**
     * @return Returns the tupleOpManager.
     */
    public final TupleOpManager getTupleOpManager() {
        return tupleOpManager;
    }

    /**
     * @param tupleOpManager
     *            The tupleOpManager to set.
     */
    public final void setTupleOpManager(TupleOpManager tupleOpManager) {
        this.tupleOpManager = tupleOpManager;
    }

    /**
     * @return Returns the migratingCodeFactory.
     */
    static MigratingCodeFactory getMigratingCodeFactory() {
        return migratingCodeFactory;
    }

    /**
     * @param migratingCodeFactory The migratingCodeFactory to set.
     */
    static void setMigratingCodeFactory(MigratingCodeFactory migratingCodeFactory) {
        TupleOpState.migratingCodeFactory = migratingCodeFactory;
    }
}
