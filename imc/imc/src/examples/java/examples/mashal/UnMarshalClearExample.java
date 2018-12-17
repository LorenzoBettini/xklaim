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
 * attached to a buffer and uses clear to discard the remaining
 * bytes in case of errors.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class UnMarshalClearExample {
	public static void main(String args[]) throws Exception {		
		UnMarshaler unMarshaler = new IMCUnMarshaler(System.in);
		Marshaler marshaler = new IMCMarshaler(System.out);
		
		do {
			StringBuffer buffer = new StringBuffer();
			byte b;
			do {
				b = unMarshaler.readByte();
				if (b == '\n')
					break;
				if (b != 'a') {
					unMarshaler.clear();
					break;
				}
				buffer.append((char)b);
			} while (true);
			marshaler.writeStringLine("input: " + buffer.toString());
		} while (true);
	}
}
