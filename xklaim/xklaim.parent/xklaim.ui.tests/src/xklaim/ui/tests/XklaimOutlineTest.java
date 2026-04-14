package xklaim.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.testing.AbstractOutlineTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import xklaim.ui.internal.XklaimActivator;

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(XklaimUiInjectorProvider.class)
@SuppressWarnings("all")
public class XklaimOutlineTest extends AbstractOutlineTest {
	@Override
	protected String getEditorId() {
		return XklaimActivator.XKLAIM_XKLAIM;
	}

	/**
	 * We must make sure to get rid of "\r" because in Windows Java text blocks do
	 * not contain "\r" and the comparison would fail.
	 */
	@Override
	protected String outlineStringRepresentation(IOutlineNode node) {
		return super.outlineStringRepresentation(node).replace("\r", "");
	}

	@Test
	public void testEmptyOutline() throws Exception {
		assertAllLabels("\t\t\t", "");
	}

	@Test
	public void testOutline() throws Exception {
		this.assertAllLabels("""
				package mydsl

				proc MyProc(String s) {

				}
				net HelloNet physical "localhost:9999" {
					node Hello1 {
						println("Hello World!")
					}
					node Hello2 {
						println("Hello World!")
					}
				}
				""", """
				mydsl
				  MyProc
				  HelloNet
				    Hello1
				    Hello2
				""");
	}
}
