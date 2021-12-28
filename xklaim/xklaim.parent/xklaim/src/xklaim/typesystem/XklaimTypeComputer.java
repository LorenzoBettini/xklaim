package xklaim.typesystem;

import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.inject.Inject;

import klava.Locality;
import klava.topology.KlavaProcess;
import xklaim.util.XklaimModelUtil;
import xklaim.xklaim.XklaimAbstractOperation;
import xklaim.xklaim.XklaimEvalOperation;
import xklaim.xklaim.XklaimInlineProcess;
import xklaim.xklaim.XklaimNodeEnvironmentEntry;

public class XklaimTypeComputer extends XklaimCustomXbaseTypeComputer {
	@Inject
	private XklaimModelUtil modelUtil;

	@Override
	public void computeTypes(XExpression expression, ITypeComputationState state) {
		if (expression instanceof XklaimEvalOperation) {
			_computeTypes((XklaimEvalOperation) expression, state);
		} else if (expression instanceof XklaimAbstractOperation) {
			_computeTypes((XklaimAbstractOperation) expression, state);
		} else if (expression instanceof XklaimInlineProcess) {
			_computeTypes((XklaimInlineProcess) expression, state);
		} else if (expression instanceof XklaimNodeEnvironmentEntry) {
			_computeTypes((XklaimNodeEnvironmentEntry) expression, state);
		} else {
			super.computeTypes(expression, state);
		}
	}

	public void _computeTypes(XklaimEvalOperation e, ITypeComputationState state) {
		state.withExpectation(getLocalityType(state))
			.computeTypes(e.getLocality());
		for (XExpression a : e.getArguments()) {
			state.withoutExpectation().computeTypes(a);
		}
		state.acceptActualType(getPrimitiveVoid(state));
	}

	private LightweightTypeReference getLocalityType(ITypeComputationState state) {
		return getRawTypeForName(Locality.class, state);
	}

	public void _computeTypes(XklaimAbstractOperation e, ITypeComputationState state) {
		state.withExpectation(getLocalityType(state))
			.computeTypes(e.getLocality());
		for (XExpression a : e.getArguments()) {
			if (modelUtil.isFormalField(a)) {
				state.withoutExpectation().computeTypes(a);
			} else {
				state.withNonVoidExpectation().computeTypes(a);
			}
		}
		if (modelUtil.isNonBlockingOperation(e)) {
			state.acceptActualType(getRawTypeForName(Boolean.TYPE, state));
		} else {
			state.acceptActualType(getPrimitiveVoid(state));
		}
	}

	public void _computeTypes(XklaimInlineProcess e, ITypeComputationState state) {
		state.withoutExpectation().computeTypes(e.getBody());
		state.acceptActualType(getRawTypeForName(KlavaProcess.class, state));
	}

	public void _computeTypes(XklaimNodeEnvironmentEntry e, ITypeComputationState state) {
		state.withExpectation(getLocalityType(state))
			.computeTypes(e.getValue());
		state.acceptActualType(getPrimitiveVoid(state));
	}

	@Override
	protected void addLocalToCurrentScope(XExpression e, ITypeComputationState state) {
		if (e instanceof XklaimAbstractOperation) {
			for (XExpression a : ((XklaimAbstractOperation) e).getArguments()) {
				addLocalToCurrentScope(a, state);
			}
		}
		super.addLocalToCurrentScope(e, state);
	}
}
