/*
 * Created on Mar 24, 2006
 */
package klava.examples.process;

import klava.KlavaException;
import klava.Locality;
import klava.topology.KlavaProcess;

/**
 * Performs eval of a given process to a given destination
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class EvalProcess extends KlavaProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The process to eval
     */
    public KlavaProcess klavaProcess;
    
    /**
     * Destination for eval (default: self)
     */
    public Locality destination = self;

    /**
     * @param klavaProcess
     * @param destination
     */
    public EvalProcess(KlavaProcess klavaProcess, Locality destination) {
        this.klavaProcess = klavaProcess;
        this.destination = destination;
    }

    /**
     * @param destination
     */
    public EvalProcess(Locality destination) {
        this.destination = destination;
    }

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        SystemOutPrint("eval(" + klavaProcess.getName() + ")@" + destination + "\n");
        eval(klavaProcess, destination);
        SystemOutPrint("done\n");
    }

}
