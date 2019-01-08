package xklaim.ui.tests

import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(XklaimUiInjectorProvider)
class XklaimContentAssistTest extends AbstractContentAssistTest {

	@Test def void testImportCompletion() {
		newBuilder.append('import java.util.Da').assertText('java.util.Date')
	}

	@Test def void testImportCompletion_1() {
		newBuilder.append('import LinkedHashSet').assertText('java.util.LinkedHashSet')
	}

}
