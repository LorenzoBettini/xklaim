/*
 * Created on Feb 25, 2005
 */
package examples.layer;

import java.io.IOException;

import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.ProtocolLayer;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Layer that removes "IN" from the input and adds "OUT" to the output.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class OutInLayer extends ProtocolLayer {

	/**
	 * Constructs an OutInLayer.
	 */
	public OutInLayer() {
	}

	/* (non-Javadoc)
	 * @see org.mikado.imc.protocols.ProtocolLayer#doPrepare(org.mikado.imc.protocols.Marshaler)
	 */
	public Marshaler doCreateMarshaler(Marshaler marshaler) throws ProtocolException {
		try {
			marshaler.writeStringLine("OUT");
			return marshaler;
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mikado.imc.protocols.ProtocolLayer#doUp(org.mikado.imc.protocols.UnMarshaler)
	 */
	public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
			throws ProtocolException {
		try {
			String header = unMarshaler.readStringLine();
			if (!header.equals("IN"))
				throw new ProtocolException("wrong header: " + header);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
		return unMarshaler;
	}
}
