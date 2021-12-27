package xklaim.swtbot.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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
		createProjectAndAssertNoErrorMarker();
	}

	@Test
	public void canRunAnXklaimFileAsJavaApplication() throws CoreException {
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
			contextMenu.menu("1 Xklaim Application");
		} catch (WidgetNotFoundException e) {
			contextMenu.menu("2 Xklaim Application");
		}
	}

}
