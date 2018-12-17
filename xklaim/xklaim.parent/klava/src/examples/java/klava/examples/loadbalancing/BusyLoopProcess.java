/*
 * Created on Apr 3, 2006
 */
package klava.examples.loadbalancing;

import klava.KlavaException;
import klava.Tuple;
import klava.examples.gui.NodeWithScreen;
import klava.topology.KlavaProcess;

/**
 * A process that performs a busy loop (only to use CPU).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class BusyLoopProcess extends KlavaProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    double c = 0;

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        out(new Tuple(getName() + " performing busy loop...\n"),
                NodeWithScreen.screenLoc);
        for (int i = 0; i < 10000; ++i)
            c += Math.random();
        out(new Tuple(getName() + " finished: " + c + "\n"), NodeWithScreen.screenLoc);
    }

}
