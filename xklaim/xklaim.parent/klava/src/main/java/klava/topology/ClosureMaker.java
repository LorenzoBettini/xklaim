/**
 * created: Jan 4, 2006
 */
package klava.topology;

import java.io.Serializable;
import java.util.Enumeration;

import klava.Environment;
import klava.KlavaException;
import klava.KlavaLogicalLocalityException;
import klava.LogicalLocality;
import klava.LogicalLocalityResolver;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;


/**
 * Performs the closure of a Tuple and TupleItem
 * 
 * @author Lorenzo Bettini
 * 
 */
public class ClosureMaker implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is used to resolve logical localities, if set, otherwise only the
     * environment is used to resolve logical localities.
     * Although the ClosureMaker can be sent, e.g., together with a
     * process, the LogicalLocalityResolver is not since it may
     * have references to local resorces (e.g., a node).
     */
    transient protected LogicalLocalityResolver logicalLocalityResolver;

    /**
     * The environment used to close processes.
     */
    protected Environment environment;

    /**
     * If true, it stops the closure as soon as an Exception is thrown.
     */
    protected boolean stopOnException = true;

    /**
     * @param logicalLocalityResolver
     *            to resolve logical localities
     * @param environment
     *            to close processes
     */
    public ClosureMaker(LogicalLocalityResolver logicalLocalityResolver,
            Environment environment) {
        this.logicalLocalityResolver = logicalLocalityResolver;
        this.environment = environment;
    }

    /**
     * Performs the closure of the passed tuple, using the passed
     * PhysicalLocality to close self.
     * 
     * @param tuple
     *            The tuple to perform the closure on
     * @param forSelf
     *            The PhysicalLocality to close self
     * @return if stopOnException is false it returns true if no problem was
     *         encountered during the closure
     * @throws KlavaException
     */
    public boolean makeClosure(Tuple tuple, PhysicalLocality forSelf)
            throws KlavaException {
        Enumeration<Object> tupleElements = tuple.tupleElements();
        int i = 0;
        boolean result = true;

        while (tupleElements.hasMoreElements()) {
            Object elem = tupleElements.nextElement();

            try {
                if (elem instanceof TupleItem)
                    tuple.setItem(i, makeClosure((TupleItem) elem, forSelf));
                else if (elem instanceof Tuple)
                    makeClosure((Tuple) elem, forSelf);
                else if (elem instanceof KlavaProcess)
                    ((KlavaProcess) elem).makeProcessClosure(forSelf,
                            environment);
            } catch (KlavaException e) {
                if (stopOnException)
                    throw e;

                /* from now on the result is false */
                result = false;

                /* but go on with the remaining items */
            }

            ++i;
        }

        return result;
    }

    /**
     * Performs the closure of the passed tuple element, using the passed
     * PhysicalLocality to close self. The closed tuple element is returned (if
     * the element does not need to be closed, it simply returns the element
     * itself).
     * 
     * If the item is a LogicalLocality it uses the logicalLocalityResolver to
     * translate it into a PhysicalLocality, if the logicalLocalityResolver is
     * set. Otherwise it simply uses the Environment.
     * 
     * @param tupleItem
     *            The tuple element to perform the closure on
     * @param forSelf
     *            The PhysicalLocality to close self
     * @return The closed tuple element
     * @throws KlavaException
     */
    public TupleItem makeClosure(TupleItem tupleItem, PhysicalLocality forSelf)
            throws KlavaException {
        if (tupleItem instanceof LogicalLocality) {
            if (tupleItem.toString().equals("self")) {
                if (forSelf == null)
                    throw new KlavaLogicalLocalityException(
                            "no specification for self");

                return forSelf;
            }

            if (logicalLocalityResolver != null)
                return logicalLocalityResolver
                        .resolve((LogicalLocality) tupleItem);
            else {
                PhysicalLocality physicalLocality = environment
                        .toPhysical((LogicalLocality) tupleItem);
                if (physicalLocality == null)
                    throw new KlavaLogicalLocalityException(tupleItem
                            .toString());

                return physicalLocality;
            }
        }

        return tupleItem;
    }

    /**
     * @return Returns the stopOnException.
     */
    public final boolean isStopOnException() {
        return stopOnException;
    }

    /**
     * @param stopOnException
     *            The stopOnException to set.
     */
    public final void setStopOnException(boolean stopOnException) {
        this.stopOnException = stopOnException;
    }
}
