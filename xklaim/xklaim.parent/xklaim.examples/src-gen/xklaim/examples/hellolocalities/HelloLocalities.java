package xklaim.examples.hellolocalities;

@SuppressWarnings("all")
public class HelloLocalities {
  public static void main(final String[] args) throws Exception {
    HelloLocalitiesNet helloLocalitiesNet = new HelloLocalitiesNet();
    helloLocalitiesNet.addNodes();
    helloLocalitiesNet.waitForCompletion();
  }
}
