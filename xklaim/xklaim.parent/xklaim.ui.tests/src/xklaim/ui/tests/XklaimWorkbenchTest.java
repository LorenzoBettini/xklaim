package xklaim.ui.tests;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.addBuilder;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.addNature;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.cleanWorkspace;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.createFile;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XklaimUiInjectorProvider.class)
public class XklaimWorkbenchTest {
	private static final String TEST_PROJECT = "xklaim.ui.tests.project";
	private static final String TEST_FILE = "TestFile";
	private static final String PLUGIN_NATURE = "org.eclipse.pde.PluginNature";

	private static IProject project;

	@BeforeClass
	public static void createTestProject() throws Exception {
		cleanWorkspace();
		var javaProject = JavaProjectSetupUtil.createJavaProject(TEST_PROJECT);
		project = javaProject.getProject();
		JavaProjectSetupUtil.addSourceFolder(javaProject, "src-gen");
		JavaProjectSetupUtil.addToClasspath(javaProject,
				JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
		addNature(project, PLUGIN_NATURE);
		addNature(project, XtextProjectHelper.NATURE_ID);
		addBuilder(project, XtextProjectHelper.BUILDER_ID);
		createFile(TEST_PROJECT + "/META-INF/MANIFEST.MF", """
				Manifest-Version: 1.0
				Bundle-ManifestVersion: 2
				Bundle-Name: xklaim.ui.tests.project
				Bundle-SymbolicName: xklaim.ui.tests.project
				Bundle-Version: 1.0.0.qualifier
				Require-Bundle: xklaim.runtime
				Bundle-RequiredExecutionEnvironment: JavaSE-21
				""");
		waitForBuild();
	}

	public IFile createTestFile(CharSequence contents) throws Exception {
		return createFile(TEST_PROJECT + "/src/" + TEST_FILE + ".xklaim", contents.toString());
	}

	@Test
	public void testErrorInGeneratedJavaCodeIsCopiedToOriginalFile() throws Exception {
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
}
