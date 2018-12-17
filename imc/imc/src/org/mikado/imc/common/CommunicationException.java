package org.mikado.imc.common;

import org.mikado.imc.common.CommunicationException;

public class CommunicationException extends IMCException {
	private static final long serialVersionUID = 3618140035141087794L;

	public CommunicationException(String detail) {
		super(detail);
	}

	public CommunicationException(String detail, Throwable cause) {
		super(detail, cause);
	}
}
