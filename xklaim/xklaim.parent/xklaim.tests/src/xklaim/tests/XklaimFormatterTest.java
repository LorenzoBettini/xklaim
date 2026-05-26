package xklaim.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(XklaimInjectorProvider.class)
public class XklaimFormatterTest {

	@Inject
	FormatterTestHelper formatterTestHelper;

	@Test
	public void testFormatter() {
		formatterTestHelper.assertFormatted(it -> {
			it.setExpectation("""
					proc MyProc(String s) {
						println("Hello")
						while (true) {
						}
					}
					
					node MyNode physical "tcp-127.0.0.1:9999" {
						println("Hello")
						while (true) {
						}
					}
					
					net HelloNet physical "localhost:9999" {
						node Hello {
							println("Hello World!")
							System.exit(0)
						}
						node Hello2 {
							println("Hello World!")
							System.exit(0)
						}
					}
					""".replace("\n", System.lineSeparator()));
			it.setToBeFormatted(
					"""
						proc MyProc(String s) { println("Hello") while(true) {} }
						
						node MyNode physical "tcp-127.0.0.1:9999" { println("Hello") while(true) {}}
						
						net HelloNet physical "localhost:9999" { 					node Hello {
								println("Hello World!")
								System.exit(0)
							} node Hello2 {											println("Hello World!")	System.exit(0)
												}
						}
						""".replace("\n", System.lineSeparator()));
		});
	}

	@Test
	public void testFormatterOrWithInlineProcBlocks() {
		formatterTestHelper.assertFormatted(it -> {
			it.setExpectation("""
					proc TestProcess(String s) {
						or(
							proc {
								in(var Integer i)@self
								println(i)
							},
							proc {
								read(var String s2)@self
								println(s2)
							}
						)
					}
					""".replace("\n", System.lineSeparator()));
			it.setToBeFormatted("""
					proc TestProcess(String s) {
						or(proc { in(var Integer i)@self println(i) }, proc { read(var String s2)@self println(s2) })
					}
					""".replace("\n", System.lineSeparator()));
		});
	}
}
