/**
 * generated by Xtext 2.25.0
 */
package xklaim;

/**
 * Initialization support for running Xtext languages without Equinox extension
 * registry.
 */
public class XklaimStandaloneSetup extends XklaimStandaloneSetupGenerated {
	public static void doSetup() {
		new XklaimStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}