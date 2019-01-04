package xklaim.example.mobility.sender;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import klava.topology.KlavaProcess;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

/**
 * This is an example model
 */
@SuppressWarnings("all")
public class Sender extends KlavaNode {
  private static class SenderProcess extends KlavaNodeCoordinator {
    @Override
    public void executeProcess() {
      this.login(Sender.server);
      final PhysicalLocality myLoc = this.getPhysical(this.self);
      KlavaProcess _Proc = new KlavaProcess() {
        PhysicalLocality myLoc;
        private KlavaProcess _initFields(PhysicalLocality myLoc) {
          this.myLoc = myLoc;
          return this;
        }
        @Override public void executeProcess() {
          {
            InputOutput.<String>println(String.format("Hello %s...", Sender.server));
            InputOutput.<String>println(("...from a process coming from " + myLoc));
            out(new Tuple(new Object[] {"DONE"}), myLoc);
          }
        }
      }._initFields(myLoc);
      eval(_Proc, Sender.server);
      in(new Tuple(new Object[] {"DONE"}), this.self);
      InputOutput.<String>println("Remote process has done its job");
      this.logout(Sender.server);
      System.exit(0);
    }
  }
  
  private static final LogicalLocality server = new LogicalLocality("server");
  
  public void setupEnvironment() {
    PhysicalLocality _physicalLocality = new PhysicalLocality("tcp-127.0.0.1:9999");
    addToEnvironment(server, getPhysical(_physicalLocality));
  }
  
  public void addMainProcess() throws IMCException {
    addNodeCoordinator(new Sender.SenderProcess());
  }
}
