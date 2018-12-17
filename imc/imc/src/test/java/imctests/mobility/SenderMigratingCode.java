package imctests.mobility;

import java.net.*;
import java.io.*;

import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.log.FileMessagePrinter;
import org.mikado.imc.mobility.*;

/**
 * <p>Title: Mikado Mobility package</p>
 * <p>Description: provide classes for moving code to another site</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dip. Sistemi e Informatica, Univ. Firenze</p>
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public class SenderMigratingCode extends Thread {
  protected String remote_host;
  protected int remote_port;
  protected int local_port;
  protected BufferedReader in;
  protected BufferedWriter out;

  public SenderMigratingCode(int port) {
    local_port = port;
  }

  public void listen() throws IOException, ClassNotFoundException {
    ServerSocket server_socket = new ServerSocket(local_port);
    System.out.println("started listening...");
    Socket socket = server_socket.accept();
    System.out.println("received connection...");
    InputStream sock_in = socket.getInputStream();
    OutputStream sock_out = socket.getOutputStream();
    ObjectInputStream obj_in =
        new ObjectInputStream(new BufferedInputStream(sock_in));
    ObjectOutputStream obj_out =
        new ObjectOutputStream(new BufferedOutputStream(sock_out));
    obj_out.flush();
    String req_string = obj_in.readObject().toString();
    String classname;
    if (! req_string.startsWith("GET ")) {
      obj_out.writeObject("Command \"" + req_string + "\" not recognized");
      obj_out.flush();
      obj_out.close();
      return;
    } else {
      classname = req_string.substring(3).trim();
    }
    try {
      Object o = Class.forName(classname).newInstance();
      JavaMigratingCode code = (JavaMigratingCode)o;
      code.addMessagePrinter
          (new FileMessagePrinter
           ("SenderMigratingCode.out"));
      code.addMessagePrinter(new DefaultMessagePrinter());
      code.setExcludeClasses(code.getClass().getSuperclass().getName());
	  MigratingCodeMarshaler marshaler = new JavaByteCodeMarshaler();
	  MigratingPacket pack = marshaler.marshal(code);
      obj_out.writeObject("SEND " + classname);
      obj_out.flush();
      obj_out.writeObject(pack);
      obj_out.flush();
      String rec_string = obj_in.readObject().toString();
      System.out.println(rec_string);
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
      obj_out.writeObject("ERROR " + classname + ": " + e.getMessage());
      obj_out.flush();
    } finally {
      socket.close();
    }
  }

  public void run() {
    try {
      listen();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    SenderMigratingCode senderMigratingCode1 = new SenderMigratingCode(9999);
    senderMigratingCode1.listen();
  }
}