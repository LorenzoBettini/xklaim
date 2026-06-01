package xklaim.ui.tests;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.createFile;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.monitor;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.AbstractWorkbenchTest;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XklaimUiInjectorProvider.class)
public class XklaimWorkbenchTest extends AbstractWorkbenchTest {
	private static final String TEST_PROJECT = "xklaim.ui.tests.project";
	private static final String TEST_FILE = "TestFile";
	private static final String PLUGIN_NATURE = "org.eclipse.pde.PluginNature";

	public IFile createTestFile(CharSequence contents) throws Exception {
		return createFile(TEST_PROJECT + "/src/" + TEST_FILE + ".xklaim", contents.toString());
	}

	@Test
	public void testErrorInGeneratedJavaCodeIsCopiedToOriginalFile() throws Exception {
		createTestProject();
		var source = createTestFile("""
				net TestNet physical "tcp-127.0.0.1:9999" {
					node Reader logical "reader" {
						eval({
							done()
						})@writer
					}
					node Writer logical "writer" {
					}
				}
				""");
		waitForBuild();

		var messages = Arrays.stream(source.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO))
				.filter(marker -> getSeverity(marker) == IMarker.SEVERITY_ERROR)
				.map(XklaimWorkbenchTest::getMessage)
				.collect(Collectors.joining("\n"));
		assertTrue("Expected a copied Java problem marker on the original file, but got:\n" + messages,
				messages.contains("Java problem:") && messages.contains("done()"));
	}

	private static int getSeverity(IMarker marker) {
		return marker.getAttribute(IMarker.SEVERITY, -1);
	}

	private static String getMessage(IMarker marker) {
		return marker.getAttribute(IMarker.MESSAGE, "");
	}

	private IProject createTestProject() {
		var projectFactory = new PluginProjectFactory();
		projectFactory.setWorkspace(ResourcesPlugin.getWorkspace());
		projectFactory.setWorkbench(PlatformUI.getWorkbench());
		projectFactory.setProjectName(TEST_PROJECT);
		projectFactory.addProjectNatures(JavaCore.NATURE_ID, PLUGIN_NATURE, XtextProjectHelper.NATURE_ID);
		projectFactory.addBuilderIds(JavaCore.BUILDER_ID, XtextProjectHelper.BUILDER_ID);
		projectFactory.addFolders(Arrays.asList("src", "src-gen"));
		projectFactory.getRequiredBundles().add("xklaim.runtime");
		var project = projectFactory.createProject(monitor(), null);
		waitForBuild();
		return project;
	}
}
