package xklaim.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.cleanWorkspace;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.root;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class XklaimAbstractSwtbotTest {

	protected static final String TEST_PROJECT = "MyTestProject";
	protected static final String PROJECT_EXPLORER = "Project Explorer";
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
//		PDETargetPlatformUtils.setTargetPlatform();
		
		bot = new SWTWorkbenchBot();

		closeWelcomePage();

		bot.viewByPartName("Problems").show();
		bot.menu("Window").menu("Show View").menu(PROJECT_EXPLORER).click();
	}

	@AfterClass
	public static void afterClass() {
		bot.resetWorkbench();
	}
	
	@After
	public void runAfterEveryTest() throws CoreException {
		cleanWorkspace();
		waitForBuild();
	}

	protected static void closeWelcomePage() throws InterruptedException {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (PlatformUI.getWorkbench().getIntroManager().getIntro() != null) {
					PlatformUI.getWorkbench().getIntroManager()
							.closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
				}
			}
		});
	}

	protected void disableBuildAutomatically() {
		clickOnBuildAutomatically(false);
	}

	protected void enableBuildAutomatically() {
		clickOnBuildAutomatically(true);
	}

	private void clickOnBuildAutomatically(boolean shouldBeEnabled) {
		if (buildAutomaticallyMenu().isChecked() == shouldBeEnabled)
			return;
		// see http://www.eclipse.org/forums/index.php/mv/msg/165852/#msg_525521
		// for the reason why we need to specify 1
		buildAutomaticallyMenu().click();
		assertEquals(shouldBeEnabled, buildAutomaticallyMenu().isChecked());
	}

	private SWTBotMenu buildAutomaticallyMenu() {
		return bot.menu("Project", 1).menu("Build Automatically");
	}

	protected boolean isProjectCreated(String name) {
		try {
			getProjectTreeItem(name);
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	protected boolean isFileCreated(String project, String... filePath) {
		try {
			getProjectTreeItem(project).expand().expandNode(filePath);
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	protected static SWTBotTree getProjectTree() {
		SWTBotView packageExplorer = getProjectExplorer();
		SWTBotTree tree = packageExplorer.bot().tree();
		return tree;
	}

	protected static SWTBotView getProjectExplorer() {
		SWTBotView view = bot.viewByTitle(PROJECT_EXPLORER);
		return view;
	}

	protected SWTBotTreeItem getProjectTreeItem(String myTestProject) {
		return getProjectTree().getTreeItem(myTestProject);
	}

	protected void assertErrorsInProject(int numOfErrors) throws CoreException {
		IMarker[] markers = root().findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);
		List<IMarker> errorMarkers = new LinkedList<IMarker>();
		for (int i = 0; i < markers.length; i++) {
			IMarker iMarker = markers[i];
			if (iMarker.getAttribute(IMarker.SEVERITY).toString()
					.equals("" + IMarker.SEVERITY_ERROR)) {
				errorMarkers.add(iMarker);
			}
		}
		assertEquals(
				"error markers: " + printMarkers(errorMarkers), numOfErrors,
				errorMarkers.size());
	}

	private String printMarkers(List<IMarker> errorMarkers) {
		StringBuffer buffer = new StringBuffer();
		for (IMarker iMarker : errorMarkers) {
			try {
				buffer.append(iMarker.getAttribute(IMarker.MESSAGE) + "\n");
				buffer.append(iMarker.getAttribute(IMarker.SEVERITY) + "\n");
			} catch (CoreException e) {
			}
		}
		return buffer.toString();
	}

	protected void createProjectAndAssertNoErrorMarker()
			throws CoreException {
		createProject();
		assertErrorsInProject(0);
	}

	protected void createProject() {
		createProjectAndAssertCreated(TEST_PROJECT);
	}

	protected void createProjectAndAssertCreated(String projectName) {
		bot.menu("File").menu("New").menu("Xklaim Project").click();

		SWTBotShell shell = bot.shell("New Template Project");
		shell.activate();

		bot.textWithLabel("Project name:").setText(TEST_PROJECT);

		bot.button("Finish").click();

		// creation of a project might require some time
		bot.waitUntil(shellCloses(shell), SWTBotPreferences.TIMEOUT);
		assertProjectCreated(projectName);
	}

	protected void assertProjectCreated(String projectName) {
		assertTrue("Project doesn't exist: " + projectName, isProjectCreated(projectName));
	}

}
