package xklaim.ui.launching;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.xbase.ui.launching.JavaApplicationLaunchShortcut;
import org.eclipse.xtext.xbase.ui.launching.LaunchShortcutUtil;

public class XklaimJavaApplicationLaunchShortcut extends JavaApplicationLaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection newSelection = LaunchShortcutUtil
					.replaceWithJavaElementDelegates((IStructuredSelection) selection, XklaimJavaElementDelegateMainLaunch.class);
			super.launch(newSelection, mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		final XklaimJavaElementDelegateMainLaunch javaElementDelegate = editor.getAdapter(XklaimJavaElementDelegateMainLaunch.class);
		if (javaElementDelegate != null) {
			super.launch(new StructuredSelection(javaElementDelegate), mode);
		} else {
			super.launch(editor, mode);
		}
	}
}
