/*
 * Created on May 23, 2006
 */
package klava.proto;

import klava.TupleSpace;
import klava.WaitingForResponse;

import org.mikado.imc.topology.RoutingTable;
import org.mikado.imc.topology.SessionManagers;

/**
 * A factory for TupleOpManagers
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class TupleOpManagerFactory {

    /**
     * The tuple space
     */
    protected TupleSpace tupleSpace;

    /**
     * The RoutingTable
     */
    protected RoutingTable routingTable;

    /**
     * The table that associates process names to generic responses.
     */
    protected WaitingForResponse<Response<String>> waitingForOkResponse;

    /**
     * The ExecutionEngine for processes received from remote sites
     */
    protected ExecutionEngine executionEngine;

    /** Managers for incoming and outgoing sessions */
    protected SessionManagers sessionManagers;

    /**
     * @param tupleSpace
     * @param routingTable
     * @param waitingForOkResponse
     * @param executionEngine
     * @param sessionManagers
     */
    public TupleOpManagerFactory(TupleSpace tupleSpace, RoutingTable routingTable, WaitingForResponse<Response<String>> waitingForOkResponse, ExecutionEngine executionEngine, SessionManagers sessionManagers) {
        this.tupleSpace = tupleSpace;
        this.routingTable = routingTable;
        this.waitingForOkResponse = waitingForOkResponse;
        this.executionEngine = executionEngine;
        this.sessionManagers = sessionManagers;
    }

    public TupleOpManager createTupleOpManager() {
        return new TupleOpManager(tupleSpace, routingTable,
                sessionManagers.outgoingSessionManager, executionEngine,
                waitingForOkResponse);
    }
}
