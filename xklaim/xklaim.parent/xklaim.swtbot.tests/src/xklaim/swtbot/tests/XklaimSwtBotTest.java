package xklaim.swtbot.tests;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lorenzo Bettini
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class XklaimSwtBotTest extends XklaimAbstractSwtbotTest {

	private static final String HELLO_WORLD_XKLAIM = "Hello.xklaim";

	@Before
	public void createANewXklaimProject() throws CoreException {
		createProject();
	}

	@Test
	public void canRunAnXklaimFileAsJavaApplication() throws CoreException, OperationCanceledException, InterruptedException {
		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				System.out.println("**** Waiting for the plugin model...");
				return PDECore.getDefault().getModelManager().isInitialized();
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "Failed waiting for inizialize of plugin models";
			}
		});
		bot.waitUntil(new ICondition() {
			@Override
			public boolean test() throws Exception {
				System.out.println("**** WAITING FOR BUILD...");
				waitForBuild();
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				System.out.println("**** BUILD DONE");
				assertErrorsInProject(0);
				return true;
			}

			@Override
			public void init(SWTBot bot) {
			}

			@Override
			public String getFailureMessage() {
				return "build failed";
			}
		});
		SWTBotTreeItem tree = getProjectTreeItem(TEST_PROJECT)
				.expand()
				.expandNode("src")
				.expandNode("mydsl")
				.getNode(HELLO_WORLD_XKLAIM);
		checkLaunchContextMenu(tree.contextMenu("Run As"));
		checkLaunchContextMenu(tree.contextMenu("Debug As"));
	}

	private void checkLaunchContextMenu(SWTBotMenu contextMenu) {
		try {
			// depending on the installed features, on a new workbench, any file has "Run As
			// Java Application" as the
			// first menu, so we need to look for the second entry
			contextMenu.menu(WidgetMatcherFactory.withRegex("\\d Xklaim Application"), false, 0);
		} catch (WidgetNotFoundException e) {
			System.out.println("MENUS: " + contextMenu.menuItems());
			throw e;
		}
	}

}
