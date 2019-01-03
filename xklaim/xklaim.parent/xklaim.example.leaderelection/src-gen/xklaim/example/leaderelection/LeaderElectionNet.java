package xklaim.example.leaderelection;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.LogicalNet;
import org.mikado.imc.common.IMCException;
import xklaim.example.leaderelection.InitialProc;

@SuppressWarnings("all")
public class LeaderElectionNet extends LogicalNet {
  public static class L1 extends ClientNode {
    private static class L1Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        LogicalLocality _logicalLocality = new LogicalLocality("next");
        LogicalLocality _logicalLocality_1 = new LogicalLocality("l2");
        this.addToEnvironment(_logicalLocality, this.getPhysical(_logicalLocality_1));
        InitialProc _initialProc = new InitialProc("l1");
        eval(_initialProc, this.self);
      }
    }
    
    public L1() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("l1"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionNet.L1.L1Process());
    }
  }
  
  public static class L2 extends ClientNode {
    private static class L2Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        LogicalLocality _logicalLocality = new LogicalLocality("next");
        LogicalLocality _logicalLocality_1 = new LogicalLocality("l3");
        this.addToEnvironment(_logicalLocality, this.getPhysical(_logicalLocality_1));
        InitialProc _initialProc = new InitialProc("l2");
        eval(_initialProc, this.self);
      }
    }
    
    public L2() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("l2"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionNet.L2.L2Process());
    }
  }
  
  public static class L3 extends ClientNode {
    private static class L3Process extends KlavaNodeCoordinator {
      @Override
      public void executeProcess() {
        LogicalLocality _logicalLocality = new LogicalLocality("next");
        LogicalLocality _logicalLocality_1 = new LogicalLocality("l1");
        this.addToEnvironment(_logicalLocality, this.getPhysical(_logicalLocality_1));
        InitialProc _initialProc = new InitialProc("l3");
        eval(_initialProc, this.self);
      }
    }
    
    public L3() {
      super(new PhysicalLocality("localhost:9999"), new LogicalLocality("l3"));
    }
    
    public void addMainProcess() throws IMCException {
      addNodeCoordinator(new LeaderElectionNet.L3.L3Process());
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
      addNodeCoordinator(new LeaderElectionNet.RG.RGProcess());
    }
  }
  
  public LeaderElectionNet() throws IMCException {
    super(new PhysicalLocality("localhost:9999"));
  }
  
  public void addNodes() throws IMCException {
    LeaderElectionNet.L1 l1 = new LeaderElectionNet.L1();
    LeaderElectionNet.L2 l2 = new LeaderElectionNet.L2();
    LeaderElectionNet.L3 l3 = new LeaderElectionNet.L3();
    LeaderElectionNet.RG rG = new LeaderElectionNet.RG();
    l1.addMainProcess();
    l2.addMainProcess();
    l3.addMainProcess();
    rG.addMainProcess();
  }
}
