/*
 * Created on 10-dic-2004
 */
package org.mikado.imc.mobility;

/**
 * A MigratingCodeFactory specialized to create JavaByteCodeMarshalers and
 * JavaByteCodeUnMarshalers.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 * 
 * @see JavaByteCodeMarshaler
 * @see JavaByteCodeUnMarshaler
 */
public class JavaByteCodeMigratingCodeFactory implements MigratingCodeFactory {
    /**
     * <p>
     * If set, this class loader will be set in all the created
     * JavaByteCodeUnMarshalers.
     * </p>
     * 
     * <p>
     * IMPORTANT: this has the precedence over the NodeClassLoaderFactory. So,
     * if both are set, this will be used, and the other one will not be
     * considered.
     * </p>
     */
    protected NodeClassLoader nodeClassLoader;

    /**
     * If set, this node class loader factory will be set in all the created
     * JavaByteCodeUnMarshalers.
     */
    protected NodeClassLoaderFactory nodeClassLoaderFactory;

    /**
     * Creates a new JavaByteCodeMigratingCodeFactory object.
     */
    public JavaByteCodeMigratingCodeFactory() {
    }

    /**
     * Constructs a JavaByteCodeMigratingCodeFactory passing a
     * NodeClassLoaderFactory.
     * 
     * @param nodeClassLoaderFactory
     */
    public JavaByteCodeMigratingCodeFactory(
            NodeClassLoaderFactory nodeClassLoaderFactory) {
        this.nodeClassLoaderFactory = nodeClassLoaderFactory;
    }

    /**
     * Constructs a JavaByteCodeMigratingCodeFactory passing a NodeClassLoader.
     * 
     * @param nodeClassLoader
     */
    public JavaByteCodeMigratingCodeFactory(NodeClassLoader nodeClassLoader) {
        this.nodeClassLoader = nodeClassLoader;
    }

    /**
     * @see org.mikado.imc.mobility.MigratingCodeFactory#createMigratingCodeMarshaler()
     */
    public MigratingCodeMarshaler createMigratingCodeMarshaler() {
        JavaByteCodeMarshaler javaByteCodeMarshaler = new JavaByteCodeMarshaler();

        return javaByteCodeMarshaler;
    }

    /**
     * @see org.mikado.imc.mobility.MigratingCodeFactory#createMigratingCodeUnMarshaler()
     */
    public MigratingCodeUnMarshaler createMigratingCodeUnMarshaler() {
        JavaByteCodeUnMarshaler javaByteCodeUnMarshaler = new JavaByteCodeUnMarshaler();

        if (nodeClassLoader != null) {
            javaByteCodeUnMarshaler.setNodeClassLoader(nodeClassLoader);
        } else if (nodeClassLoaderFactory != null) {
            javaByteCodeUnMarshaler
                    .setNodeClassLoaderFactory(nodeClassLoaderFactory);
        }

        return javaByteCodeUnMarshaler;
    }

    /**
     * Returns the node class loader.
     * 
     * @return Returns the node class loader.
     */
    public final NodeClassLoader getNodeClassLoader() {
        return nodeClassLoader;
    }

    /**
     * Sets the class loader that will be set in all the created
     * JavaByteCodeUnMarshalers.
     * 
     * @param nodeClassLoader
     *            The node class loader to set.
     */
    public final void setNodeClassLoader(NodeClassLoader nodeClassLoader) {
        this.nodeClassLoader = nodeClassLoader;
    }

    /**
     * Returns the node class loader factory.
     * 
     * @return Returns the node class loader factory.
     */
    public final NodeClassLoaderFactory getNodeClassLoaderFactory() {
        return nodeClassLoaderFactory;
    }

    /**
     * Sets the node class loader factory that will be set in all the created
     * JavaByteCodeUnMarshalers.
     * 
     * @param nodeClassLoaderFactory
     *            The nodeClassLoaderFactory to set.
     */
    public final void setNodeClassLoaderFactory(
            NodeClassLoaderFactory nodeClassLoaderFactory) {
        this.nodeClassLoaderFactory = nodeClassLoaderFactory;
    }
}
