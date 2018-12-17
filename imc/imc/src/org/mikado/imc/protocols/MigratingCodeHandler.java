/*
 * Created on Jan 17, 2005
 *
 */
package org.mikado.imc.protocols;

import org.mikado.imc.mobility.MigratingCodeFactory;


/**
 * Interface to be implemented by those classes that can deal with migrating
 * code (e.g., send and receive migrating code).
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public interface MigratingCodeHandler {
    /**
     * Returns the factory used to create a MigratingCodeMarshaler and
     * MigratingCodeUnMarshaler.
     *
     * @return The factory used to create a MigratingCodeMarshaler and
     *         MigratingCodeUnMarshaler.
     */
    MigratingCodeFactory getMigratingCodeFactory();

    /**
     * Sets the factory used to create a MigratingCodeMarshaler and
     * MigratingCodeUnMarshaler.
     *
     * @param migratingCodeFactory The factory used to create a
     *        MigratingCodeMarshaler and MigratingCodeUnMarshaler.
     */
    void setMigratingCodeFactory(MigratingCodeFactory migratingCodeFactory);
}
