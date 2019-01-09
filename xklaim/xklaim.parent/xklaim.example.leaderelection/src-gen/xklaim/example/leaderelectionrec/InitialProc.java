package xklaim.example.leaderelectionrec;

import com.google.common.base.Objects;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;
import org.eclipse.xtext.xbase.lib.InputOutput;
import xklaim.runtime.util.XklaimRuntimeUtil;

@SuppressWarnings("all")
public class InitialProc extends KlavaProcess {
  private String nodeName;
  
  public InitialProc(final String nodeName) {
    super("xklaim.example.leaderelectionrec.InitialProc");
    this.nodeName = nodeName;
  }
  
  @Override
  public void executeProcess() {
    final PhysicalLocality rg = this.getPhysical(XklaimRuntimeUtil.logloc("rg"));
    final LogicalLocality next = XklaimRuntimeUtil.logloc("next");
    Integer myId = null;
    Tuple _Tuple = new Tuple(new Object[] {"ID", Integer.class});
    in(_Tuple, rg);
    myId = (Integer) _Tuple.getItem(1);
    out(new Tuple(new Object[] {"ID", myId}), this.self);
    KlavaProcess _Proc = new KlavaProcess() {
      Integer myId;
      LogicalLocality next;
      private KlavaProcess _initFields(Integer myId, LogicalLocality next) {
        this.myId = myId;
        this.next = next;
        return this;
      }
      @Override public void executeProcess() {
        {
          Integer x = null;
          Tuple _Tuple_1 = new Tuple(new Object[] {"ID", Integer.class});
          read(_Tuple_1, this.self);
          x = (Integer) _Tuple_1.getItem(1);
          boolean _lessThan = (myId.compareTo(x) < 0);
          if (_lessThan) {
            eval(this, next);
          } else {
            boolean _greaterThan = (myId.compareTo(x) > 0);
            if (_greaterThan) {
              KlavaProcess _Proc_1 = new KlavaProcess() {
                Integer myId;
                LogicalLocality next;
                private KlavaProcess _initFields(Integer myId, LogicalLocality next) {
                  this.myId = myId;
                  this.next = next;
                  return this;
                }
                @Override public void executeProcess() {
                  {
                    Integer x1 = null;
                    Tuple _Tuple_2 = new Tuple(new Object[] {"ID", Integer.class});
                    read(_Tuple_2, this.self);
                    x1 = (Integer) _Tuple_2.getItem(1);
                    boolean _equals = Objects.equal(x1, myId);
                    if (_equals) {
                      out(new Tuple(new Object[] {"FOLLOWER"}), this.self);
                    } else {
                      eval(this, next);
                    }
                  }
                }
              }._initFields(myId, next);
              eval(_Proc_1, next);
            } else {
              out(new Tuple(new Object[] {"LEADER"}), this.self);
            }
          }
        }
      }
    }._initFields(myId, next);
    eval(_Proc, next);
    String result = null;
    Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
    in(_Tuple_1, this.self);
    result = (String) _Tuple_1.getItem(0);
    InputOutput.<String>println(((this.nodeName + ": result is ") + result));
  }
}
