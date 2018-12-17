/*
 * Created on Nov 8, 2005
 */
package klava.tests.junit;

import klava.proto.Response;

/**
 * Thread used for waiting for a tuple
 * 
 * @author Lorenzo Bettini
 */
public class WaitingForResponseProcess<ResponseType extends Response>
        extends Thread {
    ResponseType response = null;

    /**
     * @param name
     */
    public WaitingForResponseProcess(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            response.waitForResponse();
        } catch (InterruptedException e) {
            return;
        }
    }
}
