package xklaim.example.leaderelection;

import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;
import org.eclipse.xtext.xbase.lib.InputOutput;
import xklaim.example.leaderelection.CheckerProc;
import xklaim.runtime.util.XklaimRuntimeUtil;

@SuppressWarnings("all")
public class InitialProc extends KlavaProcess {
  private String nodeName;
  
  public InitialProc(final String nodeName) {
    super("xklaim.example.leaderelection.InitialProc");
    this.nodeName = nodeName;
  }
  
  @Override
  public void executeProcess() {
    final PhysicalLocality rg = this.getPhysical(XklaimRuntimeUtil.logloc("rg"));
    final LogicalLocality next = XklaimRuntimeUtil.logloc("next");
    Integer xid = null;
    Tuple _Tuple = new Tuple(new Object[] {"ID", Integer.class});
    in(_Tuple, rg);
    xid = (Integer) _Tuple.getItem(1);
    out(new Tuple(new Object[] {"ID", xid}), this.self);
    CheckerProc _checkerProc = new CheckerProc(xid);
    eval(_checkerProc, next);
    String result = null;
    Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
    in(_Tuple_1, this.self);
    result = (String) _Tuple_1.getItem(0);
    InputOutput.<String>println(((this.nodeName + ": result is ") + result));
  }
}
