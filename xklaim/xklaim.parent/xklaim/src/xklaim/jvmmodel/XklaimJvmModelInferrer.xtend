/*
 * generated by Xtext 2.16.0
 */
package xklaim.jvmmodel

import com.google.inject.Inject
import klava.LogicalLocality
import klava.PhysicalLocality
import klava.topology.ClientNode
import klava.topology.KlavaNode
import klava.topology.KlavaProcess
import klava.topology.LogicalNet
import org.eclipse.xtext.common.types.JvmAnnotationTarget
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.mikado.imc.common.IMCException
import xklaim.xklaim.XklaimAbstractNode
import xklaim.xklaim.XklaimModel
import xklaim.xklaim.XklaimNet
import xklaim.xklaim.XklaimProcess

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
	def dispatch void infer(XklaimModel program, extension IJvmDeclaredTypeAcceptor acceptor,
		boolean isPreIndexingPhase) {
		val nodes = program.nodes
		val nets = program.nets
		val nodeClasses = newArrayList
		val netClasses = newArrayList
		for (p : program.processes) {
			p.toProcessClass(acceptor)
		}
		if (!nodes.empty || !nets.empty) {
			for (node : nodes) {
				nodeClasses += toNodeClass(node, KlavaNode, acceptor, [])
			}
			for (net : nets) {
				netClasses += toNetClass(net, acceptor)
			}
			val modelFQN = program.fullyQualifiedName
			val javaClassName = program.eResource().getURI().trimFileExtension().lastSegment()
			val javaClassFQN = if (modelFQN !== null) {
					modelFQN.toString + "." + javaClassName
				} else {
					javaClassName
				}
			accept(program.toClass(javaClassFQN)) [
				members += program.toMethod('main', typeRef(Void.TYPE)) [
					parameters += program.toParameter("args", typeRef(String).addArrayTypeDimension)
					static = true
					exceptions += Exception.typeRef()
					body = '''
						«FOR nodeClass : nodeClasses»
							«nodeClass» «nodeClass.simpleName.toFirstLower» = new «nodeClass»();
						«ENDFOR»
						«FOR nodeClass : nodeClasses»
							«nodeClass.simpleName.toFirstLower».addMainProcess();
						«ENDFOR»
						«FOR netClass : netClasses»
							«netClass» «netClass.simpleName.toFirstLower» = new «netClass»();
						«ENDFOR»
						«FOR netClass : netClasses»
							«netClass.simpleName.toFirstLower».addNodes();
						«ENDFOR»
					'''
				]
			]
		}
	}

	private def JvmGenericType toNodeClass(XklaimAbstractNode node, Class<? extends KlavaNode> clazz,
			extension IJvmDeclaredTypeAcceptor acceptor, (JvmDeclaredType)=>void typeEnricher) {
		val nodeFQN = node.fullyQualifiedName
		val nodeClass = node.toClass(nodeFQN)
		val nodeProcessClass = node.toClass(nodeFQN + "Process")
		accept(nodeProcessClass) [
			declaringType = nodeClass
			static = true
			visibility = JvmVisibility.PRIVATE
			superTypes += KlavaProcess.typeRef()
			members += node.toMethod("executeProcess", typeRef(Void.TYPE)) [
				addOverrideAnnotation()
				body = node.body
			]
		]
		accept(nodeClass) [
			documentation = node.documentation
			superTypes += clazz.typeRef()
			typeEnricher.apply(it)
			members += node.toMethod("addMainProcess", typeRef(Void.TYPE)) [
				exceptions += IMCException.typeRef()
				body = '''
					addNodeProcess(new «nodeProcessClass»());
				'''
			]
		]
		nodeClass
	}

	private def JvmGenericType toNetClass(XklaimNet net, extension IJvmDeclaredTypeAcceptor acceptor) {
		val netFQN = net.fullyQualifiedName
		val netClass = net.toClass(netFQN)
		val nodeClasses = newArrayList
		val nodes = net.nodes
		for (node : nodes) {
			nodeClasses += toNodeClass(node, ClientNode, acceptor) [
				members += node.toConstructor [
					if (node.logicalLocality !== null) {
						body = '''
						super(new «PhysicalLocality»("«net.physicalLocality»"), new «LogicalLocality»("«node.logicalLocality»"));
						'''
					} else {
						body = '''
						super(new «PhysicalLocality»("«net.physicalLocality»"));
						'''
					}
				]
			]
		}
		accept(netClass) [
			documentation = net.documentation
			superTypes += LogicalNet.typeRef()
			for (nodeClass : nodeClasses) {
				nodeClass.declaringType = netClass
				nodeClass.static = true
			}
			members += net.toConstructor [
				exceptions += IMCException.typeRef()
				body = '''
					super(new «PhysicalLocality»("«net.physicalLocality»"));
				'''
			]
			members += net.toMethod("addNodes", typeRef(Void.TYPE)) [
				exceptions += IMCException.typeRef()
				body = '''
					«FOR nodeClass : nodeClasses»
						«nodeClass» «nodeClass.simpleName.toFirstLower» = new «nodeClass»();
					«ENDFOR»
					«FOR nodeClass : nodeClasses»
						«nodeClass.simpleName.toFirstLower».addMainProcess();
					«ENDFOR»
				'''
			]
		]
		netClass
	}

	def private toProcessClass(XklaimProcess process, extension IJvmDeclaredTypeAcceptor acceptor) {
		accept(process.toClass(process.fullyQualifiedName)) [
			documentation = process.documentation
			superTypes += KlavaProcess.typeRef()
			for (p : process.params) {
				members += p.toField(p.name, p.parameterType)
			}
			members += process.toConstructor [
				for (p : process.params) {
					parameters += p.toParameter(p.name, p.parameterType)
				}
				body = '''
				super("«process.fullyQualifiedName»");
				«FOR p : process.params»
				this.«p.name» = «p.name»;
				«ENDFOR»
				'''
			]
			members += process.toMethod("executeProcess", typeRef(Void.TYPE)) [
				addOverrideAnnotation()
				body = process.body
			]
		]
	}

	def private void addOverrideAnnotation(JvmAnnotationTarget it) {
		annotations += Override.annotationRef()
	}
}
