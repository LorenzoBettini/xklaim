/*
 * Created on Mar 27, 2006
 */
package klava.examples.process;

import klava.KlavaException;
import klava.Locality;
import klava.topology.KlavaProcess;

/**
 * Migrates to a specific remote site and execute a given process
 * at the remote site.
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class MigratingProcess extends EvalProcess {
    /**
     * Whether the process has already migrated
     */
    boolean migrated = false;
    
    /**
     * @param klavaProcess
     * @param destination
     */
    public MigratingProcess(KlavaProcess klavaProcess, Locality destination) {
        super(klavaProcess, destination);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        if (!migrated) {
            SystemOutPrint("migrating to " + destination + "\n");
            migrated = true;
            migrate(destination);
            // will never reach here
        } else {
            SystemOutPrint("executing " + klavaProcess.getName() + " @ " + destination + "\n");
            executeNodeProcess(klavaProcess);
            SystemOutPrint("done\n");
        }
    }

}
