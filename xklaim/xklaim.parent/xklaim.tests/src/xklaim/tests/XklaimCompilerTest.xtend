/*
 * generated by Xtext 2.16.0
 */
package xklaim.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule
import org.eclipse.xtext.xbase.testing.TemporaryFolder
import org.eclipse.xtext.xbase.testing.CompilationTestHelper
import org.eclipse.xtext.xbase.testing.CompilationTestHelper.Result
import org.eclipse.xtext.diagnostics.Severity
import com.google.common.base.Joiner

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(XklaimInjectorProvider)
class XklaimCompilerTest {
	@Rule @Inject public TemporaryFolder temporaryFolder
	@Inject extension CompilationTestHelper

	@Test
	def void testProgramWithNode() {
		'''
		package foo
		node TestNode {
			println("Hello")
		}
		'''.checkCompilation(
			"foo.TestNode" ->
			'''
			package foo;
			
			import klava.topology.KlavaNode;
			import klava.topology.KlavaProcess;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNode extends KlavaNode {
			  private static class TestNodeProcess extends KlavaProcess {
			    @Override
			    public void executeProcess() {
			      InputOutput.<String>println("Hello");
			    }
			  }
			  
			  public void addMainProcess() throws IMCException {
			    addNodeProcess(new TestNode.TestNodeProcess());
			  }
			}
			''',
			"foo.MyFile" ->
			'''
			package foo;
			
			import foo.TestNode;
			
			@SuppressWarnings("all")
			public class MyFile {
			  public static void main(final String[] args) throws Exception {
			    TestNode testNode = new TestNode();
			    testNode.addMainProcess();
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
				println("Hello")
			}
		}
		'''.checkCompilation(
			"foo.TestNet" ->
			'''
			package foo;
			
			import klava.PhysicalLocality;
			import klava.topology.KlavaNode;
			import klava.topology.KlavaProcess;
			import klava.topology.Net;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			import org.mikado.imc.common.IMCException;
			
			@SuppressWarnings("all")
			public class TestNet extends Net {
			  public static class TestNode extends KlavaNode {
			    private static class TestNodeProcess extends KlavaProcess {
			      @Override
			      public void executeProcess() {
			        InputOutput.<String>println("Hello");
			      }
			    }
			    
			    public void addMainProcess() throws IMCException {
			      addNodeProcess(new TestNet.TestNode.TestNodeProcess());
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
			''',
			"foo.MyFile" ->
			'''
			package foo;
			
			import foo.TestNet;
			
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
			    super("foo.TestProcess");
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
			out(s)@self
		}
		'''.checkCompilation(
			'''
			package foo;
			
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			  
			  public TestProcess(final String s) {
			    super("foo.TestProcess");
			    this.s = s;
			  }
			  
			  @Override
			  public void executeProcess() {
			    out(new Tuple(new Object[] {this.s}), this.self);
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
			
			import foo.AnotherProcess;
			import klava.Tuple;
			import klava.topology.KlavaProcess;
			
			@SuppressWarnings("all")
			public class TestProcess extends KlavaProcess {
			  private String s;
			  
			  public TestProcess(final String s) {
			    super("foo.TestProcess");
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
	def void testXklaimOperationsWithNestedProcess() {
		'''
		package foo
		
		proc TestProcess(String s) {
			val i = 10
			out({ println(s + i) }, s+i)@self
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
			    super("foo.TestProcess");
			    this.s = s;
			  }
			  
			  @Override
			  public void executeProcess() {
			    final int i = 10;
			    KlavaProcess _Proc = new KlavaProcess() {
			      @Override public void executeProcess() {
			        InputOutput.<String>println((TestProcess.this.s + Integer.valueOf(i)));
			      }
			    };
			    
			    out(new Tuple(new Object[] {_Proc, (this.s + Integer.valueOf(i))}), this.self);
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
