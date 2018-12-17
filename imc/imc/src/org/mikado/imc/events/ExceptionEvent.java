/*
 * Created on May 13, 2005
 */
package org.mikado.imc.events;

/**
 * An event representing an exception.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class ExceptionEvent extends Event {
	/**
	 * @param source
	 */
	public ExceptionEvent(Object source) {
		super(source);
	}

}
