package imctests.mobility;

import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.log.FileMessagePrinter;
import org.mikado.imc.log.MessagePrinter;
import org.mikado.imc.mobility.*;
import java.io.*;

/**
 * <p>
 * Title: Mikado Mobility package
 * </p>
 * <p>
 * Description: provide classes for moving code to another site
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Dip. Sistemi e Informatica, Univ. Firenze
 * </p>
 * 
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public class FileMigratingCodeRecover {
    public FileMigratingCodeRecover() {
    }

    public static void main(String args[]) throws Exception {
        NodeClassLoader classloader = new NodeClassLoader(true); // force
                                                                    // loading
        MessagePrinter printer = new FileMessagePrinter(
                "FileMigratingCodeRecover.out");

        classloader.addMessagePrinter(printer);
        classloader.addMessagePrinter(new DefaultMessagePrinter());
        // classloader.setMessages(true);

        JavaByteCodeUnMarshaler unmarshaler = new JavaByteCodeUnMarshaler();
        unmarshaler.setNodeClassLoader(classloader);

        String recover_name = "org.mikado.imc.mobility.MigratingCodeRecoverImpl";
        Class<?> c = classloader.forceLoadClass(recover_name, true);
        MigratingCodeRecover recover = (MigratingCodeRecover) (c.newInstance());
        System.out.println("recover class: "
                + recover.getClass().getCanonicalName());

        // now reload the contents of the file containing a serialized
        // JavaMigratingPacket
        FileInputStream input = new FileInputStream("MyMigratingCodeBytes.bin");
        ObjectInputStream obj_input = new ObjectInputStream(input);
        JavaMigratingPacket pack = (JavaMigratingPacket) obj_input.readObject();

        MyMigratingCode code = (MyMigratingCode) unmarshaler.unmarshal(pack);
        printer.Print("calling bar method, result: " + code.bar());
    }
}