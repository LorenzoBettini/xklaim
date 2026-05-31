package xklaim.examples.hellolocalities;

import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.LogicalNet;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

/**
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class HelloLocalitiesNet extends LogicalNet {
  private static final LogicalLocality reader = new LogicalLocality("reader");

  private static final LogicalLocality writer = new LogicalLocality("writer");

  public static class Reader extends ClientNode {
    private static class ReaderProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        Locality loc1 = null;
        Locality loc2 = null;
        Locality loc3 = null;
        Tuple _Tuple = new Tuple(new Object[] {Locality.class, Locality.class, Locality.class});
        in(_Tuple, this.self);
        loc1 = (Locality) _Tuple.getItem(0);
        loc2 = (Locality) _Tuple.getItem(1);
        loc3 = (Locality) _Tuple.getItem(2);
        InputOutput.<String>println(((((("Read localities: " + loc1) + ", ") + loc2) + ", ") + loc3));
        this.done();
      }
    }

    private static final LogicalLocality writerLoc = new LogicalLocality("writerLoc");

    public Reader() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("reader"));
    }

    public void setupEnvironment() {
      addToEnvironment(writerLoc, getPhysical(HelloLocalitiesNet.writer));
    }

    public void addMainProcess() throws IMCException {
      KlavaNodeCoordinator _coordinator = new HelloLocalitiesNet.Reader.ReaderProcess();
      setMainCoordinator(_coordinator);
      addNodeCoordinator(_coordinator);
    }
  }

  public static class Writer extends ClientNode {
    private static class WriterProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        out(new Tuple(new Object[] {this.self, HelloLocalitiesNet.reader, HelloLocalitiesNet.writer}), HelloLocalitiesNet.reader);
      }
    }

    public Writer() {
      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("writer"));
    }

    public void addMainProcess() throws IMCException {
      KlavaNodeCoordinator _coordinator = new HelloLocalitiesNet.Writer.WriterProcess();
      setMainCoordinator(_coordinator);
      addNodeCoordinator(_coordinator);
    }
  }

  public HelloLocalitiesNet() throws IMCException {
    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
  }

  public void addNodes() throws IMCException {
    HelloLocalitiesNet.Reader reader = new HelloLocalitiesNet.Reader();
    HelloLocalitiesNet.Writer writer = new HelloLocalitiesNet.Writer();
    reader.setupEnvironment();
    addManagedNode(reader);
    addManagedNode(writer);
    reader.addMainProcess();
    writer.addMainProcess();
  }
}
