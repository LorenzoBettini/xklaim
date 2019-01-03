package xklaim.example.leaderelection;

import com.google.common.base.Objects;
import klava.LogicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;

@SuppressWarnings("all")
public class NotifierProc extends KlavaProcess {
  private Integer myId;
  
  public NotifierProc(final Integer myId) {
    super("xklaim.example.leaderelection.NotifierProc");
    this.myId = myId;
  }
  
  @Override
  public void executeProcess() {
    final LogicalLocality next = new LogicalLocality("next");
    Integer x = null;
    Tuple _Tuple = new Tuple(new Object[] {"ID", Integer.class});
    read(_Tuple, this.self);
    x = (Integer) _Tuple.getItem(1);
    boolean _equals = Objects.equal(x, this.myId);
    if (_equals) {
      out(new Tuple(new Object[] {"FOLLOWER"}), this.self);
    } else {
      NotifierProc _notifierProc = new NotifierProc(this.myId);
      eval(_notifierProc, next);
    }
  }
}
