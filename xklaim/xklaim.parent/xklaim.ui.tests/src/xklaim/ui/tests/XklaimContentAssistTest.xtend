package xklaim.ui.tests

import org.eclipse.core.runtime.CoreException
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(XklaimUiInjectorProvider)
class XklaimContentAssistTest extends AbstractContentAssistTest {

	@BeforeClass
	def static void setUp() throws CoreException {
		javaProject = JavaProjectSetupUtil.createJavaProject("contentAssistTest");
		IResourcesSetupUtil.addNature(javaProject.getProject(), XtextProjectHelper.NATURE_ID);
		IResourcesSetupUtil.waitForBuild
	}

	@Test def void testImportCompletion() {
		newBuilder.append('import java.util.Da').assertText('java.util.Date')
	}

	@Test def void testImportCompletion_1() {
		newBuilder.append('import LinkedHashSet').assertText('java.util.LinkedHashSet')
	}

}
