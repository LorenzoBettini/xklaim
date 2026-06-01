package xklaim.validation;

import static org.eclipse.xtext.validation.IssueCodes.COPY_JAVA_PROBLEMS;

import org.eclipse.xtext.preferences.PreferenceKey;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.validation.SeverityConverter;

/**
 * Enables diagnostics copied from Java problems in generated code.
 */
public class XklaimConfigurableIssueCodes extends XklaimConfigurableIssueCodesProvider {

	@Override
	protected void initialize(IAcceptor<PreferenceKey> acceptor) {
		super.initialize(acceptor);
		acceptor.accept(create(COPY_JAVA_PROBLEMS, SeverityConverter.SEVERITY_ERROR));
	}
}
