/*
 * Created on Mar 23, 2006
 */
package klava.examples.topology;

import org.mikado.imc.common.IMCException;

import klava.KInteger;
import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.examples.process.InOutProcess;
import klava.topology.KlavaNode;

/**
 * There are two nodes, one logged to the other, via a newloc, and a process
 * performs an in from one node and outs result to the other.
 * 
 * @author Lorenzo Bettini
 * @version $Revision $
 */
public class ConnectedWithNewloc {

    public static void main(String[] args) throws IMCException, KlavaException {
        KlavaNode serverNode = new KlavaNode();

        /* insert a tuple in the server node */
        Tuple tuple = new Tuple(new KString("foo"), new KInteger(10));
        serverNode.out(tuple);

        KlavaNode clientNode = new KlavaNode();

        /*
         * this logs the client to the server and returns the locality of the
         * client
         */
        PhysicalLocality clientLoc = serverNode.newloc(clientNode);

        /*
         * the process will look for the tuple at the server and send the result
         * to the client
         */
        InOutProcess inOutProcess = new InOutProcess(new Tuple(new KString(),
                new KInteger()));
        inOutProcess.outDestination = clientLoc;
        serverNode.addNodeProcess(inOutProcess);

        /* let's wait for the response at the client */
        Tuple result = new Tuple(new KString(), new KInteger());
        clientNode.in(result);

        System.out.println("result: " + result);
        System.exit(0);
    }

}
