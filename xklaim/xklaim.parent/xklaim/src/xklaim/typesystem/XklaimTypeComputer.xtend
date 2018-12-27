package xklaim.typesystem

import com.google.inject.Inject
import klava.Locality
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState
import xklaim.util.XklaimModelUtil
import xklaim.xklaim.XklaimAbstractOperation
import xklaim.xklaim.XklaimInlineProcess
import klava.topology.KlavaProcess

class XklaimTypeComputer extends XklaimCustomXbaseTypeComputer {

	@Inject extension XklaimModelUtil

	override computeTypes(XExpression expression, ITypeComputationState state) {
		switch (expression) {
			XklaimAbstractOperation: _computeTypes(expression, state)
			XklaimInlineProcess: _computeTypes(expression, state)
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
		if (e.isNonBlockingOperation)
			state.acceptActualType(getRawTypeForName(Boolean.TYPE, state))
		else
			state.acceptActualType(getPrimitiveVoid(state))
	}

	def void _computeTypes(XklaimInlineProcess e, ITypeComputationState state) {
		state.withoutExpectation.computeTypes(e.body)
		state.acceptActualType(getRawTypeForName(KlavaProcess, state))
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
