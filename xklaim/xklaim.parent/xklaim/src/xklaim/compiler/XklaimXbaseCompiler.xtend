package xklaim.compiler

import klava.Tuple
import klava.topology.KlavaProcess
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import xklaim.xklaim.XklaimOutOperation

class XklaimXbaseCompiler extends XbaseCompiler {

	override protected doInternalToJavaStatement(XExpression e, ITreeAppendable appendable, boolean isReferenced) {
		switch (e) {
			XklaimOutOperation: {
				val arguments = e.arguments
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
						appendable.declareVariable(mappedThis, mappedThis.simpleName+".this")
						a.internalToJavaStatement(appendable, false)
						appendable.closeScope
						appendable.decreaseIndentation.newLine
						appendable.append("}")
						appendable.decreaseIndentation.newLine
						appendable.append("};")
					} else {
						a.internalToJavaStatement(appendable, true)
						appendable.newLine
					}
				}
				e.locality.internalToJavaStatement(appendable, true)
				appendable.append("out(new ");
				appendable.append(Tuple)
				appendable.append("(new Object[] {")
				arguments.forEach[a, i|
					if (i !== 0)
						appendable.append(", ")
					if (a instanceof XBlockExpression) {
						appendable.append(getVarName(a, appendable))
					} else {
						a.internalToJavaExpression(appendable)
					}
				]
				appendable.append("}), ")
				e.locality.internalToJavaExpression(appendable)
				appendable.append(");");
			}
			default:
				super.doInternalToJavaStatement(e, appendable, isReferenced)
		}
	}
}