/*
 * Created on 12-May-06
 */
/**
 * 
 */
package org.mikado.imc.mobility;

import org.mikado.imc.log.DefaultMessagePrinter;

/**
 * A specialization of JavaByteCodeMigratingCodeFactory that adds verbosity.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class JavaByteCodeMigratingCodeFactoryVerbose extends
        JavaByteCodeMigratingCodeFactory {

    /**
     * Creates verbose NodeClassLoaders
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public class NodeClassLoaderFactoryVerbose extends NodeClassLoaderFactory {

        /**
         * @see org.mikado.imc.mobility.NodeClassLoaderFactory#createNodeClassLoader()
         */
        @Override
        NodeClassLoader createNodeClassLoader() {
            NodeClassLoader nodeClassLoader = super.createNodeClassLoader();
            nodeClassLoader.addMessagePrinter(new DefaultMessagePrinter());
            nodeClassLoader.setMessages(true);
            return nodeClassLoader;
        }
    }

    /**
     * 
     */
    public JavaByteCodeMigratingCodeFactoryVerbose() {
        super();
    }

    /**
     * @see org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactory#createMigratingCodeMarshaler()
     */
    @Override
    public MigratingCodeMarshaler createMigratingCodeMarshaler() {
        return super.createMigratingCodeMarshaler();
    }

    /**
     * @see org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactory#createMigratingCodeUnMarshaler()
     */
    @Override
    public MigratingCodeUnMarshaler createMigratingCodeUnMarshaler() {
        JavaByteCodeUnMarshaler javaByteCodeUnMarshaler = (JavaByteCodeUnMarshaler) super
                .createMigratingCodeUnMarshaler();
        javaByteCodeUnMarshaler
                .setNodeClassLoaderFactory(new NodeClassLoaderFactoryVerbose());
        return javaByteCodeUnMarshaler;
    }

}
