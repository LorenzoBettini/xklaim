package xklaim.typesystem;

import java.util.List;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.XBasicForLoopExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.annotations.typesystem.XbaseWithAnnotationsTypeComputer;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationResult;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;

/**
 * Since tuple operation have formal fields, that is, variable declarations, we
 * must customize the type computation of statements like if, while, for adding
 * such variables in the scope of the branch instructions.
 * 
 * @author Lorenzo Bettini
 *
 */
public class XklaimCustomXbaseTypeComputer extends XbaseWithAnnotationsTypeComputer {

	@Override
	protected void _computeTypes(XIfExpression object, ITypeComputationState state) {
		ITypeComputationState conditionExpectation = state.withExpectation(getRawTypeForName(Boolean.TYPE, state));
		XExpression condition = object.getIf();
		conditionExpectation.computeTypes(condition);
		
		// customized for Xklaim
		addFormalFieldsToCurrentScope(condition, state);
		
		// TODO then expression may influence the expected type of else and vice versa
		XExpression thenExpression = getThen(object);
		ITypeComputationState thenState = reassignCheckedType(condition, thenExpression, state);
		ITypeComputationResult thenResult = thenState.computeTypes(thenExpression);
		XExpression elseExpression = getElse(object);
		if (elseExpression != null) {
			state.computeTypes(elseExpression);
		} else {
			BranchExpressionProcessor processor = new BranchExpressionProcessor(state, object) {
				@Override
				protected String getMessage() {
					return "Missing else branch for conditional expression with primitive type";
				}
			};
			processor.process(thenResult);
			processor.commit();
		}
	}

	@Override
	protected void _computeTypes(XWhileExpression object, ITypeComputationState state) {
		addFormalFieldsToCurrentScope(object.getPredicate(), state);
		super._computeTypes(object, state);
	}

	@Override
	protected void _computeTypes(XBasicForLoopExpression object, ITypeComputationState state) {
		addFormalFieldsToCurrentScope(object.getExpression(), state);
		super._computeTypes(object, state);
	}

	protected void addFormalFieldsToCurrentScope(XExpression e, ITypeComputationState state) {
		List<XVariableDeclaration> variables = EcoreUtil2.getAllContentsOfType(e, XVariableDeclaration.class);
		for (XVariableDeclaration v : variables) {
			addLocalToCurrentScope(v, state);
		}
	}
}
