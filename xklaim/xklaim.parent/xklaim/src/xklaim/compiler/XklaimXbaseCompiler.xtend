package xklaim.compiler

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import xklaim.xklaim.XklaimOutOperation
import klava.Tuple

class XklaimXbaseCompiler extends XbaseCompiler {
	override protected doInternalToJavaStatement(XExpression e, ITreeAppendable appendable, boolean isReferenced) {
		switch (e) {
			XklaimOutOperation: {
				val arguments = e.arguments
				for (a : arguments) {
					a.internalToJavaStatement(appendable, true)
					appendable.newLine
				}
				e.locality.internalToJavaStatement(appendable, true)
				appendable.append("out(new ");
				appendable.append(Tuple)
				appendable.append("(new Object[] {")
				arguments.forEach[a, i|
					if (i !== 0)
						appendable.append(", ")
					a.internalToJavaExpression(appendable)
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