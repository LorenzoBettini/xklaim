package xklaim.example.mobility.receiver;

import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import org.mikado.imc.common.IMCException;

/**
 * This is an example model
 */
@SuppressWarnings("all")
public class Receiver extends KlavaNode {
  private static class ReceiverProcess extends KlavaNodeCoordinator {
    @Override
    public void executeProcess() {
      while (true) {
        {
          final PhysicalLocality remote = new PhysicalLocality();
          this.accept(remote);
        }
      }
    }
  }
  
  public Receiver() {
    setMainPhysicalLocality(new PhysicalLocality("tcp-127.0.0.1:9999"));
  }
  
  public void addMainProcess() throws IMCException {
    addNodeCoordinator(new Receiver.ReceiverProcess());
  }
}
