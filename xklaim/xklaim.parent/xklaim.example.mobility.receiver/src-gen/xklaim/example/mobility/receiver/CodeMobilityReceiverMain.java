package xklaim.example.mobility.receiver;

@SuppressWarnings("all")
public class CodeMobilityReceiverMain {
  public static void main(final String[] args) throws Exception {
    Receiver receiver = new Receiver();
    receiver.addMainProcess();
    receiver.waitForCompletion();
  }
}
