/*
 * Created on Jan 12, 2006
 */
package klava.topology;

import java.util.List;

import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.topology.NodeCoordinator;
import org.mikado.imc.topology.NodeCoordinatorProxy;
import org.mikado.imc.topology.NodeLocation;
import org.mikado.imc.topology.NodeProcess;


/**
 * A coordinator of a klava node.
 * 
 * @author Lorenzo Bettini
 */
public abstract class KlavaNodeCoordinator extends NodeCoordinator {
    /**
     * The proxy of the node for this coordinator
     */
    protected transient KlavaNodeCoordinatorProxy klavaNodeCoordinatorProxy;

    /**
     * The proxy for standard process operations
     */
    protected transient KlavaNodeProcessProxy processProxy;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The Locality representing self, set to KlavaNode.self.
     */
    protected Locality self = KlavaNode.self;

    /**
     * 
     */
    protected KlavaNodeCoordinator() {
        super();
    }

    /**
     * @param name
     */
    protected KlavaNodeCoordinator(String name) {
        super(name);
    }

    /**
     * The entry point of a process in IMC. It simply calls executeProcess,
     * which must be defined in subclasses of this class.
     * 
     * @see org.mikado.imc.topology.NodeProcess#execute()
     */
    @Override
    public final void execute() throws IMCException {
        try {
            executeProcess();
        } catch (KlavaException e) {
            e.printStackTrace();
            throw new IMCException("Uncaught exception", e);
        }

    }

    /**
     * @see org.mikado.imc.topology.NodeCoordinator#close()
     */
    @Override
    public void close() throws IMCException {
        // TODO remove it when implemented in IMC
        super.close();
        interrupt();
    }

    /**
     * The entry point of each klava process. It must be defined in the
     * subclasses of this class.
     * 
     * @throws KlavaException
     */
    public abstract void executeProcess() throws KlavaException;

    /**
     * This also initializes the proxy for standard process operations. It does
     * this by using the node reference stored in the KlavaNodeCoordinatorProxy.
     * 
     * @see org.mikado.imc.topology.NodeCoordinator#setNodeCoordinatorProxy(org.mikado.imc.topology.NodeCoordinatorProxy)
     */
    @Override
    protected void setNodeCoordinatorProxy(
            NodeCoordinatorProxy nodeCoordinatorProxy) throws IMCException {
        super.setNodeCoordinatorProxy(nodeCoordinatorProxy);
        try {
            klavaNodeCoordinatorProxy = (KlavaNodeCoordinatorProxy) nodeCoordinatorProxy;
            processProxy = new KlavaNodeProcessProxy(
                    klavaNodeCoordinatorProxy.klavaNode);
        } catch (ClassCastException e) {
            throw new IMCException("wrong type for NodeCoordinatorProxy", e);
        }
    }

    /**
     * @throws KlavaException
     * @see klava.topology.KlavaNodeCoordinatorProxy#accept(klava.PhysicalLocality,
     *      klava.PhysicalLocality)
     */
    public boolean accept(PhysicalLocality local, PhysicalLocality remote)
            throws KlavaException {
        return klavaNodeCoordinatorProxy.accept(local, remote);
    }

    /**
     * @throws KlavaException
     * @see klava.topology.KlavaNodeCoordinatorProxy#accept(klava.PhysicalLocality)
     */
    public boolean accept(PhysicalLocality remote) throws KlavaException {
        return klavaNodeCoordinatorProxy.accept(remote);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#disconnected(klava.PhysicalLocality,
     *      klava.LogicalLocality)
     */
    public void disconnected(PhysicalLocality physicalLocality,
            LogicalLocality logicalLocality) throws KlavaException {
        klavaNodeCoordinatorProxy.disconnected(physicalLocality,
                logicalLocality);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#disconnected(klava.PhysicalLocality)
     */
    public void disconnected(PhysicalLocality physicalLocality)
            throws KlavaException {
        klavaNodeCoordinatorProxy.disconnected(physicalLocality);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#login(klava.Locality)
     */
    public boolean login(Locality remote) throws KlavaException {
        return klavaNodeCoordinatorProxy.login(remote);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#logout(klava.Locality)
     */
    public boolean logout(Locality remote) throws KlavaException {
        return klavaNodeCoordinatorProxy.logout(remote);
    }

    /**
     * @throws KlavaException
     * @see klava.topology.KlavaNodeCoordinatorProxy#register(klava.PhysicalLocality,
     *      klava.LogicalLocality)
     */
    public boolean register(PhysicalLocality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNodeCoordinatorProxy.register(remote, logical);
    }

    /**
     * @throws KlavaException
     * @see klava.topology.KlavaNodeCoordinatorProxy#register(klava.PhysicalLocality,
     *      klava.PhysicalLocality, klava.LogicalLocality)
     */
    public boolean register(PhysicalLocality local, PhysicalLocality remote,
            LogicalLocality logical) throws KlavaException {
        return klavaNodeCoordinatorProxy.register(local, remote, logical);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#subscribe(klava.Locality,
     *      klava.LogicalLocality)
     */
    public boolean subscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNodeCoordinatorProxy.subscribe(remote, logical);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#unsubscribe(klava.Locality,
     *      klava.LogicalLocality)
     */
    public boolean unsubscribe(Locality remote, LogicalLocality logical)
            throws KlavaException {
        return klavaNodeCoordinatorProxy.unsubscribe(remote, logical);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#addNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    @Override
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        processProxy.addNodeProcess(nodeProcess);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#eval(klava.topology.KlavaProcess, klava.Locality)
     */
    public void eval(KlavaProcess klavaProcess, Locality destination) throws KlavaException {
        processProxy.eval(klavaProcess, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#eval(klava.topology.KlavaProcess)
     */
    public void eval(KlavaProcess klavaProcess) throws KlavaException {
        processProxy.eval(klavaProcess);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#executeNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    @Override
    public void executeNodeProcess(NodeProcess nodeProcess) throws InterruptedException, IMCException {
        processProxy.executeNodeProcess(nodeProcess);
    }

    /**
     * @see org.mikado.imc.topology.NodeProcessProxy#getNodeStack(org.mikado.imc.topology.NodeLocation)
     */
    @Override
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return processProxy.getNodeStack(nodeLocation);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#getPhysical(klava.Locality)
     */
    public PhysicalLocality getPhysical(Locality locality) throws KlavaException {
        return processProxy.getPhysical(locality);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in_nb(klava.Tuple, klava.Locality)
     */
    public boolean in_nb(Tuple tuple, Locality destination) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.in_nb(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in_nb(klava.Tuple)
     */
    public boolean in_nb(Tuple tuple) { // NOSONAR: we want this name
        return processProxy.in_nb(tuple);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in_t(klava.Tuple, klava.Locality, long)
     */
    public boolean in_t(Tuple tuple, Locality destination, long timeout) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.in_t(tuple, destination, timeout);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple tuple, long timeout) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.in_t(tuple, timeout);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in(klava.Tuple, klava.Locality)
     */
    public void in(Tuple tuple, Locality destination) throws KlavaException {
        processProxy.in(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#in(klava.Tuple)
     */
    public void in(Tuple tuple) throws KlavaException {
        processProxy.in(tuple);
    }

    /**
     * @see org.mikado.imc.topology.NodeProcessProxy#isLocal(org.mikado.imc.topology.NodeLocation)
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return processProxy.isLocal(nodeLocation);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#out(klava.Tuple, klava.Locality)
     */
    public void out(Tuple tuple, Locality destination) throws KlavaException {
        processProxy.out(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#out(klava.Tuple)
     */
    public void out(Tuple tuple) {
        processProxy.out(tuple);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read_nb(klava.Tuple, klava.Locality)
     */
    public boolean read_nb(Tuple tuple, Locality destination) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.read_nb(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple tuple) { // NOSONAR: we want this name
        return processProxy.read_nb(tuple);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read_t(klava.Tuple, klava.Locality, long)
     */
    public boolean read_t(Tuple tuple, Locality destination, long timeout) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.read_t(tuple, destination, timeout);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple tuple, long timeout) // NOSONAR: we want this name
            throws KlavaException {
        return processProxy.read_t(tuple, timeout);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read(klava.Tuple, klava.Locality)
     */
    public void read(Tuple tuple, Locality destination) throws KlavaException {
        processProxy.read(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#read(klava.Tuple)
     */
    public void read(Tuple tuple) throws KlavaException {
        processProxy.read(tuple);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#newloc()
     */
    public PhysicalLocality newloc() throws KlavaException {
        return klavaNodeCoordinatorProxy.newloc();
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#eval(klava.topology.KlavaNodeCoordinator)
     */
    public void eval(KlavaNodeCoordinator klavaNodeCoordinator) throws KlavaException {
        klavaNodeCoordinatorProxy.eval(klavaNodeCoordinator);
    }

    /**
     * @see klava.topology.KlavaNodeCoordinatorProxy#closeSessions(klava.PhysicalLocality)
     */
    public void closeSessions(PhysicalLocality physicalLocality) {
        klavaNodeCoordinatorProxy.closeSessions(physicalLocality);
    }

    /**
     * @param logicalLocality
     * @param physicalLocality
     * @return
     * @see klava.topology.KlavaNodeCoordinatorProxy#addToEnvironment(klava.LogicalLocality, klava.PhysicalLocality)
     */
    public boolean addToEnvironment(LogicalLocality logicalLocality, PhysicalLocality physicalLocality) {
        return klavaNodeCoordinatorProxy.addToEnvironment(logicalLocality, physicalLocality);
    }

    /**
     * Executes the given OR processes in parallel, implementing the OR operator.
     *
     * <p>The OR operator runs a set of {@link KlavaOrProcess}es concurrently.
     * Each process blocks on its first retrieval operation (the "guard"). The
     * first process to complete its guard interrupts all the others and then
     * proceeds with the rest of its body. The interrupted processes terminate
     * with a {@link KlavaException} wrapping the {@link InterruptedException}
     * that arises from their blocked retrieval.</p>
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Creates a shared {@link KlavaOrMutex} and registers it in all
     *       processes (and registers all processes in the mutex).</li>
     *   <li>Starts all processes in parallel via {@code eval} at the local
     *       node ({@code self}).</li>
     *   <li>Waits ({@code join}) for all processes to terminate before
     *       returning, ensuring the OR operation is fully resolved.</li>
     * </ol>
     *
     * <p><strong>Important assumption</strong>: the guard operations of the
     * processes in the OR must be mutually exclusive. If two guards can succeed
     * simultaneously, the behavior is undefined and it is not guaranteed that
     * only one process will proceed past its guard.</p>
     *
     * @param processes the collection of OR processes to run in parallel
     * @throws KlavaException if a process cannot be started
     */
    public void or(List<KlavaOrProcess> processes) throws KlavaException {
        new KlavaOrMutex().executeInOr(processes, p -> eval(p, self));
    }

}
