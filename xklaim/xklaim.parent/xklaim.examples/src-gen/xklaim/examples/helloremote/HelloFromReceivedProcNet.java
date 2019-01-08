package xklaim.examples.helloremote;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaProcess;
import klava.topology.LogicalNet;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

/**
 * The Writer node adds a tuple to its tuple space, the Reader sends a
 * process to the Writer node to retrieve the tuple. The Writer node retrieves
 * the migrating process and executes it locally.
 * 
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class HelloFromReceivedProcNet extends LogicalNet {
  private static final LogicalLocality reader = new LogicalLocality("reader");
  
  private static final LogicalLocality writer = new LogicalLocality("writer");
  
  /**
   * Sends a process to the Writer node to retrieve the tuple
   */
  public static class Reader extends ClientNode {
    private static class ReaderProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        KlavaProcess _Proc = new KlavaProcess() {
          private KlavaProcess _initFields() {
            return this;
          }
          @Override public void executeProcess() {
            {
              String s = null;
              Tuple _Tuple = new Tuple(new Object[] {String.class});
              in(_Tuple, this.self);
              s = (String) _Tuple.getItem(0);
              InputOutput.<String>println(s);
              System.exit(0);
            }
          }
        }._initFields();
        out(new Tuple(new Object[] {_Proc}), HelloFromReceivedProcNet.writer);
      }
    }
    
    public Reader() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("reader"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new HelloFromReceivedProcNet.Reader.ReaderProcess());
    }
  }
  
  /**
   * Retrieves the migrating process and executes it locally.
   */
  public static class Writer extends ClientNode {
    private static class WriterProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        out(new Tuple(new Object[] {"Hello World"}), this.self);
        KlavaProcess P = null;
        Tuple _Tuple = new Tuple(new Object[] {KlavaProcess.class});
        in(_Tuple, this.self);
        P = (KlavaProcess) _Tuple.getItem(0);
        InputOutput.<String>println(("Received proc: " + P));
        eval(P, this.self);
      }
    }
    
    public Writer() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("writer"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new HelloFromReceivedProcNet.Writer.WriterProcess());
    }
  }
  
  public HelloFromReceivedProcNet() throws IMCException {
    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
  }
  
  public void addNodes() throws IMCException {
    HelloFromReceivedProcNet.Reader reader = new HelloFromReceivedProcNet.Reader();
    HelloFromReceivedProcNet.Writer writer = new HelloFromReceivedProcNet.Writer();
    reader.addMainProcess();
    writer.addMainProcess();
  }
}
