package xklaim.example.mobility.receiver;

import klava.PhysicalLocality;
import klava.topology.KlavaNode;
import klava.topology.KlavaNodeCoordinator;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.mikado.imc.common.IMCException;

/**
 * This contains the Net where the senders of mobile code have to
 * login before evaluating their mobile code in this net.
 * 
 * Right click on the file and select "Run As" -> "Xklaim Application".
 */
@SuppressWarnings("all")
public class Receiver extends KlavaNode {
  private static class ReceiverProcess extends KlavaNodeCoordinator {
    @Override
    public void executeProcess() {
      InputOutput.<String>println("Receiver started");
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
