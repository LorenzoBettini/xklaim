/**
 * MIKADO Project
 * IMC Framework
 * Communication Protocols Package
 * Adapted from the Jonathan Object Request Broker Distribution (ObjectWeb Consortium)
 * Adaptation: Marc Lacoste
 * Copyright (C) 2003 France Telecom R&D
 */
package org.mikado.imc.protocols;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.mikado.imc.common.IMCException;

/**
 * ProtocolException is the supertype of all exceptions raised by the communication package. 
 * It can be used to wrap an exception of another type.
 */
public class ProtocolException extends IMCException {
	private static final long serialVersionUID = 3257562914867984439L;
	Exception actual;

	/**
	 * Constructs a new ProtocolException.
	 */
	public ProtocolException() {
		super();
		actual = null;
	}

	/**
	 * Constructs a new ProtocolException with an error message.
	 */
	public ProtocolException(String s) {
		super(s);
		actual = null;
	}

	/**
	 * Constructs a new ProtocolException that wraps another exception. 
	 */
	public ProtocolException(Exception exception) {
		if (exception instanceof ProtocolException) {
			actual = ((ProtocolException) exception).represents();
		} else {
			actual = exception;
		}
	}

	/**
	 * Returns the error message of the exception, or the error message of the 
	 * exception it represents if this exception is a wrapper for another exception.
	 */
	public String getMessage() {
		if (actual != null) {
			return actual.getMessage();
		} else {
			return super.getMessage();
		}
	}

	/**
     * Returns a string representation of the exception.
	 */
	public String toString() {
		if (actual != null) {
			return actual.toString();
		} else {
			return super.toString();
		}
	}

	/**
     * Prints the execution stack of the exception to the current output.
	 */
	public void printStackTrace() {
		if (actual != null) {
			actual.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}

	/**
	 * Prints the execution stack of the exception to a print stream.
	 */
	public void printStackTrace(PrintStream s) {
		if (actual != null) {
			actual.printStackTrace(s);
		} else {
			super.printStackTrace(s);
		}
	}

	/**
	 * Prints the execution stack of the exception to a print writer.
	 */
	public void printStackTrace(PrintWriter s) {
		if (actual != null) {
			actual.printStackTrace(s);
		} else {
			super.printStackTrace(s);
		}
	}

	/**
	 * Returns the exception this MikadoProtocolException represents, if any. 
	 * Otherwise, the exception itself is returned.
	 */  
	public Exception represents() {
		if (actual != null) {
			return actual;
		} else {
			return this;
		}
	}
}
