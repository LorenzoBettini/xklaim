package xklaim.scoping;

import java.util.List;

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures;

import xklaim.runtime.util.XklaimRuntimeUtil;

/**
 * Makes the static methods of {@link XklaimRuntimeUtil} automatically
 * available.
 * 
 * @author Lorenzo Bettini
 */
public class XklaimImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	@Override
	protected List<Class<?>> getStaticImportClasses() {
		List<Class<?>> staticImportClasses = super.getStaticImportClasses();
		staticImportClasses.add(XklaimRuntimeUtil.class);
		return staticImportClasses;
	}
}
