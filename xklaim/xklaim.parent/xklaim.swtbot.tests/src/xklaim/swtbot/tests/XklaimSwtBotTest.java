package xklaim.swtbot.tests;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
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
	public void canRunAnXklaimFileAsJavaApplication() throws CoreException {
		System.out.println("**** WAITING FOR BUILD...");
		waitForBuild();
		assertErrorsInProject(0);
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
