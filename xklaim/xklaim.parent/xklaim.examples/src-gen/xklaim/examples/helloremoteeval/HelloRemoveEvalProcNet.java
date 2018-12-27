package xklaim.examples.helloremoteeval;

import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaProcess;
import klava.topology.LogicalNet;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

@SuppressWarnings("all")
public class HelloRemoveEvalProcNet extends LogicalNet {
  public static class Reader extends ClientNode {
    private static class ReaderProcess extends KlavaProcess {
      @Override
      public void executeProcess() {
        final LogicalLocality writerLoc = new LogicalLocality("writer");
        KlavaProcess _Proc = new KlavaProcess() {
          LogicalLocality writerLoc;
          private KlavaProcess _initFields(LogicalLocality writerLoc) {
            this.writerLoc = writerLoc;
            return this;
          }
          @Override public void executeProcess() {
            {
              Locality _physical = this.getPhysical(writerLoc);
              String _plus = ("executing at " + _physical);
              InputOutput.<String>println(_plus);
              String s = null;
              Tuple _Tuple = new Tuple(new Object[] {String.class});
              in(_Tuple, this.self);
              s = (String) _Tuple.getItem(0);
              InputOutput.<String>println(s);
            }
          }
        }._initFields(writerLoc);
        eval(_Proc, writerLoc);
      }
    }
    
    public Reader() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("reader"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeProcess(new HelloRemoveEvalProcNet.Reader.ReaderProcess());
    }
  }
  
  public static class Writer extends ClientNode {
    private static class WriterProcess extends KlavaProcess {
      @Override
      public void executeProcess() {
        out(new Tuple(new Object[] {"Hello World"}), this.self);
      }
    }
    
    public Writer() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("writer"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeProcess(new HelloRemoveEvalProcNet.Writer.WriterProcess());
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
