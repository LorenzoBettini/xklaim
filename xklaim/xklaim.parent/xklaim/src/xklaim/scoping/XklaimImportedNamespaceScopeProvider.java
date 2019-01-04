/**
 * 
 */
package xklaim.scoping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;

/**
 * Implicitly imported packages for our runtime library
 * 
 * @author Lorenzo Bettini
 *
 */
public class XklaimImportedNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {

	public static final QualifiedName XKLAIM_LIB = QualifiedName.create("xklaim","runtime","util");
	public static final QualifiedName KLAVA_LIB = QualifiedName.create("klava");
	public static final QualifiedName KLAVA_TOPOLOGY_LIB = QualifiedName.create("klava","topology");

	@Override
	protected List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
		List<ImportNormalizer> implicitImports = new ArrayList<>(super.getImplicitImports(ignoreCase));
		implicitImports.add(doCreateImportNormalizer(XKLAIM_LIB, true, false));
		implicitImports.add(doCreateImportNormalizer(KLAVA_LIB, true, false));
		implicitImports.add(doCreateImportNormalizer(KLAVA_TOPOLOGY_LIB, true, false));
		return implicitImports;
	}

}