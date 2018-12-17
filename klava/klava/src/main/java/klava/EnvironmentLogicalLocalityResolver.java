/*
 * Created on Nov 25, 2005
 */
package klava;

import java.io.IOException;
import java.util.Enumeration;

import klava.proto.LocalityResolverState;
import klava.proto.Response;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.topology.SessionManager;


/**
 * Uses the Environment to resolve LogicalLocalities. If it fails, it tries to
 * query all the nodes it is connected to (one after another).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class EnvironmentLogicalLocalityResolver implements
        LogicalLocalityResolver {
    /**
     * The Environment to resolve a LogicalLocality
     */
    protected Environment environment = null;

    /**
     * To request logical locality resolution if unable to solve it ourselves.
     */
    protected SessionManager sessionManager = null;

    /**
     * The table that associates process names to locality responses.
     */
    protected WaitingForResponse<Response<PhysicalLocality>> waitingForLocality = new WaitingForResponse<Response<PhysicalLocality>>();

    /**
     * @param environment
     * @param sessionManager
     * @param waitingForLocality
     */
    public EnvironmentLogicalLocalityResolver(Environment environment,
            SessionManager sessionManager,
            WaitingForResponse<Response<PhysicalLocality>> waitingForLocality) {
        this.environment = environment;
        this.sessionManager = sessionManager;
        this.waitingForLocality = waitingForLocality;
    }

    /**
     * Tries to translate a LogicalLocality into a PhysicalLocality by using the
     * local Environment. If it fails, it tries to query all the nodes it is
     * connected to (one after another). If it still fails it throws a
     * KlavaLogicalLocalityException.
     * 
     * @see klava.LogicalLocalityResolver#resolve(klava.LogicalLocality)
     */
    public PhysicalLocality resolve(LogicalLocality logicalLocality)
            throws KlavaException {
        PhysicalLocality physicalLocality = environment
                .toPhysical(logicalLocality);

        if (physicalLocality == null) {
            /* try to ask for resolution to nodes we are connected to */
            if (sessionManager != null) {
                Enumeration<ProtocolStack> stacks = sessionManager.getStacks();
                while (stacks.hasMoreElements()) {
                    ProtocolStack protocolStack = stacks.nextElement();

                    Marshaler marshaler;
                    try {
                        Response<PhysicalLocality> response = new Response<PhysicalLocality>();
                        /* register for response */
                        String processName = Thread.currentThread().getName();
                        waitingForLocality.put(processName, response);
                        marshaler = protocolStack.createMarshaler();
                        LocalityResolverState.sendResolveLocality(marshaler,
                                logicalLocality, processName);
                        protocolStack.releaseMarshaler(marshaler);

                        response.waitForResponse();

                        if (response.error == null) {
                            /* OK this node provided us with the answer! */
                            return response.responseContent;
                        }
                    } catch (ProtocolException e) {
                        throw new KlavaException(e);
                    } catch (IOException e) {
                        throw new KlavaException(e);
                    } catch (InterruptedException e) {
                        throw new KlavaException(e);
                    }
                }
            }

            /* if we get here we failed in the translation */
            throw new KlavaLogicalLocalityException(logicalLocality);
        }

        return physicalLocality;
    }

}
