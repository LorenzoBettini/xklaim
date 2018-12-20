package xklaim.compiler

import com.google.inject.Inject
import klava.Tuple
import klava.topology.KlavaProcess
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XIfExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.XWhileExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import xklaim.util.XklaimModelUtil
import xklaim.xklaim.XklaimAbstractOperation

class XklaimXbaseCompiler extends XbaseCompiler {

	@Inject extension XklaimModelUtil

	override protected doInternalToJavaStatement(XExpression e, ITreeAppendable appendable, boolean isReferenced) {
		switch (e) {
			XklaimAbstractOperation: {
				compileXklaimOperationAsStatement(e, appendable, isReferenced);
			}
			default:
				super.doInternalToJavaStatement(e, appendable, isReferenced)
		}
	}

	override protected internalToConvertedExpression(XExpression e, ITreeAppendable appendable) {
		switch (e) {
			XklaimAbstractOperation: {
				appendable.append(getVarName(e, appendable))
			}
			default:
				super.internalToConvertedExpression(e, appendable)
		}
	}

	override protected internalCanCompileToJavaExpression(XExpression expression, ITreeAppendable appendable) {
		if (expression instanceof XklaimAbstractOperation) {
			return false
		}
		return super.internalCanCompileToJavaExpression(expression, appendable)
	}

	override protected _toJavaStatement(XIfExpression e, ITreeAppendable b, boolean isReferenced) {
		precompileVariableDeclarationsForFormalFields(e.^if, b)
		super._toJavaStatement(e, b, isReferenced)
	}

	override protected _toJavaStatement(XWhileExpression e, ITreeAppendable b, boolean isReferenced) {
		precompileVariableDeclarationsForFormalFields(e.predicate, b)
		super._toJavaStatement(e, b, isReferenced)
	}

	private def ITreeAppendable compileXklaimOperationAsStatement(XklaimAbstractOperation e, ITreeAppendable appendable,
		boolean isReferenced) {
		val arguments = e.arguments
		val hasFormalFields = e.containsFormalFields

		var String tupleName
		if (hasFormalFields) {
			for (a : arguments) {
				if (a.isFormalField && !appendable.hasName(a)) {
					val varDecl = a as XVariableDeclaration
					if (varDecl.isWriteable) {
						varDecl.internalToJavaStatement(appendable, false)
					} else {
						appendable.newLine
						appendVariableTypeAndName(varDecl, appendable)
						appendable.append(";")
					}
				}
			}

			tupleName = appendable.declareSyntheticVariable(new Object(), "_Tuple")
			appendable.newLine
			appendable.append(Tuple)
			appendable.append(" " + tupleName + " = ")
			compileNewTuple(appendable, arguments)
			appendable.append(";")
		}

		for (a : arguments) {
			if (a instanceof XBlockExpression) {
				val procVarName = appendable.declareSyntheticVariable(a, "_Proc")
				appendable.newLine
				appendable.append(KlavaProcess)
				appendable.append(" " + procVarName + " = new ")
				appendable.append(KlavaProcess)
				appendable.append("() {")
				appendable.increaseIndentation.newLine
				appendable.append("@Override public void executeProcess() {")
				appendable.increaseIndentation
				// we need to reassign the mapping for this since we generate an
				// anonymous innerclass, instead of
				// this.field
				// we must generate
				// CurrentType.this.field
				appendable.openScope
				val mappedThis = appendable.getObject("this") as JvmDeclaredType
				appendable.declareVariable(mappedThis, mappedThis.simpleName + ".this")
				a.internalToJavaStatement(appendable, false)
				appendable.closeScope
				appendable.decreaseIndentation.newLine
				appendable.append("}")
				appendable.decreaseIndentation.newLine
				appendable.append("};")
			} else if (!a.isFormalField) {
				a.internalToJavaStatement(appendable, true)
			}
		}
		e.locality.internalToJavaStatement(appendable, true)

		if (isReferenced) {
			val opVar = appendable.declareSyntheticVariable(e, "_" + e.op)
			appendable.newLine
			appendable.append(Boolean.TYPE)
			appendable.append(" " + opVar + " = ")
		} else {
			appendable.newLine
		}

		appendable.append(e.op);
		appendable.append("(");
		if (hasFormalFields) {
			appendable.append(tupleName)
		} else {
			compileNewTuple(appendable, arguments)
		}
		appendable.append(", ")
		e.locality.internalToJavaExpression(appendable)
		appendable.append(");")
		if (hasFormalFields) {
			var i = 0
			for (a : arguments) {
				if (a.isFormalField) {
					val formalField = a as XVariableDeclaration
					appendable.newLine
					appendable.append(appendable.getName(a) + " = (")
					appendable.append(formalField.type.type)
					appendable.append(") " + tupleName + ".getItem(" + i + ");")
				}
				i++
			}
		}
		return appendable
	}

	private def ITreeAppendable compileNewTuple(ITreeAppendable appendable, EList<XExpression> arguments) {
		appendable.append("new ");
		appendable.append(Tuple)
		appendable.append("(new Object[] {")
		arguments.forEach [ a, i |
			if (i !== 0)
				appendable.append(", ")

			if (a instanceof XBlockExpression) {
				appendable.append(getVarName(a, appendable))
			} else if (!a.isFormalField) {
				a.internalToJavaExpression(appendable)
			} else {
				appendable.append((a as XVariableDeclaration).type.type)
				appendable.append(".class")
			}
		]
		appendable.append("})")
	}

	private def ITreeAppendable precompileVariableDeclarationsForFormalFields(XExpression e,
			ITreeAppendable appendable) {
		val xklaimOps = EcoreUtil2.getAllContentsOfType(e, XklaimAbstractOperation)
		for (o : xklaimOps) {
			o.precompileVariableDeclarationsForFormalFields(appendable)
		}
		appendable
	}

	private def ITreeAppendable precompileVariableDeclarationsForFormalFields(XklaimAbstractOperation e,
		ITreeAppendable appendable) {
		val arguments = e.arguments
		for (a : arguments) {
			if (a.isFormalField) {
				a.internalToJavaStatement(appendable, false)
			}
		}
		appendable
	}
}
