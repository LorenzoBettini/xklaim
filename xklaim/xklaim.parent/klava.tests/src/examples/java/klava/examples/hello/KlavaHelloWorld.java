package klava.examples.hello;

import klava.KString;
import klava.KlavaException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;
import klava.topology.Net;

public class KlavaHelloWorld {

	public static void main(String[] args) throws Exception {
		PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");
		KlavaNode serverNode = new Net(serverLoc);

		/* will automatically log to the server */
		KlavaNode clientNode = new ClientNode(serverLoc);

		serverNode.addNodeProcess(new KlavaProcess() {
			private static final long serialVersionUID = 1L;

            @Override
			public void executeProcess() throws KlavaException {
				in(new Tuple(new KString()), self);
			}
		});
		
		clientNode.addNodeProcess(new KlavaProcess() {
			private static final long serialVersionUID = 1L;

            @Override
			public void executeProcess() throws KlavaException {
				out(new Tuple(new KString("Hello World!")), serverLoc);
			}
		});
	}

}
