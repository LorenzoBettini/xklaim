package xklaim.compiler

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import xklaim.xklaim.XklaimOutOperation

class XklaimXbaseCompiler extends XbaseCompiler {
	override protected doInternalToJavaStatement(XExpression e, ITreeAppendable appendable, boolean isReferenced) {
		switch (e) {
			XklaimOutOperation: {
				val argsString = e.arguments.map[toString].join(", ")
				appendable.append('''out(«argsString»);''');
			}
			default:
				super.doInternalToJavaStatement(e, appendable, isReferenced)
		}
	}
}