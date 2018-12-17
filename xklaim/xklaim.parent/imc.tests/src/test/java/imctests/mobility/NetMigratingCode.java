package imctests.mobility;

/**
 * <p>Title: Mikado Mobility package</p>
 * <p>Description: provide classes for moving code to another site</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dip. Sistemi e Informatica, Univ. Firenze</p>
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public class NetMigratingCode {
  public static void main(String args[]) throws Exception {
    SenderMigratingCode senderMigratingCode1 = new SenderMigratingCode(9999);
    senderMigratingCode1.start();
    String host = "localhost";
    int port = 9999;
    String classname = "imctests.mobility.MyMigratingCodeImpl";
    ReceiverMigratingCode receiverMigratingCode1 =
        new ReceiverMigratingCode(host, port, classname);
    receiverMigratingCode1.start();
  }
}