package xklaim.example.mobility.sender;

import xklaim.example.mobility.sender.Sender;

@SuppressWarnings("all")
public class CodeMobilitySender {
  public static void main(final String[] args) throws Exception {
    Sender sender = new Sender();
    sender.setupEnvironment();
    sender.addMainProcess();
  }
}
