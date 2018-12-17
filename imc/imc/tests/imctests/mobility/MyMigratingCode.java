package imctests.mobility;

import org.mikado.imc.mobility.*;

/**
 * <p>Title: Mikado Mobility package</p>
 * <p>Description: provide classes for moving code to another site</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dip. Sistemi e Informatica, Univ. Firenze</p>
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public class MyMigratingCode extends JavaMigratingCode {
  private static final long serialVersionUID = 3689909591542411318L;

  public MyMigratingCode() {
  }

  public Object bar() {
    return null;
  }

  public String toString() {
    return bar().toString();
  }
}

class MyMigratingCodeImpl extends MyMigratingCode {
  private static final long serialVersionUID = 3256722888018113591L;
  FooClassImpl foo_obj = new FooClassImpl();

  public MyMigratingCodeImpl() {
  }

  public Object bar() {
    try {
      return newbar().bar(new BarPar());
    } catch (BarException b) {
      b.printStackTrace();
    }
    return null;
  }

  public BarClass newbar() throws BarException {
    return new BarClass();
  }
}
