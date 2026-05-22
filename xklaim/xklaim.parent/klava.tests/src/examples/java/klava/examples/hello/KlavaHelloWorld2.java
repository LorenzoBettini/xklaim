package klava.examples.hello;

import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;
import klava.topology.Net;

public class KlavaHelloWorld2 {

	public static void main(String[] args) throws Exception {
		PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");
		KlavaNode serverNode = new Net(serverLoc);

		/* will automatically log to the server */
		KlavaNode clientNode = new ClientNode(serverLoc);

		serverNode.addNodeProcess(new KlavaProcess() {
			@Override
			public void executeProcess() throws KlavaException {
				Tuple tuple = new Tuple(new Object[] {String.class});
				in(tuple, self);
				System.out.println("in: " + tuple.getItem(0));
			}
		});
		
		clientNode.addNodeProcess(new KlavaProcess() {
			@Override
			public void executeProcess() throws KlavaException {
				out(new Tuple(new Object[] {"Hello World!"}), serverLoc);
			}
		});
	}

}
