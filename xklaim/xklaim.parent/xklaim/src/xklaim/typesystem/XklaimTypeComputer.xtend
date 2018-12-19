package xklaim.typesystem

import org.eclipse.xtext.xbase.annotations.typesystem.XbaseWithAnnotationsTypeComputer
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState
import xklaim.xklaim.XklaimOutOperation

class XklaimTypeComputer extends XbaseWithAnnotationsTypeComputer {

	override computeTypes(XExpression expression, ITypeComputationState state) {
		switch (expression) {
			XklaimOutOperation: _computeTypes(expression, state)
			default: super.computeTypes(expression, state)
		}
	}

	def void _computeTypes(XklaimOutOperation e, ITypeComputationState state) {
		state.acceptActualType(getPrimitiveVoid(state))
	}
}
