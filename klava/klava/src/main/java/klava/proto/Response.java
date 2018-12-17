/*
 * Created on Oct 7, 2005
 */
package klava.proto;

import klava.KlavaTimeOutException;

/**
 * Represents a generic response. It contains a (possible) error field, that
 * should be tested first to check whether the response consists of an error and
 * a generic response content. Subclasses must specify the type of the content
 * and may add specific fields.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class Response<ResponseContent> {
    /**
     * If not null, the response consists of an error
     */
    public String error = null;

    /**
     * The content of the response
     */
    public ResponseContent responseContent = null;

    /**
     * Wait for this response to be initialized and notified.
     * 
     * @throws InterruptedException
     */
    public synchronized void waitForResponse() throws InterruptedException {
        while (error == null && responseContent == null)
            wait();
    }

    /**
     * Wait for this response to be initialized and notified. Specifies also a
     * time-out. If this passes, a KlavaTimeOutException is thrown
     * 
     * @throws InterruptedException
     * @throws KlavaTimeOutException
     */
    public synchronized void waitForResponse(long timeOut)
            throws InterruptedException, KlavaTimeOutException {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (error == null && responseContent == null)
                wait(timeOut);

            if (!(error == null && responseContent == null))
                return; // OK no time out

            timeOut -= (System.currentTimeMillis() - startTime);
            if (timeOut <= 0)
                throw new KlavaTimeOutException();
        }
    }

    /**
     * Resets this Response, so that it can be used for another request.
     */
    public void reset() {
        error = null;
        responseContent = null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "error: " + (error == null ? "null" : error) + ", "
                + "content: "
                + (responseContent == null ? "null" : responseContent);
    }
}
