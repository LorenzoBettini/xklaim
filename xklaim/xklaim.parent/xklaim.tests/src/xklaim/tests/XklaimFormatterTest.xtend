package xklaim.tests

import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.formatter.FormatterTestHelper
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(XklaimInjectorProvider)
class XklaimFormatterTest {

	@Inject extension FormatterTestHelper

	@Test def void testFormatter() {
		assertFormatted[
			expectation = '''
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
			'''
			toBeFormatted = '''
				proc MyProc(String s) { println("Hello") while(true) {} }
				
				node MyNode physical "tcp-127.0.0.1:9999" { println("Hello") while(true) {}}
				
				net HelloNet physical "localhost:9999" { 					node Hello {
						println("Hello World!")
						System.exit(0)
					} node Hello2 {											println("Hello World!")	System.exit(0)
										}
				}
			'''
		]
	}

}
