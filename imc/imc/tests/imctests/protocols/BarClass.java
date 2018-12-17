package imctests.protocols;

/**
 * <p>Title: Mikado Mobility package</p>
 * <p>Description: provide classes for moving code to another site</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dip. Sistemi e Informatica, Univ. Firenze</p>
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public class BarClass implements java.io.Serializable {
  private static final long serialVersionUID = 4050481218451813681L;
  boolean do_exc = false;

  public BarClass() {
  }

  public String bar(BarPar p) throws BarException {
    if (do_exc)
      throw new BarException("BAR");

    return "BarClass.bar - " + p.bars();
  }
}

class BarPar implements java.io.Serializable {
  private static final long serialVersionUID = 3616449007732339769L;

  String bars() {
    return "BarParString";
  }
}

class BarException extends Exception {
  private static final long serialVersionUID = 3761409733134661427L;

  public BarException(String s) {
    super(s);
  }
}