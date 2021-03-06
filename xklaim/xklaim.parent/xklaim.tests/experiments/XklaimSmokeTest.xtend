package xklaim.tests

import org.eclipse.xtext.testing.smoketest.ProcessedBy
import org.eclipse.xtext.testing.smoketest.XtextSmokeTestRunner
import org.eclipse.xtext.xbase.testing.typesystem.TypeSystemSmokeTester
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

@RunWith(XtextSmokeTestRunner)
@ProcessedBy(
	value=TypeSystemSmokeTester,
	processInParallel=true
)
@SuiteClasses(
	XklaimValidatorTest
)
class XklaimSmokeTest {
	
}