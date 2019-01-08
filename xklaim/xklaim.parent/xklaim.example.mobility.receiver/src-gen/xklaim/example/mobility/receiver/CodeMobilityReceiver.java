package xklaim.example.mobility.receiver;

import xklaim.example.mobility.receiver.Receiver;

@SuppressWarnings("all")
public class CodeMobilityReceiver {
  public static void main(final String[] args) throws Exception {
    Receiver receiver = new Receiver();
    receiver.addMainProcess();
  }
}
