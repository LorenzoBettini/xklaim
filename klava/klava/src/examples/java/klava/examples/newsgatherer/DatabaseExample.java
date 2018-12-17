/*
 * Created on Mar 29, 2006
 */
package klava.examples.newsgatherer;

import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.Event;
import org.mikado.imc.events.EventCounter;
import org.mikado.imc.events.RouteEvent;

/**
 * An example of database nodes net.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class DatabaseExample {
    /**
     * Intercepts event of a new node becoming part of the database
     * 
     * @author Lorenzo Bettini
     * @version $Revision $
     */
    public class DatabaseNodeListener extends EventCounter {
        int events = 0;

        /**
         * @see org.mikado.imc.events.EventListener#notify(org.mikado.imc.events.Event)
         */
        public void notify(Event event) {
            super.notify(event);
            try {
                mainDatabaseNode.out(new Tuple(event.toString() + "\n"),
                        new LogicalLocality("screen"));
            } catch (KlavaException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The top level node of the database
     */
    protected DatabaseNode mainDatabaseNode;

    /**
     * @throws IMCException
     * @throws KlavaException
     * 
     */
    public DatabaseExample(PhysicalLocality serverLoc) throws KlavaException,
            IMCException {
        initDB(serverLoc);
    }

    /**
     * We'll have this final net configuration:
     * 
     * <pre>
     * main node   (links item to node1)
     *     |
     *  node 1     (links item to node2)
     *     |
     *  node 2     (links item to node3)
     *     |
     *  node 3     (contains value for item)
     * </pre>
     * 
     * @param server
     * @throws KlavaException
     * @throws IMCException
     */
    protected void initDB(PhysicalLocality server) throws KlavaException,
            IMCException {
        mainDatabaseNode = new DatabaseNode("main node");
        mainDatabaseNode.subscribe(server, new LogicalLocality("main node"));
        DatabaseNodeListener databaseNodeListener = new DatabaseNodeListener();
        mainDatabaseNode.addListener(RouteEvent.ROUTE_EVENT,
                databaseNodeListener);

        DatabaseNode databaseNode3 = new DatabaseNode("node 3");
        DatabaseNode databaseNode2 = new DatabaseNode("node 2");
        DatabaseNode databaseNode1 = new DatabaseNode("node 1");

        PhysicalLocality databaseNode3Loc = databaseNode2.newloc(databaseNode3);
        PhysicalLocality databaseNode2Loc = databaseNode1.newloc(databaseNode2);
        PhysicalLocality databaseNode1Loc = mainDatabaseNode
                .newloc(databaseNode1);

        KString item = new KString("item");

        mainDatabaseNode.out(new Tuple(item, databaseNode1Loc));
        databaseNode1.out(new Tuple(item, databaseNode2Loc));
        databaseNode2.out(new Tuple(item, databaseNode3Loc));
        databaseNode3.out(new Tuple(item, new KString("item val")));
    }

    /**
     * @param args
     * @throws IMCException
     * @throws KlavaException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");

        if (args.length > 0)
            serverLoc = new PhysicalLocality(args[0]);

        new DatabaseExample(serverLoc);
    }

}
