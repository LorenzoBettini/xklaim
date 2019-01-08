package xklaim.scoping

import org.eclipse.xtext.xbase.scoping.batch.ImplicitlyImportedFeatures
import xklaim.runtime.util.XklaimRuntimeUtil

/**
 * Makes the static methods of {@link XklaimRuntimeUtil} automatically available.
 * 
 * @author Lorenzo Bettini
 */
class XklaimImplicitlyImportedFeatures extends ImplicitlyImportedFeatures {
	override protected getStaticImportClasses() {
		(super.getStaticImportClasses() + #[XklaimRuntimeUtil])
			.toList
	}
}
