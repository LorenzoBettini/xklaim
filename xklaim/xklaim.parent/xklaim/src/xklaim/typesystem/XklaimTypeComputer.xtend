package xklaim.typesystem

import klava.Locality
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.annotations.typesystem.XbaseWithAnnotationsTypeComputer
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState
import xklaim.xklaim.XklaimAbstractOperation
import com.google.inject.Inject
import xklaim.util.XklaimModelUtil

class XklaimTypeComputer extends XbaseWithAnnotationsTypeComputer {

	@Inject extension XklaimModelUtil

	override computeTypes(XExpression expression, ITypeComputationState state) {
		switch (expression) {
			XklaimAbstractOperation: _computeTypes(expression, state)
			default: super.computeTypes(expression, state)
		}
	}

	def void _computeTypes(XklaimAbstractOperation e, ITypeComputationState state) {
		state.withExpectation(getRawTypeForName(Locality, state)).computeTypes(e.locality)
		for (a : e.arguments) {
			if (a.isFormalField) {
				state.withoutExpectation.computeTypes(a)
			} else {
				state.withNonVoidExpectation.computeTypes(a)
			}
		}
		state.acceptActualType(getPrimitiveVoid(state))
	}

	override protected addLocalToCurrentScope(XExpression e, ITypeComputationState state) {
		if (e instanceof XklaimAbstractOperation) {
			for (a : e.arguments) {
				addLocalToCurrentScope(a, state)
			}
		}
		super.addLocalToCurrentScope(e, state)
	}

}
