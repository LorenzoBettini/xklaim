package xklaim.ui.launching;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.xtext.xbase.ui.launching.JavaElementDelegate;
import org.eclipse.xtext.xbase.ui.launching.JavaElementDelegateAdapterFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class XklaimJavaElementDelegateAdapterFactory extends JavaElementDelegateAdapterFactory {

	@Inject
	private Provider<XklaimJavaElementDelegateMainLaunch> mainDelegateProvider;

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		JavaElementDelegate result = null;
		if (XklaimJavaElementDelegateMainLaunch.class.equals(adapterType)) {
			result = this.mainDelegateProvider.get();
		}
		if (result != null) {
			if (adaptableObject instanceof IFileEditorInput) {
				result.initializeWith((IFileEditorInput) adaptableObject);
				return adapterType.cast(result);
			}
			if (adaptableObject instanceof IResource) {
				result.initializeWith((IResource) adaptableObject);
				return adapterType.cast(result);
			}
			if (adaptableObject instanceof IEditorPart) {
				result.initializeWith((IEditorPart) adaptableObject);
				return adapterType.cast(result);
			}
		}
		return super.getAdapter(adaptableObject, adapterType);
	}
}
