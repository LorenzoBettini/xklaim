package xklaim.ui.tests

import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.testing.AbstractOutlineTest
import org.junit.Test
import org.junit.runner.RunWith
import xklaim.ui.internal.XklaimActivator

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
@RunWith(XtextRunner)
@InjectWith(XklaimUiInjectorProvider)
class XklaimOutlineTest extends AbstractOutlineTest {

	override protected getEditorId() {
		XklaimActivator.XKLAIM_XKLAIM
	}

	@Test def void testEmptyOutline() {
		'''
			
		'''.assertAllLabels(
			""
		)
	}

	@Test def void testOutline() {
		'''
			package mydsl
			
			proc MyProc(String s) {
				
			}
			net HelloNet physical "localhost:9999" {
				node Hello1 {
					println("Hello World!")
				}
				node Hello2 {
					println("Hello World!")
				}
			}
		'''.assertAllLabels(
			'''
				mydsl
				  MyProc
				  HelloNet
				    Hello1
				    Hello2
			'''
		)
	}
}
