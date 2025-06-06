/*
 * generated by Xtext 2.16.0
 */
package xklaim.tests

import com.google.common.base.Joiner
import com.google.inject.Inject
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.TemporaryFolder
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.xbase.testing.CompilationTestHelper
import org.eclipse.xtext.xbase.testing.CompilationTestHelper.Result
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(XklaimInjectorProvider)
class XklaimCompilerTest {
	@Rule @Inject public TemporaryFolder temporaryFolder
	@Inject extension CompilationTestHelper

	@Test
	def void testProgramWithNodes() {
		'''
		package foo
		node TestNode [other -> phyloc("localhost:9999")] {
			println("Hello")
		}
		node TestNodeWithPhysicalLocality physical "localhost:9999" {
			println("Hello")
		}
		'''.checkCompilation(
			"foo.TestNode" ->
			'''
			package foo;
			
			import klava.LogicalLocality;
			import klava.PhysicalLocality;
			import klava.topology.KlavaNode;
			import klava.topology.KlavaNodeCoordinator;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			import xklaim.runtime.util.XklaimRuntimeUtil;
			
			@SuppressWarnings("all")
			public class TestNode extends KlavaNode {
			  private static class TestNodeProcess extends KlavaNodeCoordinator {
			    @Override
			    public void executeProcess() {
			      InputOutput.<String>println("Hello");
			    }
			  }
			
			  private static final LogicalLocality other = new LogicalLocality("other");
			
			  public void setupEnvironment() {
			    PhysicalLocality _phyloc = XklaimRuntimeUtil.phyloc("localhost:9999");
			    addToEnvironment(other, getPhysical(_phyloc));
			  }
			
			  public void addMainProcess() throws IMCException {
			    addNodeCoordinator(new TestNode.TestNodeProcess());
			  }
			}
			''',
			"foo.TestNodeWithPhysicalLocality" ->
			'''
			package foo;
			
			import klava.PhysicalLocality;
			import klava.topology.KlavaNode;
			import klava.topology.KlavaNodeCoordinator;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNodeWithPhysicalLocality extends KlavaNode {
			  private static class TestNodeWithPhysicalLocalityProcess extends KlavaNodeCoordinator {
			    @Override
			    public void executeProcess() {
			      InputOutput.<String>println("Hello");
			    }
			  }
			
			  public TestNodeWithPhysicalLocality() {
			    setMainPhysicalLocality(new PhysicalLocality("localhost:9999"));
			  }
			
			  public void addMainProcess() throws IMCException {
			    addNodeCoordinator(new TestNodeWithPhysicalLocality.TestNodeWithPhysicalLocalityProcess());
			  }
			}
			''',
			"foo.MyFile" ->
			'''
			package foo;
			
			@SuppressWarnings("all")
			public class MyFile {
			  public static void main(final String[] args) throws Exception {
			    TestNode testNode = new TestNode();
			    TestNodeWithPhysicalLocality testNodeWithPhysicalLocality = new TestNodeWithPhysicalLocality();
			    testNode.setupEnvironment();
			    testNode.addMainProcess();
			    testNodeWithPhysicalLocality.addMainProcess();
			  }
			}
			'''
		)
	}

	@Test
	def void testProgramWithNet() {
		'''
		package foo
		net TestNet physical "tcp-127.0.0.1:9999" {
			node TestNode {
				println("Hello from " + TestNode)
			}
			node TestNodeWithLogLoc logical "foo" {
				println("Hello from " + foo)
			}
			node TestNodeWithEmptyEnvironment [] {
				
			}
			node TestNodeWithEnvironment [l1 -> TestNode, l2 -> foo] {
				
			}
		}
		'''.checkCompilation(
			"foo.TestNet" ->
			'''
			package foo;
			
			import klava.LogicalLocality;
			import klava.PhysicalLocality;
			import klava.topology.ClientNode;
			import klava.topology.KlavaNodeCoordinator;
			import klava.topology.LogicalNet;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNet extends LogicalNet {
			  private static final LogicalLocality TestNode = new LogicalLocality("TestNode");
			
			  private static final LogicalLocality foo = new LogicalLocality("foo");
			
			  private static final LogicalLocality TestNodeWithEmptyEnvironment = new LogicalLocality("TestNodeWithEmptyEnvironment");
			
			  private static final LogicalLocality TestNodeWithEnvironment = new LogicalLocality("TestNodeWithEnvironment");
			
			  public static class TestNode extends ClientNode {
			    private static class TestNodeProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			        InputOutput.<String>println(("Hello from " + TestNet.TestNode));
			      }
			    }
			
			    public TestNode() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("TestNode"));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNode.TestNodeProcess());
			    }
			  }
			
			  public static class TestNodeWithLogLoc extends ClientNode {
			    private static class TestNodeWithLogLocProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			        InputOutput.<String>println(("Hello from " + TestNet.foo));
			      }
			    }
			
			    public TestNodeWithLogLoc() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("foo"));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNodeWithLogLoc.TestNodeWithLogLocProcess());
			    }
			  }
			
			  public static class TestNodeWithEmptyEnvironment extends ClientNode {
			    private static class TestNodeWithEmptyEnvironmentProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			      }
			    }
			
			    public TestNodeWithEmptyEnvironment() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("TestNodeWithEmptyEnvironment"));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNodeWithEmptyEnvironment.TestNodeWithEmptyEnvironmentProcess());
			    }
			  }
			
			  public static class TestNodeWithEnvironment extends ClientNode {
			    private static class TestNodeWithEnvironmentProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			      }
			    }
			
			    private static final LogicalLocality l1 = new LogicalLocality("l1");
			
			    private static final LogicalLocality l2 = new LogicalLocality("l2");
			
			    public TestNodeWithEnvironment() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("TestNodeWithEnvironment"));
			    }
			
			    public void setupEnvironment() {
			      addToEnvironment(l1, getPhysical(TestNet.TestNode));
			      addToEnvironment(l2, getPhysical(TestNet.foo));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNodeWithEnvironment.TestNodeWithEnvironmentProcess());
			    }
			  }
			
			  public TestNet() throws IMCException {
			    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
			  }
			
			  public void addNodes() throws IMCException {
			    TestNet.TestNode testNode = new TestNet.TestNode();
			    TestNet.TestNodeWithLogLoc testNodeWithLogLoc = new TestNet.TestNodeWithLogLoc();
			    TestNet.TestNodeWithEmptyEnvironment testNodeWithEmptyEnvironment = new TestNet.TestNodeWithEmptyEnvironment();
			    TestNet.TestNodeWithEnvironment testNodeWithEnvironment = new TestNet.TestNodeWithEnvironment();
			    testNodeWithEnvironment.setupEnvironment();
			    testNode.addMainProcess();
			    testNodeWithLogLoc.addMainProcess();
			    testNodeWithEmptyEnvironment.addMainProcess();
			    testNodeWithEnvironment.addMainProcess();
			  }
			}
			''',
			"foo.MyFile" ->
			'''
			package foo;
			
			@SuppressWarnings("all")
			public class MyFile {
			  public static void main(final String[] args) throws Exception {
			    TestNet testNet = new TestNet();
			    testNet.addNodes();
			  }
			}
			'''
		)
	}

	@Test
	def void testProgramWithProcess() {
		'''
		package foo
		proc TestProcess(String s) {
			println(s)
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    InputOutput.<String>println(this.s);
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimOperations() {
		'''
		package foo
		proc TestProcess(String s) {
			out(s, s)@self
			in(s, var Integer i, val Boolean b)@self
			println(i)
			println(b)
			read(s, var Integer i2, val KlavaProcess b2)@self
			println(i2)
			println(b2)
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    out(new Tuple(new Object[] {this.s, this.s}), this.self);
			    Integer i = null;
			    final Boolean b;
			    Tuple _Tuple = new Tuple(new Object[] {this.s, Integer.class, Boolean.class});
			    in(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(1);
			    b = (Boolean) _Tuple.getItem(2);
			    InputOutput.<Integer>println(i);
			    InputOutput.<Boolean>println(b);
			    Integer i2 = null;
			    final KlavaProcess b2;
			    Tuple _Tuple_1 = new Tuple(new Object[] {this.s, Integer.class, KlavaProcess.class});
			    read(_Tuple_1, this.self);
			    i2 = (Integer) _Tuple_1.getItem(1);
			    b2 = (KlavaProcess) _Tuple_1.getItem(2);
			    InputOutput.<Integer>println(i2);
			    InputOutput.<KlavaProcess>println(b2);
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimNonBlockingOperationsInIfStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(var Integer i, s)@self) {
				println(i)
			} else {
				val res = i
			}
			if (read_nb(var Integer i, s)@self) {
				println(i)
			} else {
				val res = i
			}
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    Integer i = null;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in_nb = in_nb(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(0);
			    if (_in_nb) {
			      InputOutput.<Integer>println(i);
			    } else {
			      final Integer res = i;
			    }
			    Integer i_1 = null;
			    Tuple _Tuple_1 = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _read_nb = read_nb(_Tuple_1, this.self);
			    i_1 = (Integer) _Tuple_1.getItem(0);
			    if (_read_nb) {
			      InputOutput.<Integer>println(i_1);
			    } else {
			      final Integer res_1 = i_1;
			    }
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimTimeoutOperationsInIfStatement() {
		'''
		package foo
		proc TestProcess(String s, int time) {
			val timeout = 2000 + time
			if ((in(val Integer i, s)@self within 1000) && (!in(var String l)@self within 1000)) {
				println(i)
			} else {
				val res = i
			}
			if (read(var Integer i, s)@self within timeout) {
				println(i)
			} else {
				val res = i
			}
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  private int time;
			
			  public TestProcess(final String s, final int time) {
			    this.s = s;
			    this.time = time;
			  }
			
			  @Override
			  public void executeProcess() {
			    final int timeout = (2000 + this.time);
			    final Integer i;
			    String l = null;
			    boolean _and = false;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in = in_t(_Tuple, this.self, 1000);
			    i = (Integer) _Tuple.getItem(0);
			    if (!_in) {
			      _and = false;
			    } else {
			      Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			      boolean _in_1 = in_t(_Tuple_1, this.self, 1000);
			      l = (String) _Tuple_1.getItem(0);
			      boolean _not = (!_in_1);
			      _and = _not;
			    }
			    if (_and) {
			      InputOutput.<Integer>println(i);
			    } else {
			      final Integer res = i;
			    }
			    Integer i_1 = null;
			    Tuple _Tuple_2 = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _read = read_t(_Tuple_2, this.self, timeout);
			    i_1 = (Integer) _Tuple_2.getItem(0);
			    if (_read) {
			      InputOutput.<Integer>println(i_1);
			    } else {
			      final Integer res_1 = i_1;
			    }
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimNonBlockingOperationsInComplexBooleanExpression() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(var Integer i, s)@self && !in_nb(var String l)@self) {
				println(l + i)
			} else {
				val res = (l + i)
			}
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    Integer i = null;
			    String l = null;
			    boolean _and = false;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in_nb = in_nb(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(0);
			    if (!_in_nb) {
			      _and = false;
			    } else {
			      Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			      boolean _in_nb_1 = in_nb(_Tuple_1, this.self);
			      l = (String) _Tuple_1.getItem(0);
			      boolean _not = (!_in_nb_1);
			      _and = _not;
			    }
			    if (_and) {
			      InputOutput.<String>println((l + i));
			    } else {
			      final String res = (l + i);
			    }
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimNonBlockingOperationsInWhileStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			while (in_nb(var Integer i, s)@self && !in_nb(var String l)@self) {
				println(l + i)
			}
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    Integer i = null;
			    String l = null;
			    boolean _and = false;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in_nb = in_nb(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(0);
			    if (!_in_nb) {
			      _and = false;
			    } else {
			      Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			      boolean _in_nb_1 = in_nb(_Tuple_1, this.self);
			      l = (String) _Tuple_1.getItem(0);
			      boolean _not = (!_in_nb_1);
			      _and = _not;
			    }
			    boolean _while = _and;
			    while (_while) {
			      InputOutput.<String>println((l + i));
			      boolean _and_1 = false;
			      Tuple _Tuple_2 = new Tuple(new Object[] {Integer.class, this.s});
			      boolean _in_nb_2 = in_nb(_Tuple_2, this.self);
			      i = (Integer) _Tuple_2.getItem(0);
			      if (!_in_nb_2) {
			        _and_1 = false;
			      } else {
			        Tuple _Tuple_3 = new Tuple(new Object[] {String.class});
			        boolean _in_nb_3 = in_nb(_Tuple_3, this.self);
			        l = (String) _Tuple_3.getItem(0);
			        boolean _not_1 = (!_in_nb_3);
			        _and_1 = _not_1;
			      }
			      _while = _and_1;
			    }
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimNonBlockingOperationsInForStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			for (;in_nb(var Integer i, s)@self && !in_nb(var String l)@self;) {
				println(l + i)
			}
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    Integer i = null;
			    String l = null;
			    boolean _and = false;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in_nb = in_nb(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(0);
			    if (!_in_nb) {
			      _and = false;
			    } else {
			      Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			      boolean _in_nb_1 = in_nb(_Tuple_1, this.self);
			      l = (String) _Tuple_1.getItem(0);
			      boolean _not = (!_in_nb_1);
			      _and = _not;
			    }
			    boolean _while = _and;
			    while (_while) {
			      InputOutput.<String>println((l + i));
			      boolean _and_1 = false;
			      Tuple _Tuple_2 = new Tuple(new Object[] {Integer.class, this.s});
			      boolean _in_nb_2 = in_nb(_Tuple_2, this.self);
			      i = (Integer) _Tuple_2.getItem(0);
			      if (!_in_nb_2) {
			        _and_1 = false;
			      } else {
			        Tuple _Tuple_3 = new Tuple(new Object[] {String.class});
			        boolean _in_nb_3 = in_nb(_Tuple_3, this.self);
			        l = (String) _Tuple_3.getItem(0);
			        boolean _not_1 = (!_in_nb_3);
			        _and_1 = _not_1;
			      }
			      _while = _and_1;
			    }
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimNonBlockingOperationsInDoWhileStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			do {
				println()
			} while (in_nb(var Integer i, s)@self && !in_nb(val String l)@self)
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    boolean _dowhile = false;
			    do {
			      InputOutput.println();
			      boolean _and = false;
			      Integer i = null;
			      Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			      boolean _in_nb = in_nb(_Tuple, this.self);
			      i = (Integer) _Tuple.getItem(0);
			      if (!_in_nb) {
			        _and = false;
			      } else {
			        final String l;
			        Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			        boolean _in_nb_1 = in_nb(_Tuple_1, this.self);
			        l = (String) _Tuple_1.getItem(0);
			        boolean _not = (!_in_nb_1);
			        _and = _not;
			      }
			      _dowhile = _and;
			    } while(_dowhile);
			  }
			}
			'''
		)
	}


	@Test
	def void testDuplicateFormalFieldsInXklaimOperationAfterIf() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(var Integer i, s)@self) {
				
			}
			in(var String i)@self
			println(i.length())
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    Integer i = null;
			    Tuple _Tuple = new Tuple(new Object[] {Integer.class, this.s});
			    boolean _in_nb = in_nb(_Tuple, this.self);
			    i = (Integer) _Tuple.getItem(0);
			    if (_in_nb) {
			    }
			    String i_1 = null;
			    Tuple _Tuple_1 = new Tuple(new Object[] {String.class});
			    in(_Tuple_1, this.self);
			    i_1 = (String) _Tuple_1.getItem(0);
			    InputOutput.<Integer>println(Integer.valueOf(i_1.length()));
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimOperationsWithProcessCall() {
		'''
		package foo
		proc AnotherProcess(int i) {
			
		}
		proc TestProcess(String s) {
			out(new AnotherProcess(10))@self
		}
		'''.checkCompilation(
			"foo.TestProcess" ->
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    AnotherProcess _anotherProcess = new AnotherProcess(10);
			    out(new Tuple(new Object[] {_anotherProcess}), this.self);
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimOperationsWithInlineProcess() {
		'''
		package foo
		
		proc TestProcess(String s) {
			val i = 10
			out(proc { println(s + i + self) }, s+i)@self
			out(proc println(""), s+i)@self
		}
		
		net TestNet physical "tcp-127.0.0.1:9999" {
			node TestNode logical "foo" {
				val i = 10
				out(proc {
					println(i + "" + self)
					println(foo)
				},
				i)@self
			}
		}
		'''.checkCompilation(
			"foo.TestProcess" ->
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    final int i = 10;
			    KlavaProcess _Proc = new KlavaProcess() {
			      String s;
			      int i;
			      private KlavaProcess _initFields(String s, int i) {
			        this.s = s;
			        this.i = i;
			        return this;
			      }
			      @Override public void executeProcess() {
			        InputOutput.<String>println(((this.s + Integer.valueOf(i)) + this.self));
			      }
			    }._initFields(s, i);
			    out(new Tuple(new Object[] {_Proc, (this.s + Integer.valueOf(i))}), this.self);
			    KlavaProcess _Proc_1 = new KlavaProcess() {
			      private KlavaProcess _initFields() {
			        return this;
			      }
			      @Override public void executeProcess() {
			        InputOutput.<String>println("");
			      }
			    }._initFields();
			    out(new Tuple(new Object[] {_Proc_1, (this.s + Integer.valueOf(i))}), this.self);
			  }
			}
			''',
			"foo.TestNet" ->
			'''
			package foo;
			
			import klava.LogicalLocality;
			import klava.PhysicalLocality;
			import klava.Tuple;
			import klava.topology.ClientNode;
			import klava.topology.KlavaNodeCoordinator;
			import klava.topology.KlavaProcess;
			import klava.topology.LogicalNet;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNet extends LogicalNet {
			  private static final LogicalLocality foo = new LogicalLocality("foo");
			
			  public static class TestNode extends ClientNode {
			    private static class TestNodeProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			        final int i = 10;
			        KlavaProcess _Proc = new KlavaProcess() {
			          int i;
			          private KlavaProcess _initFields(int i) {
			            this.i = i;
			            return this;
			          }
			          @Override public void executeProcess() {
			            {
			              String _plus = (Integer.valueOf(i) + "");
			              String _plus_1 = (_plus + this.self);
			              InputOutput.<String>println(_plus_1);
			              InputOutput.<LogicalLocality>println(TestNet.foo);
			            }
			          }
			        }._initFields(i);
			        out(new Tuple(new Object[] {_Proc, i}), this.self);
			      }
			    }
			
			    public TestNode() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("foo"));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNode.TestNodeProcess());
			    }
			  }
			
			  public TestNet() throws IMCException {
			    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
			  }
			
			  public void addNodes() throws IMCException {
			    TestNet.TestNode testNode = new TestNet.TestNode();
			    testNode.addMainProcess();
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimInlineProcess() {
		'''
		package foo
		
		proc TestProcess(String s) {
			val i = 10
			val P = proc { println(s + i + self) }
			out(P)@self
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    final int i = 10;
			    KlavaProcess _Proc = new KlavaProcess() {
			      String s;
			      int i;
			      private KlavaProcess _initFields(String s, int i) {
			        this.s = s;
			        this.i = i;
			        return this;
			      }
			      @Override public void executeProcess() {
			        InputOutput.<String>println(((this.s + Integer.valueOf(i)) + this.self));
			      }
			    }._initFields(s, i);
			    final KlavaProcess P = _Proc;
			    out(new Tuple(new Object[] {P}), this.self);
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimOperationsWithInlineProcessAccessingEnclosingScope() {
		'''
		package foo
		
		proc TestProcess(String s) {
			var nonFinalVar = "a"
			val finalVar = "b"
			out(proc {
				var myLocalVar = "c"
				println(finalVar + nonFinalVar)
				println(s + finalVar + nonFinalVar + myLocalVar + self)
			}, s + finalVar + nonFinalVar)@self
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    String nonFinalVar = "a";
			    final String finalVar = "b";
			    KlavaProcess _Proc = new KlavaProcess() {
			      String finalVar;
			      String nonFinalVar;
			      String s;
			      private KlavaProcess _initFields(String finalVar, String nonFinalVar, String s) {
			        this.finalVar = finalVar;
			        this.nonFinalVar = nonFinalVar;
			        this.s = s;
			        return this;
			      }
			      @Override public void executeProcess() {
			        {
			          String myLocalVar = "c";
			          InputOutput.<String>println((finalVar + nonFinalVar));
			          InputOutput.<String>println(((((this.s + finalVar) + nonFinalVar) + myLocalVar) + this.self));
			        }
			      }
			    }._initFields(finalVar, nonFinalVar, s);
			    out(new Tuple(new Object[] {_Proc, ((this.s + finalVar) + nonFinalVar)}), this.self);
			  }
			}
			'''
		)
	}

	@Test
	def void testXklaimEval() {
		'''
		package foo
		
		proc P(String s) {
			
		}
		
		proc TestProcess(String s) {
			val i = 10
			// with explicit inner proc
			eval(proc { println(s + i + self) }, new P("test"))@self
			// without explicit inner proc
			var P = proc { }
			eval(println(s + i + self), "test", P)@self
		}
		
		net TestNet physical "tcp-127.0.0.1:9999" {
			node TestNode logical "foo" {
				val i = 10
				eval(proc { println(i + "" + self) })@self
			}
		}
		'''.checkCompilation(
			"foo.TestProcess" ->
			'''
			package foo;
			
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			
			  public TestProcess(final String s) {
			    this.s = s;
			  }
			
			  @Override
			  public void executeProcess() {
			    final int i = 10;
			    KlavaProcess _Proc = new KlavaProcess() {
			      String s;
			      int i;
			      private KlavaProcess _initFields(String s, int i) {
			        this.s = s;
			        this.i = i;
			        return this;
			      }
			      @Override public void executeProcess() {
			        InputOutput.<String>println(((this.s + Integer.valueOf(i)) + this.self));
			      }
			    }._initFields(s, i);
			    P _p = new P("test");
			    eval(_Proc, this.self);
			    eval(_p, this.self);
			    KlavaProcess _Proc_1 = new KlavaProcess() {
			      private KlavaProcess _initFields() {
			        return this;
			      }
			      @Override public void executeProcess() {
			      }
			    }._initFields();
			    KlavaProcess P = _Proc_1;
			    KlavaProcess _Proc_2 = new KlavaProcess() {
			      String s;
			      int i;
			      private KlavaProcess _initFields(String s, int i) {
			        this.s = s;
			        this.i = i;
			        return this;
			      }
			      @Override public void executeProcess() {
			        InputOutput.<String>println(((this.s + Integer.valueOf(i)) + this.self));
			      }
			    }._initFields(s, i);
			    KlavaProcess _Proc_3 = new KlavaProcess() {
			      private KlavaProcess _initFields() {
			        return this;
			      }
			      @Override public void executeProcess() {
			        /* "test" */
			      }
			    }._initFields();
			    eval(_Proc_2, this.self);
			    eval(_Proc_3, this.self);
			    eval(P, this.self);
			  }
			}
			''',
			"foo.TestNet" ->
			'''
			package foo;
			
			import klava.LogicalLocality;
			import klava.PhysicalLocality;
			import klava.topology.ClientNode;
			import klava.topology.KlavaNodeCoordinator;
			import klava.topology.KlavaProcess;
			import klava.topology.LogicalNet;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNet extends LogicalNet {
			  private static final LogicalLocality foo = new LogicalLocality("foo");
			
			  public static class TestNode extends ClientNode {
			    private static class TestNodeProcess extends KlavaNodeCoordinator {
			      @Override
			      public void executeProcess() {
			        final int i = 10;
			        KlavaProcess _Proc = new KlavaProcess() {
			          int i;
			          private KlavaProcess _initFields(int i) {
			            this.i = i;
			            return this;
			          }
			          @Override public void executeProcess() {
			            String _plus = (Integer.valueOf(i) + "");
			            String _plus_1 = (_plus + this.self);
			            InputOutput.<String>println(_plus_1);
			          }
			        }._initFields(i);
			        eval(_Proc, this.self);
			      }
			    }
			
			    public TestNode() {
			      super(new PhysicalLocality("tcp-127.0.0.1:9999"), new LogicalLocality("foo"));
			    }
			
			    public void addMainProcess() throws IMCException {
			      addNodeCoordinator(new TestNet.TestNode.TestNodeProcess());
			    }
			  }
			
			  public TestNet() throws IMCException {
			    super(new PhysicalLocality("tcp-127.0.0.1:9999"));
			  }
			
			  public void addNodes() throws IMCException {
			    TestNet.TestNode testNode = new TestNet.TestNode();
			    testNode.addMainProcess();
			  }
			}
			'''
		)
	}

	def private checkCompilation(CharSequence input, CharSequence expectedGeneratedJava) {
		checkCompilation(input, expectedGeneratedJava, true)
	}

	def private checkCompilation(CharSequence input, CharSequence expectedGeneratedJava, boolean checkValidationErrors) {
		input.compile[
			if (checkValidationErrors) {
				assertNoValidationErrors
			}
			if (expectedGeneratedJava !== null) {
				assertGeneratedJavaCode(expectedGeneratedJava)
			}
			if (checkValidationErrors) {
				assertGeneratedJavaCodeCompiles
			}
		]
	}

	def private checkCompilation(CharSequence input, Pair<CharSequence, CharSequence>... expectations) {
		input.compile[
			assertNoValidationErrors
			for (exp : expectations) {
				exp.value.toString.assertEquals(getGeneratedCode(exp.key.toString))
			}
			assertGeneratedJavaCodeCompiles
		]
	}

	private def assertNoValidationErrors(Result it) {
		val allErrors = getErrorsAndWarnings.filter[severity == Severity.ERROR]
		if (!allErrors.empty) {
			throw new IllegalStateException("One or more resources contained errors : "+
				Joiner.on(',').join(allErrors)
			);
		}
	}

	def private assertGeneratedJavaCode(CompilationTestHelper.Result r, CharSequence expected) {
		expected.toString.assertEquals(r.singleGeneratedCode)
	}

	def private assertGeneratedJavaCodeCompiles(CompilationTestHelper.Result r) {
		r.compiledClass // check Java compilation succeeds
	}
}
