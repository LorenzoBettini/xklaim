/*
 * Created on Oct 21, 2005
 */
package klava.proto;

import java.io.IOException;

import klava.KlavaException;
import klava.LogicalLocality;
import klava.LogicalLocalityResolver;
import klava.PhysicalLocality;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.ProtocolStateSimple;
import org.mikado.imc.protocols.TransmissionChannel;
import org.mikado.imc.protocols.UnMarshaler;
import org.mikado.imc.protocols.WrongStringProtocolException;


/**
 * Takes care of receiving a request for resolving a logical locality
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LocalityResolverState extends ProtocolStateSimple {
    /**
     * Actually takes care of resolving a logical locality into a physical
     * locality.
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.1 $
     */
    public class LocalityResolverThread extends Thread {
        /**
         * The (remote) requester process' name
         */
        String processName;

        /**
         * The logical locality to resolve
         */
        LogicalLocality logicalLocality;

        /**
         * @param locality
         * @param name
         */
        public LocalityResolverThread(LogicalLocality locality, String name) {
            logicalLocality = locality;
            processName = name;
        }

        public void run() {
            try {
                if (logicalLocalityResolver == null) {
                    // TODO what if it is null?!
                    ProtocolStack protocolStack = getProtocolStack();

                    Marshaler marshaler = protocolStack.createMarshaler();
                    ResponseState.sendResponseLocality(marshaler, null,
                            processName, false);
                    protocolStack.releaseMarshaler(marshaler);

                    return;
                }

                // TODO what if it is null
                ProtocolStack protocolStack = getProtocolStack();
                Marshaler marshaler = protocolStack.createMarshaler();

                try {
                    /* this may block */
                    PhysicalLocality physicalLocality = logicalLocalityResolver
                            .resolve(logicalLocality);

                    ResponseState.sendResponseLocality(marshaler,
                            physicalLocality, processName, true);
                } catch (KlavaException e) {
                    ResponseState.sendResponseLocality(marshaler, null,
                            processName, false);
                } finally {
                    protocolStack.releaseMarshaler(marshaler);
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } /* if we get exceptions we can't do much... */
        }
    }

    /**
     * The delegate for resolving a logical locality
     */
    LogicalLocalityResolver logicalLocalityResolver;

    public static final String RESOLVELOC_S = "RESOLVELOCALITY";

    /**
     * 
     */
    public LocalityResolverState(LogicalLocalityResolver logicalLocalityResolver) {
        this.logicalLocalityResolver = logicalLocalityResolver;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolStateSimple#enter(java.lang.Object,
     *      org.mikado.imc.protocols.TransmissionChannel)
     */
    @Override
    public void enter(Object param, TransmissionChannel transmissionChannel)
            throws ProtocolException {
        if (param != null && !param.equals(RESOLVELOC_S))
            throw new WrongStringProtocolException(RESOLVELOC_S, param
                    .toString());

        try {
            UnMarshaler unMarshaler = getUnMarshaler(transmissionChannel);
            String process = unMarshaler.readStringLine();
            if (!process.equals(TupleOpState.PROCESS_S))
                throw new WrongStringProtocolException(TupleOpState.PROCESS_S,
                        process);

            process = unMarshaler.readStringLine();
            LogicalLocality logicalLocality = new LogicalLocality(unMarshaler
                    .readStringLine());

            new LocalityResolverThread(logicalLocality, process).start();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Sends a request for resolving a logical locality using the passed stack.
     * 
     * @param marshaler
     * @param logicalLocality
     *            The logical locality to resolve
     * @param processName
     *            The name of the process sending the request
     * @throws ProtocolException
     * @throws IOException
     */
    public static void sendResolveLocality(Marshaler marshaler,
            LogicalLocality logicalLocality, String processName)
            throws ProtocolException, IOException {
        marshaler.writeStringLine(RESOLVELOC_S);
        marshaler.writeStringLine(TupleOpState.PROCESS_S);
        marshaler.writeStringLine(processName);
        marshaler.writeStringLine(logicalLocality.toString());
    }

    /**
     * @return Returns the logicalLocalityResolver.
     */
    public final LogicalLocalityResolver getLogicalLocalityResolver() {
        return logicalLocalityResolver;
    }

    /**
     * @param logicalLocalityResolver
     *            The logicalLocalityResolver to set.
     */
    public final void setLogicalLocalityResolver(
            LogicalLocalityResolver logicalLocalityResolver) {
        this.logicalLocalityResolver = logicalLocalityResolver;
    }
}
