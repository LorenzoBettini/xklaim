package xklaim.examples.hello;

import xklaim.examples.hello.HelloNet;

@SuppressWarnings("all")
public class Hello {
  public static void main(final String[] args) throws Exception {
    HelloNet helloNet = new HelloNet();
    helloNet.addNodes();
  }
}
