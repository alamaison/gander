package uk.ac.ic.doc.gander;

import static org.junit.Assert.assertTrue;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Print;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * We're keen on using the print statement because it means we can easily run
 * our test cases to make sure they work as we expect.
 */
public final class ScopedPrintNode {

	private ScopedAstNode node;
	private Module module;

	public static ScopedPrintNode findPrintNode(MutableModel model, String moduleName,
			String tag) throws Exception {
		return new ScopedPrintNode(model, moduleName, tag);
	}

	public Namespace getScope() {
		return node.getScope();
	}

	public Namespace getGlobalNamespace() {
		return module;
	}

	/**
	 * Expression being printed.
	 */
	public exprType getExpression() {
		return ((Print) node.getNode()).values[0];
	}
	
	/**
	 * Expression being printed as a String if it's a simple variable.
	 */
	public String getExpressionName() {
		return ((Name)getExpression()).id;
	}

	private ScopedPrintNode(MutableModel model, String moduleName, String tag)
			throws Exception {

		module = model.loadModule(moduleName);
		node = findNode(module, tag);
	}

	private static ScopedAstNode findNode(Module module, String tag)
			throws Exception {
		assertTrue("Module not found", module != null);
		ScopedAstNode node = new TaggedNodeAndScopeFinder(module, tag)
				.getTaggedNode();
		assertTrue("Unable to find node tagged with '" + tag + "'",
				node != null);
		return node;
	}

}