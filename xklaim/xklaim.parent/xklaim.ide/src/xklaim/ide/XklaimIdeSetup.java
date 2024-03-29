/*
 * generated by Xtext 2.25.0
 */
package xklaim.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.xtext.util.Modules2;
import xklaim.XklaimRuntimeModule;
import xklaim.XklaimStandaloneSetup;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class XklaimIdeSetup extends XklaimStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new XklaimRuntimeModule(), new XklaimIdeModule()));
	}
	
}
