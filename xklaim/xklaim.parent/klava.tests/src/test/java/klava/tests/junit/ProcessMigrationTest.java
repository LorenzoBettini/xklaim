/*
 * Created on Feb 7, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;

import org.mikado.imc.protocols.ProtocolException;


/**
 * Tests for process migration
 * 
 * @author Lorenzo Bettini
 */
public class ProcessMigrationTest extends ClientServerBase {

    /**
     * @see junit.ClientServerBase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @see junit.ClientServerBase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleMigration() throws ProtocolException,
            InterruptedException, KlavaException {
        clientLoginsToServer();
        MigratingProcess migratingProcess = new MigratingProcess(serverLoc);
        clientNode.eval(migratingProcess);

        Tuple template = new Tuple(new KString());
        clientNode.in(template);
        assertEquals(new KString("not migrated yet"), template.getItem(0));

        template = new Tuple(new KString(), new PhysicalLocality());
        serverNode.in(template);
        assertEquals(new KString("arrived at"), template.getItem(0));
        assertEquals(serverLoc, template.getItem(1));

    }

    public void testCallingMigration() throws ProtocolException,
            InterruptedException, KlavaException {
        System.out.println("*** testCallingMigration");
        clientLoginsToServer();
        MigratingCallingProcess migratingCallingProcess = new MigratingCallingProcess(
                serverLoc, clientLoc);
        clientNode.eval(migratingCallingProcess);

        Tuple template = new Tuple(new KString());
        clientNode.in(template);
        assertEquals(new KString("not migrated yet"), template.getItem(0));

        /*
         * notice that there must be two such tuples (one inserted by
         * MigratingProcess and one by MigratingCallingProcess
         */
        template = new Tuple(new KString(), new PhysicalLocality());
        serverNode.in(template);
        assertEquals(new KString("arrived at"), template.getItem(0));
        assertEquals(serverLoc, template.getItem(1));

        template = new Tuple(new KString(), new PhysicalLocality());
        serverNode.in(template);
        assertEquals(new KString("arrived at"), template.getItem(0));
        assertEquals(serverLoc, template.getItem(1));

        /* now the original process should be back home */
        template = new Tuple(new KString("back home"), new PhysicalLocality());
        clientNode.in(template);
        assertEquals(clientLoc, template.getItem(1));

        /* and should be terminated */
        template = new Tuple(new KString("terminated at"),
                new PhysicalLocality());
        clientNode.in(template);
        assertEquals(clientLoc, template.getItem(1));

        /*
         * on the contrary nothing must have been put after the process migrated
         * (due to calling a process that migrates) and after the process
         * migrated back home
         */
        template = new Tuple(new KString("terminated at"), new PhysicalLocality());
        assertFalse(clientNode.in_t(template, 2000));
        
        template = new Tuple(new KString("terminated at"), new PhysicalLocality());
        assertFalse(serverNode.in_t(template, 2000));

        Thread.sleep(3000);
    }
}
