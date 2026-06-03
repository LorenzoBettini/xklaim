package xklaim.examples.helloremoteeval;

@SuppressWarnings("all")
public class HelloRemoveEvalProcMain {
  public static void main(final String[] args) throws Exception {
    HelloRemoveEvalProcNet helloRemoveEvalProcNet = new HelloRemoveEvalProcNet();
    helloRemoveEvalProcNet.addNodes();
    helloRemoveEvalProcNet.waitForCompletion();
  }
}
