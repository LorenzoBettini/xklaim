/*
 * generated by Xtext 2.16.0
 */
package xklaim.tests

import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith
import xklaim.xklaim.XklaimModel

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(XklaimInjectorProvider)
class XklaimValidatorTest {
	@Inject extension ParseHelper<XklaimModel> parseHelper
	@Inject extension ValidationTestHelper validationTestHelper

	@Test
	def void testValidSelfLocality() {
		'''
			proc TestProc() {
				out("hi")@self
			}
		'''.parse.assertNoIssues
	}

	@Test
	def void testValidLocality() {
		'''
			import klava.Locality
			import klava.PhysicalLocality
			import klava.LogicalLocality
			
			proc TestProc(Locality l1, PhysicalLocality l2, LogicalLocality l3) {
				out("hi")@l1
				out("hi")@l2
				out("hi")@l3
			}
		'''.parse.assertNoIssues
	}

	@Test
	def void testInvalidLocality() {
		'''
			proc TestProc(int i) {
				out("hi")@i
			}
		'''.parse.assertErrorsAsStrings("Type mismatch: cannot convert from int to Locality")
	}

	@Test
	def void testCanAccessFormalTupleFields() {
		'''
		package foo
		proc TestProcess(String s) {
			in(var Integer i, s)@self
			println(i)
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testFormalTupleFieldMustHaveAType() {
		'''
		package foo
		proc TestProcess(String s) {
			in(var i, s)@self
		}
		'''.parse.assertErrorsAsStrings("Type must be specified")
	}

	@Test
	def void testFormalTupleFieldCannotHaveInitialization() {
		'''
		package foo
		proc TestProcess(String s) {
			in(var Integer i = 10, s)@self
		}
		'''.parse.assertErrorsAsStrings("Formal field must not be initialized")
	}

	@Test
	def void testCanUseValForFormalTupleFields() {
		'''
		package foo
		proc TestProcess(String s) {
			in(val Integer i, s)@self
			println(i)
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testNonBlockingInOperationAsStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			in_nb(val Integer i, s)@self
			println(i)
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testNonBlockingInOperationAsBooleanExpressionInIfStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(val Integer i, s)@self && !in_nb(val String l)@self) {
				println(l + i)
			} else {
				println(l + i)
			}
			if (read_nb(val Integer i, s)@self) {
				println(i)
			} else {
				println(i)
			}
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testNonBlockingInOperationAsBooleanExpressionInWhileStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			while (in_nb(val Integer i, s)@self) {
				println(i)
			}
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testNonBlockingInOperationAsBooleanExpressionInDoWhileStatement() {
		'''
		package foo
		proc TestProcess(String s) {
			do {
				println()
			} while (in_nb(val Integer i, s)@self)
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testFormalFieldsInNonBlockingInOperationAsBooleanExpressionAreNotVisibleOutside() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(val Integer i, s)@self && !in_nb(val String l)@self) {
				
			}
			println(l + i)
		}
		'''.parse.assertErrorsAsStrings(
			'''
			The method or field i is undefined
			The method or field l is undefined
			'''
		)
	}

	@Test
	def void testDuplicateFormalFieldsInXklaimOperationInBooleanExpression() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(var Integer i, s)@self && !in_nb(var String i)@self) {
				
			}
		}
		'''.parse.assertErrorsAsStrings(
			'''
			Duplicate local variable i
			'''
		)
	}

	@Test
	def void testDuplicateFormalFieldsInXklaimOperationAfterIf() {
		'''
		package foo
		proc TestProcess(String s) {
			if (in_nb(var Integer i, s)@self) {
				
			}
			in(var String i)@self
		}
		'''.parse.assertNoErrors
	}

	@Test
	def void testDuplicateFormalFieldsInXklaimOperation() {
		'''
		package foo
		proc TestProcess(String s) {
			in(var Integer i, s)@self
			in(var String i)@self
		}
		'''.parse.assertErrorsAsStrings(
			'''
			Duplicate local variable i
			'''
		)
	}

	@Test
	def void testInlineProcInOperations() {
		'''
		package foo
		
		proc TestProcess(String s) {
			var nonFinalVar = "a"
			val finalVar = "b"
			out(proc {
				var myLocalVar = "c"
				println(finalVar + nonFinalVar)
				println(s + finalVar + nonFinalVar + myLocalVar + self)
			},
			proc println(finalVar + nonFinalVar),
			s + finalVar + nonFinalVar)@self
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testInlineProcInVariable() {
		'''
		package foo
		
		proc TestProcess(String s) {
			var nonFinalVar = "a"
			val finalVar = "b"
			val P = proc {
				var myLocalVar = "c"
				println(finalVar + nonFinalVar)
				println(s + finalVar + nonFinalVar + myLocalVar + self)
			}
			out(P)@self
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testValidEvalOperation() {
		'''
		package foo
		
		proc P(String s) {
			
		}
		
		proc TestProcess(String s) {
			eval(new P("test"))@self
			eval(proc println("test"))@self
		}
		'''.parse.assertNoIssues
	}

	@Test
	def void testEvalOperationWithoutExplicitInnerProc() {
		'''
		package foo
		
		proc P(String s) {
			
		}
		
		proc TestProcess(String s) {
			eval("test")@self
		}
		'''.parse.assertNoIssues
	}

	def private assertErrorsAsStrings(EObject o, CharSequence expected) {
		expected.toString.trim.assertEquals(
			o.validate.filter[severity == Severity.ERROR].map[message].sort.join(System.lineSeparator))
	}
}
