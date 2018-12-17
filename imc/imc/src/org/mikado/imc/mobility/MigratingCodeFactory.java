/*
 * Created on 10-dic-2004
 */
package org.mikado.imc.mobility;

/**
 * Abstract factory to create MigratingCodeMarshaler and
 * MigratinCodeUnMarshaler.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public interface MigratingCodeFactory {
	/**
	 * Creates a MigratingCodeMarshaler.
	 * 
	 * @return the created MigratingCodeMarshaler
	 */
	MigratingCodeMarshaler createMigratingCodeMarshaler();

	/**
	 * Creates a MigratingCodeUnMarshaler.
	 * 
	 * @return the created MigratingCodeUnMarshaler
	 */
	MigratingCodeUnMarshaler createMigratingCodeUnMarshaler();
}
