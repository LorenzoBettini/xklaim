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
 * Used to test process migration. This process also invokes another process.
 * 
 * @author Lorenzo Bettini
 */
public class MigratingCallingProcess extends KlavaProcess {
    /**
     * where to migrate to
     */
    Locality remote;

    /**
     * where we come from
     */
    Locality home;

    boolean notYetHome = true;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MigratingCallingProcess(Locality remote, Locality home) {
        this.remote = remote;
        this.home = home;
    }

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        System.out.println(getClass().getName() + " executing at "
                + getPhysical(self));

        if (getMigrationStatus() == NOT_MIGRATED) {
            MigratingProcess migratingProcess = new MigratingProcess(remote);

            /* invokes a process that migrates */
            executeNodeProcess(migratingProcess);
        } else {
            if (getMigrationStatus() == ARRIVED) {
                if (notYetHome) {
                    notYetHome = false;
                    out(
                            new Tuple(new KString("arrived at"),
                                    getPhysical(self)), self);
                    System.out.println("going back home " + home);
                    migrate(home);
                } else {
                    System.out.println("back home " + home);
                    out(
                            new Tuple(new KString("back home"),
                                    getPhysical(self)), self);
                }
            }
        }

        /* it must not get here unless we're back home */
        System.out.println(getClass().getName() + " terminated at "
                + getPhysical(self));
        out(new Tuple(new KString("terminated at"), getPhysical(self)), self);
    }

}
