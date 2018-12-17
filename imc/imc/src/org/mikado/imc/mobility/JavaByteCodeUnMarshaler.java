package org.mikado.imc.mobility;

import java.io.IOException;


/**
 * An implementation of MigratingCodeUnMarshaler suitable for Java byte code
 * class loading, starting from a JavaMigratingPacket.
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.5 $
 */
public class JavaByteCodeUnMarshaler implements MigratingCodeUnMarshaler {
    /** If set, it will be used to load all the necessary classes from the MigratingPacket */
    private NodeClassLoader fixed_class_loader = null;

    /** Used to create a different class loader for each unmarshalled packet */
    private NodeClassLoaderFactory class_loader_factory = new NodeClassLoaderFactory();

    /** The default class name of the object through which the
     * the migrating object will be unmarshaled */
    private String recover_name = "org.mikado.imc.mobility.MigratingCodeRecoverImpl";

    /**
     * Creates a new JavaByteCodeUnMarshaler object.
     */
    public JavaByteCodeUnMarshaler() {
    }

    /**
     * Creates a new JavaByteCodeUnMarshaler object.
     *
     * @param cl the class loader that will always be used to load classes
     * for migrating objects
     */
    public JavaByteCodeUnMarshaler(NodeClassLoader cl) {
        fixed_class_loader = cl;
    }

    /**
     * @see org.mikado.imc.mobility.MigratingCodeUnMarshaler#unmarshal(org.mikado.imc.mobility.MigratingPacket)
     */
    public MigratingCode unmarshal(MigratingPacket pack)
        throws InstantiationException, IllegalAccessException, 
            ClassNotFoundException, IOException {
        NodeClassLoader class_loader = fixed_class_loader;

        if (class_loader == null) {
            class_loader = class_loader_factory.createNodeClassLoader();
        }

        // create a new class loader each time, if fixed_class_loader is not set
		Class<?> c = class_loader.forceLoadClass(recover_name, true);
        MigratingCodeRecover recover = (MigratingCodeRecover) (c.newInstance());

        recover.set_packet((JavaMigratingPacket) pack);
        recover.setMessagePrinters(class_loader.getMessagePrinters());

        return recover.recover();
    }

    /**
     * If you set the class loader, it will be used to load all the classes of the
     * migrating objects.
     *
     * @param cl the class loader
     */
    public void setNodeClassLoader(NodeClassLoader cl) {
        fixed_class_loader = cl;
    }

    /**
     * This factory will be used to create a different class loader to load the classes
     * of a single migrating object.  This will take place only if the class loader
     * has not been set previously.
     *
     * @param f the factory
     */
    public void setNodeClassLoaderFactory(NodeClassLoaderFactory f) {
        class_loader_factory = f;
    }

    /**
     * Set the name of the class for the object through which the
     * the migrating object will be unmarshaled.
     * By default it is "org.mikado.imc.mobility.MigratingCodeRecoverImpl"
     *
     * @param s the name of the class
     */
    public void setRecoverName(String s) {
        recover_name = s;
    }
}
