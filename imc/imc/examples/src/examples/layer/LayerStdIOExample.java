/*
 * Created on Feb 24, 2005
 */
package examples.layer;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.ProtocolLayerEndPoint;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Uses a Marshaler attached to System.out and an UnMarshaler
 * attached to System.in.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class LayerStdIOExample {
	public static void main(String args[]) throws Exception {
		ProtocolLayer protocolLayer = 
			new ProtocolLayerEndPoint(new IMCUnMarshaler(System.in), new IMCMarshaler(System.out));
		
		String readline;
		UnMarshaler unMarshaler;
		Marshaler marshaler;
		
		do {
			unMarshaler = protocolLayer.doCreateUnMarshaler(null);
			readline = unMarshaler.readStringLine();
			marshaler = protocolLayer.doCreateMarshaler(null);
			marshaler.writeStringLine("input: " + readline);
			protocolLayer.doReleaseMarshaler(marshaler);
		} while (true);
	}
}
