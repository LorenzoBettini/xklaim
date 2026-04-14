package xklaim.ui.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XklaimUiInjectorProvider.class)
@SuppressWarnings("all")
public class XklaimContentAssistTest extends AbstractContentAssistTest {
	@BeforeClass
	public static void setUp() throws CoreException {
		AbstractContentAssistTest.javaProject = JavaProjectSetupUtil.createJavaProject("contentAssistTest");
		IResourcesSetupUtil.addNature(AbstractContentAssistTest.javaProject.getProject(), XtextProjectHelper.NATURE_ID);
		IResourcesSetupUtil.waitForBuild();
	}

	@Test
	public void testImportCompletion() throws Exception {
		newBuilder().append("import java.util.Da").assertText("java.util.Date");
	}

	@Test
	public void testImportCompletion_1() throws Exception {
		newBuilder().append("import LinkedHashSet").assertText("java.util.LinkedHashSet");
	}
}
