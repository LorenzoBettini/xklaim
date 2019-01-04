package xklaim.tests

import com.google.inject.Inject
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.util.ReplaceRegion
import org.eclipse.xtext.xbase.imports.ImportOrganizer
import org.junit.Test
import org.junit.runner.RunWith
import xklaim.xklaim.XklaimModel

import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(XklaimInjectorProvider)
class XklaimOrganizeImportsTest {

	@Inject extension ParseHelper<XklaimModel>
	@Inject ImportOrganizer importOrganizer

	def protected assertIsOrganizedTo(CharSequence model, CharSequence expected) {
		val domainModel = parse(model.toString)
		val changes = importOrganizer.getOrganizedImportChanges(domainModel.eResource as XtextResource)
		val builder = new StringBuilder(model)
		val sortedChanges = changes.sortBy[offset]
		var ReplaceRegion lastChange = null
		for (it : sortedChanges) {
			if (lastChange !== null && lastChange.endOffset > offset)
				fail("Overlapping text edits: " + lastChange + ' and ' + it)
			lastChange = it
		}
		for (it : sortedChanges.reverse)
			builder.replace(offset, offset + length, text)
		assertEquals(expected.toString, builder.toString)
	}

	@Test def testSimple() {
		'''
			package foo 
			
			proc Foo(java.io.Serializable s) {
			}
		'''.assertIsOrganizedTo('''
			package foo
			
			import java.io.Serializable
			
			proc Foo(Serializable s) {
			}
		''')
	}

	@Test def testDefaultPackage() {
		'''
			proc Foo(java.io.Serializable s) {
			}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable
			
			proc Foo(Serializable s) {
			}
		''')
	}

	@Test def testDefaultPackageLeadingWhitespace() {
		'''
			«»
			   	
			proc Foo(java.io.Serializable s) {
			}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable
			
			proc Foo(Serializable s) {
			}
		''')
	}

	@Test def testDefaultPackageWithJavaDoc() {
		'''
			/**
			 * some doc
			 */
			proc Foo(java.io.Serializable s) {
			}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable
			
			/**
			 * some doc
			 */
			proc Foo(Serializable s) {
			}
		''')
	}

}
