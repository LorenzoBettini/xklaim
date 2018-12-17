package klava.topology;

import klava.Environment;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.topology.NodeProcess;
import org.mikado.imc.topology.NodeProcessProxy;


/**
 * The abstract base class for processes to be executed on a klava nodes.
 * 
 * @author Lorenzo Bettini
 */
public abstract class KlavaProcess extends NodeProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The specialized proxy to forward node operations
     */
    protected transient KlavaNodeProcessProxy klavaNodeProcessProxy;

    /**
     * The Locality representing self. It defaults to KlavaNode.self; however it
     * can be changed when computing the closure of the process in order to set
     * it, e.g., to a PhysicalLocality corresponding to the originating node.
     */
    protected Locality self = KlavaNode.self;

    /**
     * The Environmente of the process; this is used when the closure of the
     * process is built. If a logical locality cannot be solved with the
     * environment of the process then the node's environment will be used.
     */
    protected Environment environment = new Environment();

    /**
     * This is used by the process to perform closures. If the making of the
     * closure fails with this one, we rely on the node's closure capabilities.
     */
    protected ClosureMaker closureMaker;

    /**
     * Whether tuples are closed automatically.
     */
    protected boolean doAutomaticClosure = false;

    /**
     * Initial state of a process
     */
    public final static int NOT_MIGRATED = 0;

    /**
     * The process has migrated to another site, so it has stopped its execution
     * in this node.
     */
    public final static int MIGRATED = 1;

    /**
     * The process has migrated from this node to this node itself (so it's not
     * really a migration).
     */
    public final static int CONTINUED = 2;

    /**
     * The process is going to be restored after migration.
     */
    public final static int ARRIVED = 3;

    /**
     * The process is migrating.
     */
    public final static int MIGRATING = 4;

    /**
     * The current migration status of the process (default: not migrated)
     */
    protected int migrationStatus = NOT_MIGRATED;

    /**
     * The process that called this process (useful upon migration).
     */
    protected KlavaProcess caller = null;

    /**
     * Creates a klava process
     */
    public KlavaProcess() {
        initProcess();
    }

    /**
     * Creates a klava process
     * 
     * @param name
     *            The name of the process
     */
    public KlavaProcess(String name) {
        super(name);
        initProcess();
    }

    /**
     * Called by the constructor to initialize the process fields appropriately.
     */
    private void initProcess() {
        /*
         * if this fails we rely on the node, so we don't want this to exit upon
         * the first exception
         */
        closureMaker = new ClosureMaker(null, environment);
        closureMaker.setStopOnException(false);

        /*
         * the classes of these packages must be common to all the nodes we
         * visit
         */
        addExcludePackage("klava.");
        addExcludePackage("momi.");
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
            if (migrationStatus == MIGRATING) {
                /*
                 * the process has migrated into this site, so we mark it as
                 * arrived.
                 */
                updateMigrationStatus(ARRIVED);
            }

            executeProcess();

            /*
             * check if there's a caller (in case of migration) and in case runs
             * it
             */
            if (caller != null && migrationStatus == ARRIVED) {
                /*
                 * notice that the migration status of the caller has already
                 * been update during the recursive updateMigrationStatus
                 */
                addNodeProcess(caller);
            }
        } catch (KlavaException e) {
            e.printStackTrace();
            throw new IMCException("Uncaught exception", e);
        } catch (ProcessTerminatedException e) {
            /*
             * simply avoid this to be propagated. The process terminated
             * locally, e.g., after a migration
             */
        }

    }

    /**
     * Takes care of checking that the passed proxy is actually a specialized
     * KlavaNodeProcessProxy, otherwise throws an IMCException
     * 
     * @see org.mikado.imc.topology.NodeProcess#setNodeProcessProxy(org.mikado.imc.topology.NodeProcessProxy)
     */
    @Override
    protected void setNodeProcessProxy(NodeProcessProxy nodeProcessProxy)
            throws IMCException {
        super.setNodeProcessProxy(nodeProcessProxy);
        try {
            klavaNodeProcessProxy = (KlavaNodeProcessProxy) nodeProcessProxy;
        } catch (ClassCastException e) {
            throw new IMCException("wrong type for NodeProcessProxy", e);
        }
    }

    /**
     * The entry point of each klava process. It must be defined in the
     * subclasses of this class.
     * 
     * @throws KlavaException
     */
    public abstract void executeProcess() throws KlavaException;

    /**
     * @see org.mikado.imc.topology.NodeProcess#addNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        klavaNodeProcessProxy.addNodeProcess(nodeProcess);
    }

    protected boolean in_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        makeAutomaticClosure(tuple);
        return klavaNodeProcessProxy.in_nb(tuple, toPhysical(destination));
    }

    protected boolean in_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        makeAutomaticClosure(tuple);
        return klavaNodeProcessProxy.in_t(tuple, toPhysical(destination),
                timeout);
    }

    protected void in(Tuple tuple, Locality destination) throws KlavaException {
        makeAutomaticClosure(tuple);
        klavaNodeProcessProxy.in(tuple, toPhysical(destination));
    }

    protected void out(Tuple tuple, Locality destination) throws KlavaException {
        makeAutomaticClosure(tuple);
        klavaNodeProcessProxy.out(tuple, toPhysical(destination));
    }

    protected boolean read_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        makeAutomaticClosure(tuple);
        return klavaNodeProcessProxy.read_nb(tuple, toPhysical(destination));
    }

    protected boolean read_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        makeAutomaticClosure(tuple);
        return klavaNodeProcessProxy.read_t(tuple, toPhysical(destination),
                timeout);
    }

    protected void read(Tuple tuple, Locality destination)
            throws KlavaException {
        makeAutomaticClosure(tuple);
        klavaNodeProcessProxy.read(tuple, toPhysical(destination));
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#eval(klava.topology.KlavaProcess,
     *      klava.Locality)
     */
    protected void eval(KlavaProcess klavaProcess, Locality destination)
            throws KlavaException {
        klavaNodeProcessProxy.eval(klavaProcess, toPhysical(destination));
    }

    /**
     * Tries to translate the passed locality into a PhysicalLocality by using
     * the process' environment (if it is set). If this cannot be done, it
     * returns the passed locality itself, so that the process can delegate the
     * translation to the node.
     * 
     * @param locality
     *            The locality to translate into a PhysicalLocality
     * @return The PhysicalLocality resulting from the translation or the passed
     *         locality itself if the translation cannot be done
     */
    protected Locality toPhysical(Locality locality) {
        /*
         * make sure that, if we refer to self, we actually use the self of the
         * process (which can be already closed)
         */
        if (locality.toString().equals("self")) {
            locality = self;
        }

        if (locality instanceof LogicalLocality) {
            PhysicalLocality physicalLocality = environment
                    .toPhysical((LogicalLocality) locality);
            if (physicalLocality != null)
                return physicalLocality;
        }

        return locality;
    }

    /**
     * Tries to translate the passed locality into a PhysicalLocality by using
     * the process' environment (if it is set). If this cannot be done, it
     * queries the node for such translation
     * 
     * @param locality
     *            The locality to translate into a PhysicalLocality
     * @return The PhysicalLocality resulting from the translation or the passed
     *         locality itself if the translation cannot be done
     * @throws KlavaException
     */
    protected Locality getPhysical(Locality locality) throws KlavaException {
        /*
         * make sure that, if we refer to self, we actually use the self of the
         * process (which can be already closed)
         */
        if (locality.toString().equals("self")) {
            locality = self;
        }

        if (locality instanceof LogicalLocality) {
            PhysicalLocality physicalLocality = environment
                    .toPhysical((LogicalLocality) locality);
            if (physicalLocality != null)
                return physicalLocality;
            else
                return klavaNodeProcessProxy.getPhysical(locality);
        }

        return locality;
    }

    /**
     * Adds a mapping logical locality - physical locality to the process' own
     * environment. The mapping is inserted only if a mapping for the specified
     * logical locality was not already present in the process' environment.
     * 
     * @param logicalLocality
     * @param physicalLocality
     */
    public void addToEnvironment(LogicalLocality logicalLocality,
            PhysicalLocality physicalLocality) {
        environment.try_add(logicalLocality, physicalLocality);
    }

    /**
     * Makes the closure of this process: sets the self to the passed
     * PhysicalLocality (if not already set) and updates the process'
     * environment with the passed environment (only the mapping for logical
     * localities not already present are inserted in the process' environment).
     * 
     * @param forSelf
     * @param environment
     */
    public void makeProcessClosure(PhysicalLocality forSelf,
            Environment environment) {
        if (self instanceof LogicalLocality) {
            /* it is not already closed */
            self = new PhysicalLocality(forSelf);
        }

        this.environment.addFromEnvironment(environment);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#makeClosure(klava.Tuple,
     *      klava.PhysicalLocality)
     */
    protected void makeClosure(Tuple tuple, PhysicalLocality forSelf)
            throws KlavaException {
        /* we were able to close everything */
        if (closureMaker.makeClosure(tuple, forSelf))
            return;

        /*
         * if we're here, something has still to be closed so we rely on the
         * underlying node
         */
        klavaNodeProcessProxy.makeClosure(tuple, forSelf);
    }

    /**
     * @see klava.topology.KlavaNodeProcessProxy#makeClosure(klava.TupleItem,
     *      klava.PhysicalLocality)
     */
    protected TupleItem makeClosure(TupleItem tupleItem,
            PhysicalLocality forSelf) throws KlavaException {
        try {
            return closureMaker.makeClosure(tupleItem, forSelf);
        } catch (KlavaException e) {
            /* we failed, so we rely on the node */
        }

        return klavaNodeProcessProxy.makeClosure(tupleItem, forSelf);
    }

    /**
     * Translates the passed Locality and returns its translation.
     * 
     * @param locality
     * @return the translated locality.
     * @throws KlavaException
     */
    protected PhysicalLocality translateLocality(Locality locality)
            throws KlavaException {
        Locality translated = toPhysical(locality);

        /* OK we're done */
        if (translated instanceof PhysicalLocality)
            return (PhysicalLocality) translated;

        /* otherwise rely on the node */
        return klavaNodeProcessProxy.getPhysical(locality);
    }

    /**
     * If it is the case, makes the closure of the passed tuple.
     * 
     * @param tuple
     * @throws KlavaException
     */
    protected void makeAutomaticClosure(Tuple tuple) throws KlavaException {
        if (!doAutomaticClosure)
            return;

        makeClosure(tuple, translateSelf());
    }

    /**
     * Tries to translate self.
     * 
     * @return the translated self or null if it cannot be translated
     * @throws KlavaException
     */
    protected PhysicalLocality translateSelf() throws KlavaException {
        Locality translatedSelf = toPhysical(self);
        PhysicalLocality forSelf = null;

        if (translatedSelf instanceof PhysicalLocality)
            forSelf = (PhysicalLocality) translatedSelf;
        else {
            try {
                forSelf = klavaNodeProcessProxy.getPhysical(self);
            } catch (KlavaLogicalLocalityException e) {
                /*
                 * OK we cannot resolve self, but this might not be necessary in
                 * the for a closure, so we just go on; in case, an exception
                 * will be raised during the closure
                 */
            }
        }

        return forSelf;
    }

    /**
     * Makes the current process migrate to a remote site
     * 
     * @param locality
     * @throws KlavaException
     */
    protected void migrate(Locality locality) throws KlavaException {
        updateMigrationStatus(MIGRATING);
        eval(this, locality);

        /* now the process locally has to be considered migrated */
        updateMigrationStatus(MIGRATED);

        /* this makes the process terminate locally */
        throw new ProcessTerminatedException(getName());
    }

    /**
     * Sets the migration status of this process and of its caller (thus
     * recursively updates all the callers in the chain).
     * 
     * @param status
     */
    protected void updateMigrationStatus(int status) {
        migrationStatus = status;
        if (caller != null)
            caller.updateMigrationStatus(status);
    }

    /**
     * @return Returns the doAutomaticClosure.
     */
    public boolean isDoAutomaticClosure() {
        return doAutomaticClosure;
    }

    /**
     * @param doAutomaticClosure
     *            The doAutomaticClosure to set.
     */
    public void setDoAutomaticClosure(boolean doAutomaticClosure) {
        this.doAutomaticClosure = doAutomaticClosure;
    }

    /**
     * @return Returns the migrationStatus.
     */
    public int getMigrationStatus() {
        return migrationStatus;
    }

    /**
     * This is an overloaded version of executeNodeProcess considering a
     * KlavaProcess. In this case, updates the caller of the process we're about
     * to execute. We set the caller to this very process.
     * 
     * Notice that callers are not inserted in a stack; this mechanism is
     * thought for strong mobility, which cannot be achieved in Java directly:
     * this is realized by xklaim compiler through a translation process.
     * 
     * WARNING: calling executeNodeProcess passing this will break the caller
     * mechanism. This will never happen when the code is generated by the
     * xklaim compiler.
     * 
     * @see klava.topology.KlavaNodeProcessProxy#executeNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    public void executeNodeProcess(KlavaProcess klavaProcess)
            throws KlavaException {
        /* tell the process we're about to execute that we called it */
        klavaProcess.setCaller(this);
        try {
            klavaNodeProcessProxy.executeNodeProcess(klavaProcess);

            if (klavaProcess.getMigrationStatus() == MIGRATED) {
                /*
                 * we must terminate too, since the process we called had made
                 * us migrate
                 */
                throw new ProcessTerminatedException(getName());
            }
        } catch (InterruptedException e) {
            throw new KlavaException(e);
        } catch (IMCException e) {
            throw new KlavaException(e);
        }
    }

    /**
     * @return Returns the caller.
     */
    protected final KlavaProcess getCaller() {
        return caller;
    }

    /**
     * @param caller
     *            The caller to set.
     */
    protected final void setCaller(KlavaProcess caller) {
        this.caller = caller;
    }
}
