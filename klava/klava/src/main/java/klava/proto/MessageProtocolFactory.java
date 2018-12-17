/*
 * Created on Nov 11, 2005
 */
package klava.proto;

import klava.Environment;
import klava.LogicalLocalityResolver;
import klava.PhysicalLocality;
import klava.WaitingForResponse;

import org.mikado.imc.events.EventManager;
import org.mikado.imc.mobility.MigratingCodeFactory;
import org.mikado.imc.protocols.Protocol;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManagers;

/**
 * Generates the message Protocol
 * 
 * @author Lorenzo Bettini
 */
public class MessageProtocolFactory implements
        org.mikado.imc.protocols.ProtocolFactory {
    /**
     * The table that associates process names to generic responses.
     */
    WaitingForResponse<Response<String>> waitingForResponse = new WaitingForResponse<Response<String>>();

    /**
     * The table that associates process names to tuple responses.
     */
    WaitingForResponse<TupleResponse> waitingForTuple = new WaitingForResponse<TupleResponse>();

    /**
     * The table that associates process names to locality responses.
     */
    WaitingForResponse<Response<PhysicalLocality>> waitingForLocality = new WaitingForResponse<Response<PhysicalLocality>>();

    /**
     * The factory of managers of a received TuplePacket.
     */
    TupleOpManagerFactory tupleOpManagerFactory = null;

    /**
     * The RoutingTable
     */
    RoutingTable routingTable = null;

    /**
     * The Environment used to keep track of locality mappings
     */
    Environment environment = null;

    /**
     * The EventManager that will be se in the generated Protocol
     */
    EventManager eventManager = null;

    /**
     * The SessionManager containing the established sessions, i.e., those
     * created by a login.
     */
    SessionManagers sessionManagers = null;

    /**
     * The resolver of LogicalLocalities.
     */
    LogicalLocalityResolver logicalLocalityResolver;

    /**
     * This is used to dispatch and retrieve mobile code.
     */
    MigratingCodeFactory migratingCodeFactory;

    /**
     * 
     */
    public MessageProtocolFactory() {
    }

    /**
     * @param waitingForResponse
     * @param waitingForTuple
     * @param waitingForLocality
     * @param tupleOpManagerFactory
     * @param routingTable
     * @param environment
     * @param eventManager
     * @param sessionManagers
     * @param migratingCodeFactory
     */
    public MessageProtocolFactory(
            WaitingForResponse<Response<String>> waitingForResponse,
            WaitingForResponse<TupleResponse> waitingForTuple,
            WaitingForResponse<Response<PhysicalLocality>> waitingForLocality,
            TupleOpManagerFactory tupleOpManagerFactory,
            RoutingTable routingTable, Environment environment,
            EventManager eventManager, SessionManagers sessionManagers,
            LogicalLocalityResolver logicalLocalityResolver,
            MigratingCodeFactory migratingCodeFactory) {
        this.waitingForResponse = waitingForResponse;
        this.waitingForTuple = waitingForTuple;
        this.waitingForLocality = waitingForLocality;
        this.tupleOpManagerFactory = tupleOpManagerFactory;
        this.routingTable = routingTable;
        this.environment = environment;
        this.eventManager = eventManager;
        this.sessionManagers = sessionManagers;
        this.logicalLocalityResolver = logicalLocalityResolver;
        this.migratingCodeFactory = migratingCodeFactory;
    }

    /**
     * @see org.mikado.imc.protocols.ProtocolFactory#createProtocol()
     */
    public Protocol createProtocol() throws ProtocolException {
        MessageState klavaMessageState = new MessageState(routingTable,
                environment, sessionManagers, logicalLocalityResolver,
                migratingCodeFactory);

        klavaMessageState.setTupleOpManager(tupleOpManagerFactory
                .createTupleOpManager());
        klavaMessageState.setWaitingForResponse(waitingForResponse);
        klavaMessageState.setWaitingForTuple(waitingForTuple);
        klavaMessageState.setWaitingForLocality(waitingForLocality);

        Protocol protocol = new Protocol(klavaMessageState);
        protocol.setState("MESSAGE", klavaMessageState);
        klavaMessageState.setNextState("MESSAGE");

        protocol.setEventManager(eventManager);

        return protocol;
    }

}
