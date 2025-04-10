package xklaim.util;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.exists;

import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;

import xklaim.xklaim.XklaimAbstractOperation;

public class XklaimModelUtil {
	public boolean containsFormalFields(final XklaimAbstractOperation o) {
		return exists(o.getArguments(), this::isFormalField);
	}

	public boolean isFormalField(final XExpression e) {
		return e instanceof XVariableDeclaration;
	}

}
