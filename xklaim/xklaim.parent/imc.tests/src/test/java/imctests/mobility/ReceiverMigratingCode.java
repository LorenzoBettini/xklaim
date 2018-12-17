package imctests.mobility;

import java.net.*;
import java.io.*;

import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.log.FileMessagePrinter;
import org.mikado.imc.log.MessagePrinter;
import org.mikado.imc.mobility.*;

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

public class ReceiverMigratingCode extends Thread {
    protected String host;

    protected int port;

    protected String classname;

    public ReceiverMigratingCode(String host, int port, String classname) {
        this.host = host;
        this.port = port;
        this.classname = classname;
    }

    public void connect() throws UnknownHostException, IOException,
            ClassNotFoundException {
        System.out.println("connecting to " + host + ":" + port + "...");
        Socket socket = new Socket(host, port);
        System.out.println("connected...");
        System.out.println("requesting object of class " + classname);
        InputStream sock_in = socket.getInputStream();
        OutputStream sock_out = socket.getOutputStream();
        ObjectOutputStream obj_output = new ObjectOutputStream(
                new BufferedOutputStream(sock_out));
        obj_output.flush();
        ObjectInputStream obj_input = new ObjectInputStream(
                new BufferedInputStream(sock_in));
        obj_output.writeObject("GET " + classname);
        obj_output.flush();
        String res = obj_input.readObject().toString();
        System.out.println(res);
        if (!res.startsWith("SEND")) {
            return;
        } else {
            try {
                // now read from the socket a serialized JavaMigratingPacket
                JavaMigratingPacket pack = (JavaMigratingPacket) obj_input
                        .readObject();
                NodeClassLoader classloader = new NodeClassLoader(true); // force
                                                                            // loading
                MessagePrinter printer = new FileMessagePrinter(
                        "ReceiverMigratingCode.out");

                classloader.addMessagePrinter(printer);
                classloader.addMessagePrinter(new DefaultMessagePrinter());
                // classloader.setMessages(true);

                JavaByteCodeUnMarshaler unmarshaler = new JavaByteCodeUnMarshaler();
                unmarshaler.setNodeClassLoader(classloader);

                MyMigratingCode code = (MyMigratingCode) unmarshaler
                        .unmarshal(pack);
                System.out.println(code.toString());
                obj_output.writeObject("RECEIVED");
                obj_output.flush();
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }

    public void run() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 9999;
        String classname = "imctests.mobility.MyMigratingCodeImpl";
        ReceiverMigratingCode receiverMigratingCode1 = new ReceiverMigratingCode(
                host, port, classname);
        receiverMigratingCode1.connect();
    }
}