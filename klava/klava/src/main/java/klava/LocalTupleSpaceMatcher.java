/**
 * LocalTupleSpaceMatcher.
 * @author Lorenzo Bettini
 * @version 1.0
 *
 * Used by class Node to retrieve tuples by means of in and read from
 * a local tuple space (i.e. the tuple space of the node and the tuple
 * space created by the node itself by means of a newloc).
 */

package klava;

public class LocalTupleSpaceMatcher {

    /**
     * the tuple space where the search is performed.
     */
    protected TupleSpace TS = null;

    /**
     * Constructor
     */
    public LocalTupleSpaceMatcher() {
    }

    /**
     * Constructor
     * 
     * @param ts
     *            the tuple space where the search will be performed.
     */
    public LocalTupleSpaceMatcher(TupleSpace ts) {
        TS = ts;
    }

    /**
     * set the tuple space
     * 
     * @param ts
     *            the tuple space
     */
    public void setTupleSpace(TupleSpace ts) {
        TS = ts;
    }

    /**
     * retrieve a tuple with "in" in the tuple space. Default implementation
     * simply forwards the method to the tuple space.
     * 
     * @param t
     *            the tuple used as a template.
     * @param blocking
     *            whether it is a blocking request
     * @param requester
     *            the process that requests the tuple
     * @return true if it succeeds, false otherwise
     * @throws InterruptedException
     */
    public boolean in(Tuple t, boolean blocking) throws InterruptedException {
        if (blocking)
            return TS.in(t);
        else
            return TS.in_nb(t);
    }

    /**
     * retrieve a tuple with "in" in the tuple space, within a specified
     * timeout. Default implementation simply forwards the method to the tuple
     * space.
     * 
     * @param t
     *            the tuple used as a template.
     * @param timeout
     *            the timeout.
     * @param requester
     *            the process that requests the tuple
     * @return true if it succeeds, false otherwise
     * @throws InterruptedException
     */
    public boolean in(Tuple t, long timeout) throws InterruptedException {
        return TS.in_t(t, timeout);
    }

    /**
     * retrieve a tuple with "read" in the tuple space. Default implementation
     * simply forwards the method to the tuple space.
     * 
     * @param t
     *            the tuple used as a template.
     * @param blocking
     *            whether it is a blocking request
     * @param requester
     *            the process that requests the tuple
     * @return true if it succeeds, false otherwise
     * @throws InterruptedException
     */
    public boolean read(Tuple t, boolean blocking) throws InterruptedException {
        if (blocking)
            return TS.read(t);
        else
            return TS.read_nb(t);
    }

    /**
     * retrieve a tuple with "read" in the tuple space, within a specified
     * timeout. Default implementation simply forwards the method to the tuple
     * space.
     * 
     * @param t
     *            the tuple used as a template.
     * @param timeout
     *            the timeout.
     * @param requester
     *            the process that requests the tuple
     * @return true if it succeeds, false otherwise
     * @throws InterruptedException
     */
    public boolean read(Tuple t, long timeout) throws InterruptedException {
        return TS.read_t(t, timeout);
    }

    public boolean should_retry() {
        return false;
    }

}
