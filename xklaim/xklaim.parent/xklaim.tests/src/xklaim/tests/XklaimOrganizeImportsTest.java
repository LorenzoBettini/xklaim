package xklaim.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.ReplaceRegion;
import org.eclipse.xtext.xbase.imports.ImportOrganizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import xklaim.xklaim.XklaimModel;

@RunWith(XtextRunner.class)
@InjectWith(XklaimInjectorProvider.class)
public class XklaimOrganizeImportsTest {

	@Inject
	ParseHelper<XklaimModel> parseHelper;
	@Inject
	ImportOrganizer importOrganizer;

	protected void assertIsOrganizedTo(CharSequence model, CharSequence expected) throws Exception {
		var domainModel = parseHelper.parse(model.toString());
		var changes = importOrganizer.getOrganizedImportChanges((XtextResource) domainModel.eResource());
		var builder = new StringBuilder(model);
		List<ReplaceRegion> sortedChanges = changes.stream()
				.sorted(Comparator.comparingInt(ReplaceRegion::getOffset))
				.toList();
		ReplaceRegion lastChange = null;
		for (var it : sortedChanges) {
			if (lastChange != null && lastChange.getEndOffset() > it.getOffset())
				Assert.fail("Overlapping text edits: " + lastChange + " and " + it);
			lastChange = it;
		}
		var reversed = new ArrayList<>(sortedChanges);
		Collections.reverse(reversed);
		for (var it : reversed)
			builder.replace(it.getOffset(), it.getOffset() + it.getLength(), it.getText());
		Assert.assertEquals(expected.toString().replace("\r", ""), builder.toString().replace("\r", ""));
	}

	@Test
	public void testSimple() throws Exception {
		assertIsOrganizedTo(
				"package foo \n\nproc Foo(java.io.Serializable s) {\n}\n",
				"""
				package foo
				
				import java.io.Serializable
				
				proc Foo(Serializable s) {
				}
				""");
	}

	@Test
	public void testDefaultPackage() throws Exception {
		assertIsOrganizedTo("""
				proc Foo(java.io.Serializable s) {
				}
				""",
				"""
				import java.io.Serializable
				
				proc Foo(Serializable s) {
				}
				""");
	}

	@Test
	public void testDefaultPackageLeadingWhitespace() throws Exception {
		assertIsOrganizedTo(
				"\n   \t\nproc Foo(java.io.Serializable s) {\n}\n",
				"""
				import java.io.Serializable
				
				proc Foo(Serializable s) {
				}
				""");
	}

	@Test
	public void testDefaultPackageWithJavaDoc() throws Exception {
		assertIsOrganizedTo("""
				/**
				 * some doc
				 */
				proc Foo(java.io.Serializable s) {
				}
				""",
				"""
				import java.io.Serializable
				
				/**
				 * some doc
				 */
				proc Foo(Serializable s) {
				}
				""");
	}
}
