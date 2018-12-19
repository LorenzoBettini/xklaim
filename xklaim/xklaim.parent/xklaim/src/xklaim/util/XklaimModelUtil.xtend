package xklaim.util

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import xklaim.xklaim.XklaimAbstractOperation

class XklaimModelUtil {

	def getFormalFields(XklaimAbstractOperation o) {
		return o.arguments.filter[isFormalField].toList
	}

	def isFormalField(XExpression e) {
		return e instanceof XVariableDeclaration
	}
}