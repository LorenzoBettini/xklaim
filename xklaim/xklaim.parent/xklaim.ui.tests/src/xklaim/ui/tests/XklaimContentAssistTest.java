package xklaim.ui.tests;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.ui.PlatformUI;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XklaimUiInjectorProvider.class)
@SuppressWarnings("all")
public class XklaimContentAssistTest extends AbstractContentAssistTest {
	@BeforeClass
	public static void setUp() throws CoreException {
		var projectFactory = new PluginProjectFactory();
		projectFactory.setWorkspace(ResourcesPlugin.getWorkspace());
		projectFactory.setWorkbench(PlatformUI.getWorkbench());
		projectFactory.setProjectName("contentAssistTest");
		projectFactory.addProjectNatures(JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature", XtextProjectHelper.NATURE_ID);
		projectFactory.addBuilderIds(JavaCore.BUILDER_ID, XtextProjectHelper.BUILDER_ID);
		projectFactory.addFolders(java.util.Arrays.asList("src", "src-gen"));
		projectFactory.getRequiredBundles().add("xklaim.runtime");
		AbstractContentAssistTest.javaProject = JavaCore.create(projectFactory.createProject(IResourcesSetupUtil.monitor(), null));
		IResourcesSetupUtil.waitForBuild();
	}

	@Test
	public void testImportCompletion() throws Exception {
		newBuilder().append("import java.util.Da").assertProposal("java.util.Date");
	}

	@Test
	public void testImportCompletion_1() throws Exception {
		newBuilder().append("import LinkedHashSet").assertProposal("java.util.LinkedHashSet");
	}

	@Test
	public void testCompletionInKlaimOperation() throws Exception {
		newBuilder().append("""
				import klava.Locality
				import klava.PhysicalLocality

				proc TestProc(Locality locality, PhysicalLocality physicalLocality, String physicalString) {
					out(phys<|>)@self
				}
				""").assertTextAtCursorPosition("<|>",
						"physicalLocality",
						"physicalString");
	}

	@Test
	public void testLocalityCompletion() throws Exception {
		newBuilder().append("""
				import klava.Locality
				import klava.PhysicalLocality

				proc TestProc(Locality locality, PhysicalLocality physicalLocality, String physicalString) {
					out("hello")@phys<|>
				}
				""").assertTextAtCursorPosition("<|>", "physicalLocality");
	}

	@Test
	public void testLocalityCompletionIncludesSelfWithPrefix() throws Exception {
		newBuilder().append("""
				proc TestProc(String string) {
					out("hello")@s<|>
				}
				""").assertTextAtCursorPosition("<|>", "self");
	}

	@Test
	public void testLocalityCompletionIncludesSelfWithoutPrefix() throws Exception {
		newBuilder().append("""
				proc TestProc(String string) {
					out("hello")@<|>
				}
				""").assertTextAtCursorPosition("<|>",
				"getPhysical()",
				"logloc()",
				"phyloc()",
				"self",
				"toPhysical()",
				"translateLocality()",
				"translateSelf");
	}
}
