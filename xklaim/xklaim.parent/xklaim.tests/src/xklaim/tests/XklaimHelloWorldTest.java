package xklaim.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.TemporaryFolder;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import klava.topology.KlavaNode;

@RunWith(XtextRunner.class)
@InjectWith(XklaimInjectorProvider.class)
public class XklaimHelloWorldTest {

	@Rule
	@Inject
	public TemporaryFolder temporaryFolder;
	@Inject
	CompilationTestHelper compilationTestHelper;

	@Test
	public void testHelloWorldPrintsMessage() throws Exception {
		var source = """
				package test
				node Hello {
					out("Hello World")@self
					in(var String message)@self
					println(message)
					done()
				}
				""";

		var baos = new ByteArrayOutputStream();
		var savedOut = System.out;
		System.setOut(new PrintStream(baos));
		try {
			compilationTestHelper.compile(source, result -> {
				try {
					Class<?> helloClass = result.getCompiledClass("test.Hello");
					KlavaNode hello = (KlavaNode) helloClass.getDeclaredConstructor().newInstance();
					helloClass.getMethod("addMainProcess").invoke(hello);
					hello.waitForCompletion(5_000L);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} finally {
			System.setOut(savedOut);
		}
		Assert.assertTrue(
				"Expected 'Hello World' in output but got: " + baos.toString(),
				baos.toString().contains("Hello World"));
	}
}
