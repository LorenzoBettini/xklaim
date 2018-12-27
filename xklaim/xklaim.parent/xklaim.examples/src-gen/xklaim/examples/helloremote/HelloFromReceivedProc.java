package xklaim.examples.helloremote;

import xklaim.examples.helloremote.HelloFromReceivedProcNet;

@SuppressWarnings("all")
public class HelloFromReceivedProc {
  public static void main(final String[] args) throws Exception {
    HelloFromReceivedProcNet helloFromReceivedProcNet = new HelloFromReceivedProcNet();
    helloFromReceivedProcNet.addNodes();
  }
}
