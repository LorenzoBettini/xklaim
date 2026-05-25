package xklaim.swtbot.tests;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.cleanWorkspace;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.root;
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class XklaimAbstractSwtbotTest {

	protected static final String TEST_PROJECT = "MyTestProject";
	protected static final String PROJECT_EXPLORER = "Project Explorer";
	private static final String XKLAIM_PROJECT_WIZARD_ID = "xklaim.ui.wizard.XklaimNewProjectWizard";
	protected static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
//		PDETargetPlatformUtils.setTargetPlatform();
		
		bot = new SWTWorkbenchBot();

		waitForWorkbenchWindow();
		closeWelcomePage();
		showView(IPageLayout.ID_PROBLEM_VIEW);
		showView(IPageLayout.ID_PROJECT_EXPLORER);
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
		runOnDisplay(new Runnable() {
			@Override
			public void run() {
				if (PlatformUI.getWorkbench().getIntroManager().getIntro() != null) {
					PlatformUI.getWorkbench().getIntroManager()
							.closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
				}
			}
		});
	}

	private static void waitForWorkbenchWindow() {
		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				final boolean[] available = new boolean[1];
				runOnDisplay(new Runnable() {
					@Override
					public void run() {
						IWorkbenchWindow window = getWorkbenchWindow();
						Shell shell = window != null ? window.getShell() : null;
						available[0] = shell != null && !shell.isDisposed();
					}
				});
				return available[0];
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "No workbench window";
			}
		});
	}

	private static void showView(final String viewId) {
		runOnDisplay(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = getWorkbenchWindow();
				if (window == null || window.getActivePage() == null) {
					throw new IllegalStateException("No workbench page available");
				}
				try {
					window.getActivePage().showView(viewId);
				} catch (PartInitException e) {
					throw new RuntimeException("Cannot show view " + viewId, e);
				}
			}
		});
	}

	private static IWorkbenchWindow getWorkbenchWindow() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window;
		}
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		return windows.length > 0 ? windows[0] : null;
	}

	private static void runOnDisplay(Runnable runnable) {
		Display display = Display.getDefault();
		if (display.getThread() == Thread.currentThread()) {
			runnable.run();
		} else {
			display.syncExec(runnable);
		}
	}

	private static void runAsyncOnDisplay(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	protected void disableBuildAutomatically() {
		setBuildAutomatically(false);
	}

	protected void enableBuildAutomatically() {
		setBuildAutomatically(true);
	}

	private void setBuildAutomatically(boolean shouldBeEnabled) {
		try {
			IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
			if (description.isAutoBuilding() != shouldBeEnabled) {
				description.setAutoBuilding(shouldBeEnabled);
				ResourcesPlugin.getWorkspace().setDescription(description);
			}
			assertEquals(shouldBeEnabled, ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
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
		openXklaimProjectWizard();

		SWTBotShell shell = bot.shell("New Template Project");
		shell.activate();

		bot.textWithLabel("Project name:").setText(projectName);

		bot.button("Finish").click();

		// creation of a project might require some time
		bot.waitUntil(shellCloses(shell), SWTBotPreferences.TIMEOUT);
		assertProjectCreated(projectName);
	}

	private void openXklaimProjectWizard() {
		final AtomicReference<RuntimeException> failure = new AtomicReference<RuntimeException>();
		runAsyncOnDisplay(new Runnable() {
			@Override
			public void run() {
				try {
					IWorkbenchWindow window = getWorkbenchWindow();
					if (window == null) {
						throw new IllegalStateException("No workbench window available");
					}
					IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry()
							.findWizard(XKLAIM_PROJECT_WIZARD_ID);
					if (descriptor == null) {
						throw new IllegalStateException("Cannot find wizard " + XKLAIM_PROJECT_WIZARD_ID);
					}
					IWorkbenchWizard wizard = descriptor.createWizard();
					wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
					new WizardDialog(window.getShell(), wizard).open();
				} catch (CoreException e) {
					failure.set(new RuntimeException("Cannot open wizard " + XKLAIM_PROJECT_WIZARD_ID, e));
				} catch (RuntimeException e) {
					failure.set(e);
				}
			}
		});
		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				if (failure.get() != null) {
					throw failure.get();
				}
				try {
					bot.shell("New Template Project");
					return true;
				} catch (WidgetNotFoundException e) {
					return false;
				}
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "Xklaim project wizard did not open";
			}
		});
	}

	protected void assertProjectCreated(String projectName) {
		assertTrue("Project doesn't exist: " + projectName, isProjectCreated(projectName));
	}

}
