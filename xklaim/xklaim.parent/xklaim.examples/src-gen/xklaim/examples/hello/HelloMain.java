package xklaim.examples.hello;

@SuppressWarnings("all")
public class HelloMain {
  public static void main(final String[] args) throws Exception {
    HelloNet helloNet = new HelloNet();
    helloNet.addNodes();
    helloNet.waitForCompletion();
  }
}
