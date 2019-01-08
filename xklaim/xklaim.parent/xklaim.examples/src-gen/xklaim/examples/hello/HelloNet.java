package xklaim.examples.hello;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.LogicalNet;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

/**
 * The Writer node adds a tuple to its tuple space, the Reader node tries
 * to retrieve it from Writer.
 * 
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class HelloNet extends LogicalNet {
  private static final LogicalLocality reader = new LogicalLocality("reader");
  
  private static final LogicalLocality writer = new LogicalLocality("writer");
  
  /**
   * Tries to retrieve a matching tuple from the Writer node.
   */
  public static class Reader extends ClientNode {
    private static class ReaderProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        String s = null;
        Tuple _Tuple = new Tuple(new Object[] {String.class});
        in(_Tuple, HelloNet.Reader.writerLoc);
        s = (String) _Tuple.getItem(0);
        InputOutput.<String>println(s);
        System.exit(0);
      }
    }
    
    private static final LogicalLocality writerLoc = new LogicalLocality("writerLoc");
    
    public Reader() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("reader"));
    }
    
    public void setupEnvironment() {
      addToEnvironment(writerLoc, getPhysical(HelloNet.writer));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new HelloNet.Reader.ReaderProcess());
    }
  }
  
  public static class Writer extends ClientNode {
    private static class WriterProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        out(new Tuple(new Object[] {"Hello World"}), this.self);
      }
    }
    
    public Writer() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("writer"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new HelloNet.Writer.WriterProcess());
    }
  }
  
  public HelloNet() throws IMCException {
    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
  }
  
  public void addNodes() throws IMCException {
    HelloNet.Reader reader = new HelloNet.Reader();
    HelloNet.Writer writer = new HelloNet.Writer();
    reader.setupEnvironment();
    reader.addMainProcess();
    writer.addMainProcess();
  }
}
