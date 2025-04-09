package xklaim.ui.examples;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.CommonUIPlugin;
import org.eclipse.emf.common.ui.wizard.ExampleInstallerWizard;
import org.eclipse.emf.common.util.BasicMonitor;

/**
 * Forces a clean build of the project, so that generated files (and trace files
 * for running and debugging) are ensured to be generated.
 * 
 * @author Lorenzo Bettini
 *
 */
public class XklaimExampleInstallerWizard extends ExampleInstallerWizard {

	@Override
	protected void openFiles(IProgressMonitor monitor) {
		super.openFiles(monitor);
		List<IProject> projects = projectDescriptors.stream().map(ProjectDescriptor::getProject)
				.collect(Collectors.toList());
		monitor.beginTask("Building projects", projects.size());
		for (IProject project : projects) {
			var subProgress = BasicMonitor.subProgress(monitor, 1);
			subProgress.beginTask("Building project " + project.getName(), 1);
			try {
				project.build(IncrementalProjectBuilder.CLEAN_BUILD, subProgress);
			} catch (CoreException e) {
				CommonUIPlugin.INSTANCE.log(e);
			} finally {
				subProgress.done();
			}
		}
		monitor.done();
	}
}
