/**
 *   Testcase.java  version 1.0  Feb 10, 2009
 */
package klava.examples.db;

import org.mikado.imc.common.IMCException;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import klava.topology.LogicalNet;

/**
 * Place class description here
 * 
 * @version 1.0
 * @author Fan Yang
 * 
 */
public class Testcase {

    /**
     * @param args
     * @throws KlavaException
     * @throws IMCException
     */
    public static void main(String[] args) throws KlavaException, IMCException {
        // Initialize constants
        PhysicalLocality server = new PhysicalLocality("tcp-127.0.0.1:9999");
        LogicalLocality userA = new LogicalLocality("userA");
        LogicalLocality userB = new LogicalLocality("userB");
        LogicalLocality db = new LogicalLocality("db");

        // Initialize Server node
        KlavaNode servernode = new LogicalNet(server);

        // /////////////////Initialize application/////////////////////////////
        DataNode dbnode = new DataNode(server, db);

         pause(100);

        createUserNode(server, "userA");
        createUserNode(server, "userB");
        createUserNode(server, "userC");
        createUserNode(server, "userD");
        createUserNode(server, "userE");
        createUserNode(server, "userF");
        createUserNode(server, "userG");
        createUserNode(server, "userH");
        createUserNode(server, "userI");
        createUserNode(server, "userJ");

        // pause(100); //uncomment this will solve the problem

        //createUserNode(server, userB);

        pause(2000);

        System.out.println("-----------------------------------------------");

        dbnode.printTupleSpace();

        System.exit(0);
        
        //userAnode.close();
        //userBnode.close();
        //dbnode.close();
        //servernode.close();

    }

    /**
     * @param server
     * @param userA
     * @throws KlavaException
     */
    private static void createUserNode(PhysicalLocality server,
            String userA) throws KlavaException {
        new UserNode(server, new LogicalLocality(userA));
    }

    public static void pause(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}
