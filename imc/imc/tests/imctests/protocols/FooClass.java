package imctests.protocols;

/**
 * <p>Title: Mikado Mobility package</p>
 * <p>Description: provide classes for moving code to another site</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Dip. Sistemi e Informatica, Univ. Firenze</p>
 * @author Lorenzo Bettini, bettini at dsi.unifi.it
 * @version 1.0
 */

public interface FooClass extends java.io.Serializable {
  void foo();
}

class FooClassImpl implements FooClass {
  private static final long serialVersionUID = 3618142268524149046L;

public FooClassImpl() {
  }

  public void foo() {
    System.out.println("FooClassImpl.foo");
  }
}

class FooMember implements java.io.Serializable {
  private static final long serialVersionUID = 3256727273180051766L;

public void foom() {
    System.out.println("FooMember.foom");
  }
}
