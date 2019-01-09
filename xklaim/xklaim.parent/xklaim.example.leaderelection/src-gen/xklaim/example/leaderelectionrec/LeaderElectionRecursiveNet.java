package xklaim.example.leaderelectionrec;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.LogicalNet;
import org.mikado.imc.common.IMCException;
import xklaim.example.leaderelectionrec.InitialProc;

/**
 * Leader election example in Xklaim.
 * 
 * This variant demonstrates recursive (possibly anonymous) processes
 * in Xklaim.
 * 
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class LeaderElectionRecursiveNet extends LogicalNet {
  private static final LogicalLocality L1 = new LogicalLocality("L1");
  
  private static final LogicalLocality L2 = new LogicalLocality("L2");
  
  private static final LogicalLocality L3 = new LogicalLocality("L3");
  
  private static final LogicalLocality rg = new LogicalLocality("rg");
  
  public static class L1 extends ClientNode {
    private static class L1Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        InitialProc _initialProc = new InitialProc("L1");
        eval(_initialProc, this.self);
      }
    }
    
    private static final LogicalLocality next = new LogicalLocality("next");
    
    public L1() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("L1"));
    }
    
    public void setupEnvironment() {
      addToEnvironment(next, getPhysical(LeaderElectionRecursiveNet.L2));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionRecursiveNet.L1.L1Process());
    }
  }
  
  public static class L2 extends ClientNode {
    private static class L2Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        InitialProc _initialProc = new InitialProc("L2");
        eval(_initialProc, this.self);
      }
    }
    
    private static final LogicalLocality next = new LogicalLocality("next");
    
    public L2() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("L2"));
    }
    
    public void setupEnvironment() {
      addToEnvironment(next, getPhysical(LeaderElectionRecursiveNet.L3));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionRecursiveNet.L2.L2Process());
    }
  }
  
  public static class L3 extends ClientNode {
    private static class L3Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        InitialProc _initialProc = new InitialProc("L3");
        eval(_initialProc, this.self);
      }
    }
    
    private static final LogicalLocality next = new LogicalLocality("next");
    
    public L3() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("L3"));
    }
    
    public void setupEnvironment() {
      addToEnvironment(next, getPhysical(LeaderElectionRecursiveNet.L1));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionRecursiveNet.L3.L3Process());
    }
  }
  
  public static class RG extends ClientNode {
    private static class RGProcess extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        out(new Tuple(new Object[] {"ID", 0}), this.self);
        out(new Tuple(new Object[] {"ID", 1}), this.self);
        out(new Tuple(new Object[] {"ID", 2}), this.self);
      }
    }
    
    public RG() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("rg"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionRecursiveNet.RG.RGProcess());
    }
  }
  
  public LeaderElectionRecursiveNet() throws IMCException {
    super(new PhysicalLocality("localhost:9999"));
  }
  
  public void addNodes() throws IMCException {
    LeaderElectionRecursiveNet.L1 l1 = new LeaderElectionRecursiveNet.L1();
    LeaderElectionRecursiveNet.L2 l2 = new LeaderElectionRecursiveNet.L2();
    LeaderElectionRecursiveNet.L3 l3 = new LeaderElectionRecursiveNet.L3();
    LeaderElectionRecursiveNet.RG rG = new LeaderElectionRecursiveNet.RG();
    l1.setupEnvironment();
    l2.setupEnvironment();
    l3.setupEnvironment();
    l1.addMainProcess();
    l2.addMainProcess();
    l3.addMainProcess();
    rG.addMainProcess();
  }
}
