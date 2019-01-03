package xklaim.example.mobility.sender;

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
      final PhysicalLocality server = new PhysicalLocality("tcp-127.0.0.1:9999");
      this.login(server);
      final PhysicalLocality myLoc = this.getPhysical(this.self);
      KlavaProcess _Proc = new KlavaProcess() {
        PhysicalLocality myLoc;
        private KlavaProcess _initFields(PhysicalLocality myLoc) {
          this.myLoc = myLoc;
          return this;
        }
        @Override public void executeProcess() {
          {
            InputOutput.<String>println("Hello...");
            InputOutput.<String>println(("...from a process coming from " + myLoc));
            out(new Tuple(new Object[] {"DONE"}), myLoc);
          }
        }
      }._initFields(myLoc);
      eval(_Proc, server);
      in(new Tuple(new Object[] {"DONE"}), this.self);
      InputOutput.<String>println("Remote process has done its job");
      this.logout(server);
      System.exit(0);
    }
  }
  
  public void addMainProcess() throws IMCException {
    addNodeCoordinator(new Sender.SenderProcess());
  }
}
