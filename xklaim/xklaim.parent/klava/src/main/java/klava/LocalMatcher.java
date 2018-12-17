/**
 * LocalMatcher.
 * @author Lorenzo Bettini
 * @version 1.0
 *
 * Used by class Node to preprocess the tuple received from the Network
 * and see if it matches with the one requested.
 * The default implementation is that the tuple is OK.
 */

package klava;

public class LocalMatcher {

    /**
     * Constructor
     */
    public LocalMatcher() {
    }

    /**
     * Do a match with the tuple received from the network. Default
     * implementation always return true.
     * 
     * @param source
     *            the template used for the match
     * @param received
     *            candidate tuple
     * @param requester
     *            the KlavaProcess that requested the tuple
     * @return true if the local match succeeds
     */
    public boolean localMatch(Tuple source, Tuple received) {
        return true;
    }
}
