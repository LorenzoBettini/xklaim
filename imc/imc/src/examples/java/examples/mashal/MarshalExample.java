/*
 * Created on Feb 24, 2005
 */
package examples.mashal;

import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Uses a Marshaler attached to System.out and an UnMarshaler
 * attached to System.in.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class MarshalExample {
	public static void main(String args[]) throws Exception {
		UnMarshaler unMarshaler = new IMCUnMarshaler(System.in);
		Marshaler marshaler = new IMCMarshaler(System.out);
		
		String readline;
		do {
			readline = unMarshaler.readStringLine();
			marshaler.writeStringLine("input: " + readline);
		} while (true);
	}
}
