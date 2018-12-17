/*
 * Created on Feb 24, 2005
 */
package examples.layer;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.ProtocolStack;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Uses a Marshaler attached to System.out and an UnMarshaler
 * attached to System.in.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LayerOutInExample {
	public static void main(String args[]) throws Exception {
		ProtocolStack protocolStack = new ProtocolStack();
		protocolStack.insertLayer(new OutInLayer());
		protocolStack.insertLayer(new ProtocolLayerEndPoint(new IMCUnMarshaler(System.in), new IMCMarshaler(System.out)));
		
		String readline;
		UnMarshaler unMarshaler;
		Marshaler marshaler;
		
		do {
			unMarshaler = protocolStack.createUnMarshaler();
			readline = unMarshaler.readStringLine();
			marshaler = protocolStack.createMarshaler();
			marshaler.writeStringLine("read:\n" + readline);
			protocolStack.releaseMarshaler(marshaler);
		} while (true);
	}
}
