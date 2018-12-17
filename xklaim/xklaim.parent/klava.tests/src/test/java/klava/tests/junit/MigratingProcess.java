/*
 * Created on Feb 7, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.Tuple;
import klava.topology.KlavaProcess;

/**
 * Used to test process migration.
 * 
 * @author Lorenzo Bettini
 */
public class MigratingProcess extends KlavaProcess {
    /**
     * where to migrate to
     */
    Locality remote;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MigratingProcess(Locality remote) {
        this.remote = remote;
    }

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        System.out.println("executing at " + getPhysical(self));
        
        if (getMigrationStatus() == NOT_MIGRATED) {
            System.out.println("not migrated yet");
            out(new Tuple(new KString("not migrated yet")), self);
            migrate(remote);
        } else {
            if (getMigrationStatus() == ARRIVED) {
                System.out.println("after migration");
                out(new Tuple(new KString("arrived at"), getPhysical(self)),
                        self);
            }
        }
    }

}
