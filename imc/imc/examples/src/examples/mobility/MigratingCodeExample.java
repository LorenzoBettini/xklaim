/*
 * Created on Feb 24, 2005
 */
package examples.mobility;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.mikado.imc.mobility.JavaByteCodeMigratingCodeFactory;
import org.mikado.imc.mobility.MigratingCode;
import org.mikado.imc.mobility.NodeClassLoader;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.Marshaler;
import org.mikado.imc.protocols.UnMarshaler;

/**
 * Uses a Marshaler attached to a file to store and retrieve "mobile" code.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class MigratingCodeExample {
    public static void main(String args[]) throws Exception {
        JavaByteCodeMigratingCodeFactory javaByteCodeMigratingCodeFactory = new JavaByteCodeMigratingCodeFactory();
        javaByteCodeMigratingCodeFactory
                .setNodeClassLoader(new NodeClassLoader(true));
        Marshaler marshaler = new IMCMarshaler(new FileOutputStream("foo.bin"),
                javaByteCodeMigratingCodeFactory);

        marshaler.writeMigratingCode(new MyMobileCode());
        marshaler.flush();

        UnMarshaler unMarshaler = new IMCUnMarshaler(new FileInputStream(
                "foo.bin"), javaByteCodeMigratingCodeFactory);

        MigratingCode migratingCode = unMarshaler.readMigratingCode();
        System.out.println("migrating code class: "
                + migratingCode.getClass().getCanonicalName());
    }
}
