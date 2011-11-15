package uk.ac.ic.doc.gander.ast;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.ScopedAstNode;
import uk.ac.ic.doc.gander.TaggedNodeAndScopeFinder;
import uk.ac.ic.doc.gander.model.MutableModel;

public class AstParentNodeFinderTest {
	private static final String TEST_FOLDER = "python_test_code";

	private MutableModel model;

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
	}

	@Test
	public void callParent1() throws Throwable {
		ScopedAstNode node = findNode("call_parent", "test1");
		Call call = ((Call) ((Expr) node.getNode()).value);
		Name callable = ((Name) call.func);
		SimpleNode parent = AstParentNodeFinder.findParent(callable, node
				.getScope().getAst());

		assertEquals(call, parent);
	}

	@Test
	public void callParent2() throws Throwable {
		ScopedAstNode node = findNode("call_parent", "test2");
		Call call = ((Call)((Call) ((Expr) node.getNode()).value).func);
		Name callable = ((Name) call.func);
		SimpleNode parent = AstParentNodeFinder.findParent(callable, node
				.getScope().getAst());

		assertEquals(call, parent);
	}


	private ScopedAstNode findNode(String moduleName, String tag)
			throws Exception {
		return new TaggedNodeAndScopeFinder(model.loadModule(moduleName), tag)
				.getTaggedNode();
	}
}
