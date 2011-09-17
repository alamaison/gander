package uk.ac.ic.doc.gander.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Pass;
import org.python.pydev.parser.jython.ast.VisitorBase;

class TraverseEverythingVisitor extends VisitorBase {

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		throw new AssertionFailedError("Unexpected item in the baggage area");
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

}

/**
 * Test that {@link CodeBlock}s model the Python code block concept correctly.
 */
public class CodeBlockTest extends AbstractModelTest {

	private Module module;

	@Before
	public void setup() throws Throwable {
		createTestModel(MODULE_STRUCTURE_PROJ);
		module = getModel().loadModule("my_module");
	}

	@Test
	public void module() throws Throwable {
		CodeBlock block = module.getCodeBlock();
		assertEquals(Collections.emptySet(), block.getFormalParameters());
	}

	@Test
	public void classEmpty() throws Throwable {
		CodeBlock block = module.getClasses().get("my_class_empty")
				.getCodeBlock();
		assertEquals(Collections.emptySet(), block.getFormalParameters());

		block.accept(new TraverseEverythingVisitor() {
			@Override
			public Object visitPass(Pass node) throws Exception {
				return null;
			}
		});
	}

	@Test
	public void classWithMethod() throws Throwable {
		CodeBlock block = module.getClasses().get("my_class").getCodeBlock();
		assertEquals(Collections.emptySet(), block.getFormalParameters());

		block.accept(new TraverseEverythingVisitor() {
			@Override
			public Object visitFunctionDef(FunctionDef node) throws Exception {
				assertEquals("my_method_empty", ((NameTok) node.name).id);
				return null;
			}

			@Override
			public Object visitPass(Pass node) throws Exception {
				return null;
			}
		});
	}

	@Test
	public void function() throws Throwable {
		CodeBlock block = module.getFunctions().get("my_free_function")
				.getCodeBlock();
		assertEquals(Collections.emptySet(), block.getFormalParameters());

		block.accept(new TraverseEverythingVisitor() {
			@Override
			public Object visitPass(Pass node) throws Exception {
				return null;
			}
		});
	}

	@Test
	public void functionsWithArguments() throws Throwable {
		CodeBlock block = module.getFunctions().get("my_function_with_args")
				.getCodeBlock();
		Set<String> args = new HashSet<String>();
		args.add("a");
		args.add("b");
		assertEquals(args, block.getFormalParameters());

		block.accept(new TraverseEverythingVisitor() {
			@Override
			public Object visitPass(Pass node) throws Exception {
				return null;
			}
		});
	}
}
