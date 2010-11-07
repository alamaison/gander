package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Num;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

public class CfgTest {

	private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";
	private BasicBlock start;
	private BasicBlock end;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialiseGraph(String testFuncName) throws Throwable,
			Exception {
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Function function = model.getTopLevelPackage().getModules()
				.get("my_module").getFunctions().get(testFuncName);
		Cfg graph = function.getCfg();

		start = graph.getStart();
		assertFalse("START not empty", start.iterator().hasNext());
		assertEquals("START links incorrect", 1, start.getOutSet().size());

		end = graph.getEnd();
		assertFalse("END not empty", end.iterator().hasNext());
		assertEquals("END links incorrect", 0, end.getOutSet().size());

		// test that the start and end are distinguishable from each other
		assertNotSame("START node indistinguishable from END", start, end);
	}

	private static boolean isFunctionNamed(String name, SimpleNode node) {
		Call call = (Call) node;
		Name funcName = (Name) call.func;
		return funcName.id.equals(name);
	}

	private static void assertFunctionNamed(String name, SimpleNode node) {
		Call call = (Call) node;
		Name funcName = (Name) call.func;
		assertEquals("Function name doesn't match", name, funcName.id);
	}

	@Test
	public void testSimpleControlFlow() throws Throwable {
		initialiseGraph("my_fun");

		// Block 1
		BasicBlock block = start.getOutSet().iterator().next();
		int i = 0;
		for (SimpleNode node : block) {
			if (i == 0) {
				// x = 3
				Assign ass = (Assign) node;
				Num num = (Num) ass.value;
				assertTrue(num.num.equals("3"));

				assertEquals(1, ass.targets.length);
				Name name = (Name) ass.targets[0];
				assertTrue(name.id.equals("x"));
			} else if (i == 1) {
				// y.m()
				Call call = (Call) node;
				
				Attribute attr = (Attribute) call.func;
				Name objectName = (Name) attr.value;
				assertEquals("y", objectName.id);
				NameTok methodName = (NameTok) attr.attr;
				assertEquals("m", methodName.id);
			} else {
				assertTrue(false);
			}

			i++;
		}
		assertEquals(2, i); // two statements in block

		// Block 1 only links to the end block
		Set<BasicBlock> out = block.getOutSet();
		assertEquals(1, out.size());
		assertEquals(end, out.iterator().next());
	}

	@Test
	public void testIfControlFlow() throws Throwable {
		initialiseGraph("my_fun_if");

		// Block 1
		BasicBlock block1 = start.getOutSet().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 links to next block (if-stmt body) and END
		assertTrue("Doesn't link to END", block1.getOutSet().contains(end));
		assertEquals("Wrong number of successors", 2, block1.getOutSet().size());

		// Block 2
		BasicBlock block2 = null;
		for (BasicBlock b : block1.getOutSet()) { // find non-END successor
			if (b != end) {
				block2 = b;
				break;
			}
		}
		assertTrue("No non-END successor to Block 1", block2 != null);

		nodes = block2.iterator();
		assertFunctionNamed("c", nodes.next()); // c()
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links only to END
		assertTrue("Doesn't link to END", block2.getOutSet().contains(end));
		assertEquals("Too many successors", 1, block2.getOutSet().size());
	}

	@Test
	public void testIfElse() throws Throwable {
		initialiseGraph("my_fun_if_else");

		// Block 1
		BasicBlock block1 = start.getOutSet().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 must not link to END - it has to get there either via
		// 'then' or 'else'
		assertTrue("Block 1 must not link to END", !block1.getOutSet()
				.contains(end));
		assertEquals("Wrong number of successors", 2, block1.getOutSet().size());

		// Block 2
		// use first successor - we don't know (care) whether this is the
		// 'then' or the 'else' branch
		Iterator<BasicBlock> block1Successors = block1.getOutSet().iterator();
		BasicBlock block2 = block1Successors.next();
		assertTrue("Block 1 has no successors", block2 != null);

		nodes = block2.iterator();
		SimpleNode node = nodes.next();
		assertTrue( // c() or d()
				"Neither 'c()' nor 'c()' found: doesn't match either branch",
				isFunctionNamed("c", node) || isFunctionNamed("d", node));
		
		boolean block2IsThenBranch = false;
		if (isFunctionNamed("c", node))
			block2IsThenBranch = true;
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links only to END
		assertTrue("Doesn't link to END", block2.getOutSet().contains(end));
		assertEquals("Too many successors", 1, block2.getOutSet().size());

		// Block 3
		// use whichever successor we didn't use as Block 2 - we don't
		// know (care) whether this is the 'then' or the 'else' branch but we
		// do care that whatever it is, it must be the *other* one.
		BasicBlock block3 = block1Successors.next();
		assertTrue("Couldn't find other branch branch", block3 != null);

		nodes = block3.iterator();
		node = nodes.next();
		if (block2IsThenBranch)
			assertFunctionNamed("d", node);
		else
			assertFunctionNamed("c", node);
		assertFalse("Only one statement expected in Block 3", nodes.hasNext());
		
		// Block 3 links only to END
		assertTrue("Doesn't link to END", block3.getOutSet().contains(end));
		assertEquals("Too many successors", 1, block3.getOutSet().size());
	}
}
