package org.mikado.imc.mobility;

import java.io.*;

import java.util.*;

import org.mikado.imc.log.MessagePrinters;


/**
 * The standard implementation of the MigratingCodeRecover interface
 *
 * @author $author$
 * @version $Revision: 1.6 $
 */
public class MigratingCodeRecoverImpl implements MigratingCodeRecover {
    /** the packet from where to deserialize the object and its classes */
    protected JavaMigratingPacket pack;

    /** the message printer associated */
    protected MessagePrinters messagePrinters;

    /**
     * set the packet where the object is stored
     *
     * @param pack the packet containing the object to deserialize
     */
    public void set_packet(JavaMigratingPacket pack) {
        this.pack = pack;
    }

    /**
     * set the message printer for this MigratingCodeRecover
     *
     * @param printer the message printer
     */
    public void setMessagePrinters(MessagePrinters printer) {
        messagePrinters = printer;
    }

    /**
     * recover an object (deserialize it and load its classes) from a
     * JavaMigratingPacket
     *
     * @return the recovered object
     *
     * @throws ClassNotFoundException
     * @throws java.io.IOException
     * @throws SecurityException
     */
    public JavaMigratingCode recover()
        throws ClassNotFoundException, IOException, SecurityException {
        PrintMessage("Recovering code ...");

        NodeClassLoader classLoader = (NodeClassLoader) getClass()
                                                            .getClassLoader();
        setMessagePrinters(classLoader.getMessagePrinters());

        if (classLoader instanceof NodeClassLoader) {
            classLoader.addClassBytes(pack.className, pack.classBytes);

            Hashtable<String, byte[]> usedClasses = pack.usedClasses;

            if (usedClasses != null) {
                Enumeration<String> en = usedClasses.keys();
                String classname;

                while (en.hasMoreElements()) {
                    classname = en.nextElement();
                    classLoader.addClassBytes(classname,
                        (byte[]) usedClasses.get(classname));
                    PrintMessage("Added class : " + classname);
                }
            }
        }

        return deserializePacket();
    }

    /**
     * Deserialize the JavaMigratingCode contained in the packet
     *
     * @return the deserialized JavaMigratingCode contained in the packet
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected JavaMigratingCode deserializePacket()
        throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIStream = new ByteArrayInputStream(pack.getObjectBytes());
        ObjectInputStream objIStream = new ObjectInputStream(byteIStream);

        // during the next deserialization, the dynamic loading will
        // automatically take place in background
        return (JavaMigratingCode) objIStream.readObject();
    }

    /**
     * Print a message through the associated message printer
     *
     * @param s the string to print
     */
    protected void PrintMessage(String s) {
        if (messagePrinters != null) {
            messagePrinters.Print(getClass().getName() + ": " + s);
        }
    }
}
