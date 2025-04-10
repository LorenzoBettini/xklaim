package xklaim.compiler;

import static org.eclipse.xtext.EcoreUtil2.getAllContentsOfType;
import static org.eclipse.xtext.xbase.lib.IterableExtensions.forEach;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;

import com.google.inject.Inject;

import klava.Tuple;
import klava.topology.KlavaProcess;
import xklaim.util.XklaimModelUtil;
import xklaim.xklaim.XklaimAbstractOperation;
import xklaim.xklaim.XklaimBlockingRetrieveOperation;
import xklaim.xklaim.XklaimEvalOperation;
import xklaim.xklaim.XklaimInlineProcess;
import xklaim.xklaim.XklaimNodeEnvironmentEntry;

public class XklaimXbaseCompiler extends XbaseCompiler {
	@Inject
	private XklaimModelUtil xklaimModelUtil;

	@Inject
	private IJvmModelAssociations jvmModelAssociations;

	@Override
	protected void doInternalToJavaStatement(final XExpression e, final ITreeAppendable appendable,
			final boolean isReferenced) {
		if (e instanceof XklaimEvalOperation ev) {
			compileXklaimEvalAsStatement(ev, appendable, isReferenced);
		} else if (e instanceof XklaimAbstractOperation ab) {
			compileXklaimOperationAsStatement(ab, appendable, isReferenced);
		} else if (e instanceof XklaimInlineProcess inp) {
			compileInnerProcess(appendable, inp);
		} else if (e instanceof XklaimNodeEnvironmentEntry nenv) {
			compileNodeEnvironmentEntry(nenv, appendable);
		} else {
			super.doInternalToJavaStatement(e, appendable, isReferenced);
		}
	}

	@Override
	protected void internalToConvertedExpression(final XExpression e, final ITreeAppendable appendable) {
		if (e instanceof XklaimAbstractOperation || e instanceof XklaimInlineProcess) {
			appendable.append(getVarName(e, appendable));
		} else {
			super.internalToConvertedExpression(e, appendable);
		}
	}

	@Override
	protected boolean internalCanCompileToJavaExpression(final XExpression expression,
			final ITreeAppendable appendable) {
		if (expression instanceof XklaimAbstractOperation) {
			return false;
		}
		return super.internalCanCompileToJavaExpression(expression, appendable);
	}

	@Override
	protected void _toJavaStatement(final XIfExpression e, final ITreeAppendable b, final boolean isReferenced) {
		precompileVariableDeclarationsForFormalFields(e.getIf(), b);
		super._toJavaStatement(e, b, isReferenced);
	}

	@Override
	protected void _toJavaStatement(final XWhileExpression e, final ITreeAppendable b, final boolean isReferenced) {
		precompileVariableDeclarationsForFormalFields(e.getPredicate(), b);
		super._toJavaStatement(e, b, isReferenced);
	}

	private ITreeAppendable compileXklaimOperationAsStatement(final XklaimAbstractOperation e,
			final ITreeAppendable appendable, final boolean isReferenced) {
		final var arguments = e.getArguments();
		final var hasFormalFields = xklaimModelUtil.containsFormalFields(e);
		String tupleName = preprocessTupleFields(arguments, hasFormalFields, appendable);
		internalToJavaStatement(e.getLocality(), appendable, true);
		XExpression timeout = null;
		if (e instanceof XklaimBlockingRetrieveOperation blockOp && blockOp.getTimeout() != null) {
			timeout = blockOp.getTimeout();
			internalToJavaStatement(timeout, appendable, true);
		}
		if (isReferenced) {
			final var opVar = appendable.declareSyntheticVariable(e, "_" + e.getOp());
			appendable.newLine();
			appendable.append(Boolean.TYPE);
			appendable.append(" " + opVar + " = ");
		} else {
			appendable.newLine();
		}
		appendable.append(e.getOp() + (timeout != null ? "_t" : ""));
		appendable.append("(");
		if (hasFormalFields) {
			appendable.append(tupleName);
		} else {
			compileNewTuple(appendable, arguments);
		}
		appendable.append(", ");
		internalToJavaExpression(e.getLocality(), appendable);
		if (timeout != null) {
			appendable.append(", ");
			internalToJavaExpression(timeout, appendable);
		}
		appendable.append(");");
		if (hasFormalFields) {
			assignValuesToFormalFields(arguments, tupleName, appendable);
		}
		return appendable;
	}

	/**
	 * If the returned string is not null it means that a variable for the tuple
	 * has been created because it contains formal fields; later, the formal fields
	 * must be given the values retrieved through pattern matching.
	 * 
	 * See {@link #assignValuesToFormalFields(List, String, ITreeAppendable)}
	 * 
	 * @param arguments
	 * @param hasFormalFields
	 * @param appendable
	 * @return
	 */
	private String preprocessTupleFields(final List<XExpression> arguments, final boolean hasFormalFields,
			final ITreeAppendable appendable) {
		String tupleName = null;
		if (hasFormalFields) {
			for (final XExpression a : arguments) {
				// the variable declaration has already been generated when the operation is
				// used in an if or while so we must use hasName
				if (xklaimModelUtil.isFormalField(a) && !appendable.hasName(a)) {
					compileVariableForFormalField(a, appendable);
				}
			}
			tupleName = appendable.declareSyntheticVariable(new Object(), "_Tuple");
			appendable.newLine();
			appendable.append(Tuple.class);
			appendable.append(" " + tupleName + " = ");
			compileNewTuple(appendable, arguments);
			appendable.append(";");
		}
		for (final XExpression a : arguments) {
			if (!xklaimModelUtil.isFormalField(a)) {
				internalToJavaStatement(a, appendable, true);
			}
		}
		return tupleName;
	}

	/**
	 * Generates the assignments to give values to formal fields after pattern matching.
	 * 
	 * @param arguments
	 * @param tupleName
	 * @param appendable
	 */
	private void assignValuesToFormalFields(final List<XExpression> arguments, String tupleName,
			final ITreeAppendable appendable) {
		var i = 0;
		for (final XExpression a : arguments) {
			if (xklaimModelUtil.isFormalField(a)) {
				final var formalField = (XVariableDeclaration) a;
				appendable.newLine();
				// don't use the var name directly since the original declaration
				// might have been renamed to avoid duplication in generated code
				// e.g., for blocks of the shape in(var String s)@self ; in(var Integer s)@self
				// which are legal in Xklaim
				appendable.append(appendable.getName(a) + " = (");
				appendable.append(formalField.getType().getType());
				appendable.append(") " + tupleName + ".getItem(" + Integer.valueOf(i) + ");");
			}
			i++;
		}
	}

	private void compileVariableForFormalField(final XExpression exp, final ITreeAppendable appendable) {
		final var varDecl = (XVariableDeclaration) exp;
		if (varDecl.isWriteable()) {
			internalToJavaStatement(varDecl, appendable, false);
		} else {
			// for final variables don't generate the assignment to the
			// default literal: they will be initialized later after the matching
			// and this is legal in Java (as long as the final variable hasn't been
			// already initialized)
			appendable.newLine();
			appendVariableTypeAndName(varDecl, appendable);
			appendable.append(";");
		}
	}

	private ITreeAppendable compileXklaimEvalAsStatement(final XklaimEvalOperation e, final ITreeAppendable appendable,
			final boolean isReferenced) {
		final var arguments = e.getArguments();
		for (final XExpression a : arguments) {
			if (getLightweightType(a).isSubtypeOf(KlavaProcess.class)) {
				internalToJavaStatement(a, appendable, true);
			} else {
				final var procVarName = declareSyntheticVariableForInnerProcess(a, appendable);
				compileAsInnerProcess(a, appendable, procVarName);
			}
		}
		internalToJavaStatement(e.getLocality(), appendable, true);
		if (isReferenced) {
			final var opVar = appendable.declareSyntheticVariable(e, "_" + e.getOp());
			appendable.newLine();
			appendable.append(Boolean.TYPE);
			appendable.append(" " + opVar + " = ");
		}
		for (final XExpression a : arguments) {
			appendable.newLine();
			appendable.append(e.getOp());
			appendable.append("(");
			// we cannot assume it has already been compiled with a synthetic variable
			// e.g., referring directly to a variable of type process
			if (appendable.hasName(a)) {
				appendable.append(getVarName(a, appendable));
			} else {
				internalToJavaExpression(a, appendable);
			}
			appendable.append(", ");
			internalToJavaExpression(e.getLocality(), appendable);
			appendable.append(");");
		}
		return appendable;
	}

	private ITreeAppendable compileInnerProcess(final ITreeAppendable appendable, final XklaimInlineProcess proc) {
		final var procVarName = declareSyntheticVariableForInnerProcess(proc, appendable);
		return compileAsInnerProcess(proc.getBody(), appendable, procVarName);
	}

	private ITreeAppendable compileNodeEnvironmentEntry(final XklaimNodeEnvironmentEntry e,
			final ITreeAppendable appendable) {
		internalToJavaStatement(e.getValue(), appendable, true);
		appendable.newLine();
		appendable.append("addToEnvironment(");
		appendable.append(e.getKey());
		appendable.append(", getPhysical(");
		internalToJavaExpression(e.getValue(), appendable);
		appendable.append("));");
		return appendable;
	}

	private String declareSyntheticVariableForInnerProcess(final XExpression e, final ITreeAppendable appendable) {
		return appendable.declareSyntheticVariable(e, "_Proc");
	}

	/**
	 * Creates an anonymous inner class for the expression representing the inner
	 * process body; references to variables in the enclosing scope are turned into
	 * fields of the inner process so that their values are closed.
	 */
	private ITreeAppendable compileAsInnerProcess(final XExpression body, final ITreeAppendable appendable,
			final String procVarName) {
		appendable.newLine();
		appendable.append(KlavaProcess.class);
		appendable.append(" " + procVarName + " = new ");
		appendable.append(KlavaProcess.class);
		appendable.append("() {");
		appendable.increaseIndentation().newLine();
		var eclosingScopeVars = EcoreUtil2.getAllContentsOfType(body, XAbstractFeatureCall.class).stream()
				.map(it -> it.getFeature())
				.filter(it -> {
					// original process parameters are translated into fields
					if (it instanceof JvmField)
						return jvmModelAssociations.getSourceElements(it).stream()
								.anyMatch(JvmFormalParameter.class::isInstance);
					if (it instanceof XVariableDeclaration)
						return appendable.hasName(it); // no name means not in the enclosing scope
					else
						return false;
				})
				.collect(Collectors.toCollection(LinkedHashSet::new));
		for (final JvmIdentifiableElement v : eclosingScopeVars) {
			generateTypeAndNameFromEnclosingReference(v, appendable);
			appendable.append(";");
			appendable.newLine();
		}
		appendable.append("private ");
		appendable.append(KlavaProcess.class);
		appendable.append(" _initFields(");
		forEach(eclosingScopeVars, (var v, var i) -> {
			if (i.intValue() != 0) {
				appendable.append(", ");
			}
			generateTypeAndNameFromEnclosingReference(v, appendable);
		});
		appendable.append(") {");
		appendable.increaseIndentation();
		for (final JvmIdentifiableElement v : eclosingScopeVars) {
			appendable.newLine();
			final var varName = getEnclosingScopeVarName(appendable, v);
			appendable.append("this." + varName + " = " + varName);
			appendable.append(";");
		}
		appendable.newLine();
		appendable.append("return this;");
		appendable.decreaseIndentation().newLine();
		appendable.append("}");
		appendable.newLine();
		appendable.append("@Override public void executeProcess() {");
		appendable.increaseIndentation();
		/*
		 * NO: we manually "close" the inner process by passing
		 * arguments for referred local variables and process parameters
		// we need to reassign the mapping for this since we generate an
		// anonymous innerclass, instead of
		// this.field
		// we must generate
		// CurrentType.this.field
		appendable.openScope
		val mappedThis = appendable.getObject("this") as JvmDeclaredType
		appendable.declareVariable(mappedThis, mappedThis.simpleName + ".this")
		*/
		internalToJavaStatement(body, appendable, false);
		// appendable.closeScope
		appendable.decreaseIndentation().newLine();
		appendable.append("}");
		appendable.decreaseIndentation().newLine();
		appendable.append("}._initFields(");
		forEach(eclosingScopeVars,  (var v, var i) -> {
			if (i.intValue() != 0) {
				appendable.append(", ");
			}
			appendable.append(getEnclosingScopeVarName(appendable, v));
		});
		appendable.append(");");
		return appendable;
	}

	private ITreeAppendable generateTypeAndNameFromEnclosingReference(final JvmIdentifiableElement v,
			final ITreeAppendable appendable) {
		if (v instanceof XVariableDeclaration varDecl) {
			if (varDecl.getType() != null) {
				serialize(varDecl.getType(), v, appendable);
			} else {
				var type = getLightweightType(varDecl.getRight());
				if (type.isAny()) {
					type = getTypeForVariableDeclaration(varDecl.getRight());
				}
				appendable.append(type);
			}
			appendable.append(" " + appendable.getName(v));
		} else {
			final var p = (JvmField) v;
			serialize(p.getType(), p, appendable);
			appendable.append(" " + p.getSimpleName());
		}
		return appendable;
	}

	private String getEnclosingScopeVarName(final ITreeAppendable appendable, final JvmIdentifiableElement e) {
		if (e instanceof XVariableDeclaration) {
			return appendable.getName(e);
		}
		return e.getSimpleName();
	}

	private ITreeAppendable compileNewTuple(final ITreeAppendable appendable, final List<XExpression> arguments) {
		appendable.append("new ");
		appendable.append(Tuple.class);
		appendable.append("(new Object[] {");
		forEach(arguments, (var a, var i) -> {
			if (i.intValue() != 0) {
				appendable.append(", ");
			}
			if (!xklaimModelUtil.isFormalField(a)) {
				internalToJavaExpression(a, appendable);
			} else {
				appendable.append(((XVariableDeclaration) a).getType().getType());
				appendable.append(".class");
			}
		});
		appendable.append("})");
		return appendable;
	}

	private ITreeAppendable precompileVariableDeclarationsForFormalFields(final XExpression e,
		final ITreeAppendable appendable) {
		final var xklaimOps = getAllContentsOfType(e, XklaimAbstractOperation.class);
		for (final XklaimAbstractOperation o : xklaimOps) {
			precompileVariableDeclarationsForFormalFields(o, appendable);
		}
		return appendable;
	}

	private ITreeAppendable precompileVariableDeclarationsForFormalFields(final XklaimAbstractOperation e,
		final ITreeAppendable appendable) {
		final var arguments = e.getArguments();
		for (final XExpression a : arguments) {
			if (xklaimModelUtil.isFormalField(a)) {
				compileVariableForFormalField(a, appendable);
			}
		}
		return appendable;
	}
}
