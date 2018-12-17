/*
 * Created on Jan 18, 2005
 *
 */
package org.mikado.imc.topology;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventManager;
import org.mikado.imc.mobility.JavaMigratingCode;
import org.mikado.imc.protocols.ProtocolStack;

/**
 * A node process that can execute standard actions.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.19 $
 */
public abstract class NodeProcess extends JavaMigratingCode implements
        Closeable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** The proxy for the node. */
    protected transient NodeProcessProxy nodeProcessProxy;

    /**
     * This is used to generate unique process name when no explicit name is
     * specified.
     */
    protected static int nextId = 0;
    
    /**
     * Whether information passed to the verbose() methods should be
     * printed or not (default is no)
     */
    protected boolean verbosity = false;
    
    /**
     * If the process terminates due to an uncaught exception, then this
     * will be stored in this field.
     */
    protected IMCException finalException = null;

    /**
     * The default name of the process is created by using the process class
     * name and an unique (incremented) number.
     * 
     * Creates a new NodeCoordinator object.
     */
    public NodeProcess() {
        setName(getClass().getName() + "-" + getNextId());
    }

    /**
     * Creates a new NodeCoordinator object.
     * 
     * @param name
     *            The name of this process
     */
    public NodeProcess(String name) {
        super(name);
    }

    /**
     * @return Returns the NodeProcessProxy.
     */
    protected NodeProcessProxy getNodeProcessProxy() {
        return nodeProcessProxy;
    }

    /**
     * @param nodeProcessProxy
     *            The NodeProcessProxy to set.
     * @throws IMCException
     */
    protected void setNodeProcessProxy(NodeProcessProxy nodeProcessProxy)
            throws IMCException {
        this.nodeProcessProxy = nodeProcessProxy;
    }

    /**
     * This is the method invoked to actually start the process. Derived classes
     * must provide an implementation for it.
     * 
     * @throws IMCException
     */
    public abstract void execute() throws IMCException;

    /**
     * Method used to start the process. This will implicitly call execute().
     */
    public final void run() {
        try {
            preExecute();
            execute();
            postExecute();
        } catch (IMCException e) {
            e.printStackTrace();
            finalException = e;
        } finally {
            /* finally remove me from the list of running processes */
            if (nodeProcessProxy != null)
                nodeProcessProxy.removeNodeProcess(this);

            System.out.println("terminated " + getName());
        }
    }

    /**
     * Called before execute. The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void preExecute() throws IMCException {

    }

    /**
     * Called after execute. The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void postExecute() throws IMCException {

    }

    /**
     * @return The EventManager
     */
    public EventManager getEventManager() {
        return nodeProcessProxy.getEventManager();
    }

    /**
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager) {
        nodeProcessProxy.setEventManager(eventManager);
    }

    /**
     * @param nodeLocation
     * @return the ProtocolStack
     */
    public ProtocolStack getNodeStack(NodeLocation nodeLocation) {
        return nodeProcessProxy.getNodeStack(nodeLocation);
    }

    /**
     * @param nodeLocation
     * @return whether the NodeLocation is local
     */
    public boolean isLocal(NodeLocation nodeLocation) {
        return nodeProcessProxy.isLocal(nodeLocation);
    }

    /**
     * @param nodeProcess
     * @throws IMCException
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        nodeProcessProxy.addNodeProcess(nodeProcess);
    }

    /**
     * @param nodeProcess
     * @throws InterruptedException
     * @throws IMCException
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        nodeProcessProxy.executeNodeProcess(nodeProcess);
    }

    /**
     * This is called to tell the process that it should terminate.
     * 
     * Subclasses can override it in order to make the process terminate somehow
     * (e.g., by closing a stream used by the process, or a socket, etc.).
     * 
     * The default implementation does nothing.
     * 
     * @throws IMCException
     */
    public void close() throws IMCException {

    }

    /**
     * Returns the next id (incremented)
     * 
     * @return Returns the nextId.
     */
    synchronized protected static int getNextId() {
        return ++nextId;
    }

    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard output of the
     * node.
     * 
     * If the process is not running on a node, print it on the std out.
     * 
     * @param s
     *            The string to print
     */
    protected void SystemOutPrint(String s) {
        if (nodeProcessProxy == null) {
            System.out.print(getName() + ": " + s);
        } else {
            nodeProcessProxy.SystemOutPrint(getName() + ": " + s);
        }
    }

    /**
     * Prints by prefixing the process name.
     * 
     * This will be printed on the (possibly redirected) standard error of the
     * node.
     * 
     * If the process is not running on a node, print it on the std out.
     * 
     * @param s
     *            The string to print
     */
    protected void SystemErrPrint(String s) {
        if (nodeProcessProxy == null) {
            System.err.print(getName() + ": " + s);
        } else {
            nodeProcessProxy.SystemErrPrint(getName() + ": " + s);
        }
    }

    /**
     * @return Returns the verbosity.
     */
    public boolean isVerbosity() {
        return verbosity;
    }

    /**
     * @param verbosity The verbosity to set.
     */
    public void setVerbosity(boolean verbosity) {
        this.verbosity = verbosity;
    }
    
    /**
     * If verbosity is set then prints the passed string using
     * SystemOutPrint.
     * 
     * @param s
     */
    protected void verbose(String s) {
        if (verbosity)
            SystemOutPrint(s);
    }

    /**
     * @return Returns the finalException.
     */
    public IMCException getFinalException() {
        return finalException;
    }

    /**
     * @param finalException The finalException to set.
     */
    public void setFinalException(IMCException finalException) {
        this.finalException = finalException;
    }
}
