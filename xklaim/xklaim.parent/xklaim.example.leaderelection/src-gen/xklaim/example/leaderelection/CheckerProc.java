package xklaim.example.leaderelection;

import klava.LogicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;
import xklaim.runtime.util.XklaimRuntimeUtil;

@SuppressWarnings("all")
public class CheckerProc extends KlavaProcess {
  private Integer myId;
  
  public CheckerProc(final Integer myId) {
    this.myId = myId;
  }
  
  @Override
  public void executeProcess() {
    final LogicalLocality next = XklaimRuntimeUtil.logloc("next");
    Integer x = null;
    Tuple _Tuple = new Tuple(new Object[] {"ID", Integer.class});
    read(_Tuple, this.self);
    x = (Integer) _Tuple.getItem(1);
    boolean _lessThan = (this.myId.compareTo(x) < 0);
    if (_lessThan) {
      CheckerProc _checkerProc = new CheckerProc(this.myId);
      eval(_checkerProc, next);
    } else {
      boolean _greaterThan = (this.myId.compareTo(x) > 0);
      if (_greaterThan) {
        NotifierProc _notifierProc = new NotifierProc(this.myId);
        eval(_notifierProc, next);
      } else {
        out(new Tuple(new Object[] {"LEADER"}), this.self);
      }
    }
  }
}
