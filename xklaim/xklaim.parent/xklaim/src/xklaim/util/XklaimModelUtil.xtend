package xklaim.util

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XVariableDeclaration
import xklaim.xklaim.XklaimAbstractOperation
import xklaim.xklaim.XklaimNonBlockingInOperation
import xklaim.xklaim.XklaimNonBlockingReadOperation

class XklaimModelUtil {

	def containsFormalFields(XklaimAbstractOperation o) {
		return o.arguments.exists[isFormalField]
	}

	def isFormalField(XExpression e) {
		return e instanceof XVariableDeclaration
	}

	def isNonBlockingOperation(XklaimAbstractOperation o) {
		return
			o instanceof XklaimNonBlockingInOperation ||
			o instanceof XklaimNonBlockingReadOperation
	}
}