/*
 * Created on Mar 29, 2006
 */
package klava.examples.newsgatherer;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.events.EventCounter;
import org.mikado.imc.events.PrintEventListener;
import org.mikado.imc.events.RouteEvent;

import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.LogicalNet;

/**
 * An example application for NewsGatherer
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class NewsGathererApplication {

    /**
     * @param serverLoc
     * @throws IMCException
     * @throws KlavaException
     * @throws InterruptedException
     */
    public NewsGathererApplication(PhysicalLocality serverLoc)
            throws KlavaException, IMCException, InterruptedException {
        LogicalNet newsNet = new LogicalNet(serverLoc);
        EventCounter eventCounter = new EventCounter();
        newsNet.addListener(RouteEvent.ROUTE_EVENT, eventCounter);
        newsNet.addListener(RouteEvent.ROUTE_EVENT, new PrintEventListener());

        new DatabaseExample(serverLoc);

        NewsClientNode newsClientNode = new NewsClientNode("news client",
                serverLoc);

        // wait for all nodes to become known at the net server
        eventCounter.waitForEventNumber(10);

        // this will give the initial information to the NewsGatherer
        KString item = new KString("item");
        newsClientNode.out(new Tuple(item, newsClientNode
                .getPhysical(new LogicalLocality("main node"))));

        LogicalLocality clientLocScreen = new LogicalLocality("screen");
        LogicalLocality clientLoc = new LogicalLocality("news client");
        newsClientNode.addNodeProcess(new NewsGatherer(item, clientLoc,
                newsClientNode.getPhysical(clientLocScreen)));

        // now wait for response from the NewsGatherer
        Tuple responseFromGatherer = new Tuple(item, new KString());
        newsClientNode.in(responseFromGatherer);
        newsClientNode.out(new Tuple("gatherer's result: "), clientLocScreen);
        newsClientNode.out(responseFromGatherer, clientLocScreen);
    }

    /**
     * @param args
     * @throws IMCException
     * @throws KlavaException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws KlavaException, IMCException,
            InterruptedException {
        PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");

        if (args.length > 0)
            serverLoc = new PhysicalLocality(args[0]);

        new NewsGathererApplication(serverLoc);
    }

}
