package xklaim.typesystem;

import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.inject.Inject;

import klava.Locality;
import klava.topology.KlavaProcess;
import xklaim.util.XklaimModelUtil;
import xklaim.xklaim.XklaimAbstractOperation;
import xklaim.xklaim.XklaimBlockingRetrieveOperation;
import xklaim.xklaim.XklaimEvalOperation;
import xklaim.xklaim.XklaimInlineProcess;
import xklaim.xklaim.XklaimNodeEnvironmentEntry;
import xklaim.xklaim.XklaimNonBlockingRetrieveOperation;

public class XklaimTypeComputer extends XklaimCustomXbaseTypeComputer {
	@Inject
	private XklaimModelUtil modelUtil;

	@Override
	public void computeTypes(XExpression expression, ITypeComputationState state) {
		if (expression instanceof XklaimEvalOperation exp) {
			_computeTypes(exp, state);
		} else if (expression instanceof XklaimAbstractOperation exp) {
			_computeTypes(exp, state);
		} else if (expression instanceof XklaimInlineProcess exp) {
			_computeTypes(exp, state);
		} else if (expression instanceof XklaimNodeEnvironmentEntry exp) {
			_computeTypes(exp, state);
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
		if (e instanceof XklaimNonBlockingRetrieveOperation) {
			state.acceptActualType(getRawTypeForName(Boolean.TYPE, state));
		} else if (e instanceof XklaimBlockingRetrieveOperation blockOp &&
						blockOp.getTimeout() != null) {
			state.withExpectation(getRawTypeForName(Long.TYPE, state)).computeTypes(blockOp.getTimeout());
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
		if (e instanceof XklaimAbstractOperation xklaimAbstractOperation) {
			for (XExpression a : xklaimAbstractOperation.getArguments()) {
				addLocalToCurrentScope(a, state);
			}
		}
		super.addLocalToCurrentScope(e, state);
	}
}
