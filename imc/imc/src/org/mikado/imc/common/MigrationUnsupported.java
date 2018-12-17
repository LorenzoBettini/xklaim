/*
 * Created on 10-dic-2004
 */
package org.mikado.imc.common;

/**
 * Thrown if migrating of code is not supported
 *
 * @author bettini
 */
public class MigrationUnsupported extends IMCException {
	private static final long serialVersionUID = 3256439218178961459L;

	public MigrationUnsupported(String detail) {
		super(detail);
	}

	public MigrationUnsupported(String detail, Throwable cause) {
		super(detail, cause);
	}
}
