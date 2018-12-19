package xklaim.util

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import xklaim.xklaim.XklaimAbstractOperation

class XklaimModelUtil {

	def containsFormalFields(XklaimAbstractOperation o) {
		return o.arguments.exists[isFormalField]
	}

	def isFormalField(XExpression e) {
		return e instanceof XVariableDeclaration
	}
}