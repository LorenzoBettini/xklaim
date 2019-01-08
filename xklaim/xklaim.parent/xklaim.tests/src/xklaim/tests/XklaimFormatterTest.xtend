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
			'''
			toBeFormatted = '''
				proc MyProc(String s) { println("Hello") while(true) {} }
			'''
		]
	}

}
