/*
 * generated by Xtext 2.16.0
 */
package xklaim.jvmmodel

import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import xklaim.xklaim.XklaimModel
import klava.topology.KlavaNode

/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class XklaimJvmModelInferrer extends AbstractModelInferrer {

	/**
	 * convenience API to build and initialize JVM types and their members.
	 */
	@Inject extension JvmTypesBuilder

	@Inject extension IQualifiedNameProvider

	/**
	 * The dispatch method {@code infer} is called for each instance of the
	 * given element's type that is contained in a resource.
	 * 
	 * @param element
	 *            the model to create one or more
	 *            {@link JvmDeclaredType declared
	 *            types} from.
	 * @param acceptor
	 *            each created
	 *            {@link JvmDeclaredType type}
	 *            without a container should be passed to the acceptor in order
	 *            get attached to the current resource. The acceptor's
	 *            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)
	 *            accept(..)} method takes the constructed empty type for the
	 *            pre-indexing phase. This one is further initialized in the
	 *            indexing phase using the lambda you pass as the last argument.
	 * @param isPreIndexingPhase
	 *            whether the method is called in a pre-indexing phase, i.e.
	 *            when the global index is not yet fully updated. You must not
	 *            rely on linking using the index if isPreIndexingPhase is
	 *            <code>true</code>.
	 */
	def dispatch void infer(XklaimModel program, extension IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		val nodes = program.nodes
		if (!nodes.empty) {
			for (node : nodes) {
				accept(node.toClass(node.fullyQualifiedName)) [
					documentation = node.documentation
					superTypes += KlavaNode.typeRef()
				]
			}
			val modelFQN = program.fullyQualifiedName
			val javaClassName = program.eResource().getURI().trimFileExtension().lastSegment()
			val javaClassFQN = if (modelFQN !== null) { modelFQN.toString + "." + javaClassName} else { javaClassName }
			accept(program.toClass(javaClassFQN)) [
				members += program.toMethod('main', typeRef(Void.TYPE)) [
					parameters += program.toParameter("args", typeRef(String).addArrayTypeDimension)
					static = true
					// Associate the script as the body of the main method
					body = '''
					'''
				]
			]
		}
	}
}
