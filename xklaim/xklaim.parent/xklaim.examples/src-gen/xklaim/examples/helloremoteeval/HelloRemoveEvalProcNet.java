package xklaim.examples.helloremoteeval;

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
 * The Writer node adds a tuple to its tuple space, the Reader evaluates a
 * process to the Writer node to retrieve the tuple.
 * 
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class HelloRemoveEvalProcNet extends LogicalNet {
  private static final LogicalLocality reader = new LogicalLocality("reader");
  
  private static final LogicalLocality writer = new LogicalLocality("writer");
  
  /**
   * Evaluates a process to the Writer node to retrieve the tuple
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
              PhysicalLocality _physical = this.getPhysical(HelloRemoveEvalProcNet.writer);
              String _plus = ("executing at " + _physical);
              InputOutput.<String>println(_plus);
              String s = null;
              Tuple _Tuple = new Tuple(new Object[] {String.class});
              in(_Tuple, this.self);
              s = (String) _Tuple.getItem(0);
              InputOutput.<String>println(s);
              System.exit(0);
            }
          }
        }._initFields();
        eval(_Proc, HelloRemoveEvalProcNet.writer);
      }
    }
    
    public Reader() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("reader"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new HelloRemoveEvalProcNet.Reader.ReaderProcess());
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
      addNodeCoordinator(new HelloRemoveEvalProcNet.Writer.WriterProcess());
    }
  }
  
  public HelloRemoveEvalProcNet() throws IMCException {
    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
  }
  
  public void addNodes() throws IMCException {
    HelloRemoveEvalProcNet.Reader reader = new HelloRemoveEvalProcNet.Reader();
    HelloRemoveEvalProcNet.Writer writer = new HelloRemoveEvalProcNet.Writer();
    reader.addMainProcess();
    writer.addMainProcess();
  }
}
