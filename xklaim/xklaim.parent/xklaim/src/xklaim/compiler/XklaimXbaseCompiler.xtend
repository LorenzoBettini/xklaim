package xklaim.compiler

import com.google.inject.Inject
import klava.Tuple
import klava.topology.KlavaProcess
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import xklaim.util.XklaimModelUtil
import xklaim.xklaim.XklaimAbstractOperation

class XklaimXbaseCompiler extends XbaseCompiler {

	@Inject extension XklaimModelUtil

	override protected doInternalToJavaStatement(XExpression e, ITreeAppendable appendable, boolean isReferenced) {
		switch (e) {
			XklaimAbstractOperation: {
				compileXklaimOperationAsStatement(e, appendable);
			}
			default:
				super.doInternalToJavaStatement(e, appendable, isReferenced)
		}
	}

	private def ITreeAppendable compileXklaimOperationAsStatement(XklaimAbstractOperation e,
		ITreeAppendable appendable) {
		val arguments = e.arguments
		val formalFields = e.formalFields
		val hasFormalFields = !formalFields.empty

		var String tupleName
		if (hasFormalFields) {
			tupleName = appendable.declareSyntheticVariable(e, "_Tuple")
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
		appendable.newLine
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
					appendable.append(formalField.type.type)
					appendable.append(" " + formalField.name + " = (")
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
}
