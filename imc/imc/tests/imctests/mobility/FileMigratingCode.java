package imctests.mobility;

import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.log.FileMessagePrinter;
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

public class FileMigratingCode extends JavaMigratingCode {
    private static final long serialVersionUID = 3257569520561631794L;

    public static void main(String args[]) throws Exception {
        MyMigratingCodeImpl code = new MyMigratingCodeImpl();
        code.addMessagePrinter(new FileMessagePrinter("FileMigratingCode.out"));
        code.addMessagePrinter(new DefaultMessagePrinter());
        code.setExcludeClasses("mikadotest.mobility.MyMigratingCode");
        MigratingCodeMarshaler marshaler = new JavaByteCodeMarshaler();
        MigratingPacket pack = marshaler.marshal(code);
        FileOutputStream output = new FileOutputStream(
                "MyMigratingCodeBytes.bin");
        ObjectOutputStream obj_out = new ObjectOutputStream(output);
        obj_out.writeObject(pack);
        obj_out.close();
    }
}
