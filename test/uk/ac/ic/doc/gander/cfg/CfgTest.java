package uk.ac.ic.doc.gander.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Num;

import uk.ac.ic.doc.gander.cfg.Model;
import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
import uk.ac.ic.doc.gander.cfg.model.Cfg;
import uk.ac.ic.doc.gander.cfg.model.Function;

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
		Function function = model.getTopLevelPackage().getModules().get(
				"my_module").getFunctions().get(testFuncName);
		Cfg graph = function.getCfg();

		start = graph.getStart();
		assertFalse("START not empty", start.iterator().hasNext());
		assertEquals("START links incorrect", 1, start.getSuccessors().size());

		end = graph.getEnd();
		assertFalse("END not empty", end.iterator().hasNext());
		assertEquals("END links incorrect", 0, end.getSuccessors().size());

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
		BasicBlock block = start.getSuccessors().iterator().next();
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
		Collection<BasicBlock> out = block.getSuccessors();
		assertEquals(1, out.size());
		assertEquals(end, out.iterator().next());
	}

	@Test
	public void testIfControlFlow() throws Throwable {
		initialiseGraph("my_fun_if");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 links to next block (if-stmt body) and END
		assertTrue("Doesn't link to END", block1.getSuccessors().contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		BasicBlock block2 = null;
		for (BasicBlock b : block1.getSuccessors()) { // find non-END successor
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
		assertTrue("Doesn't link to END", block2.getSuccessors().contains(end));
		assertEquals("Too many successors", 1, block2.getSuccessors().size());
	}

	@Test
	public void testIfImmediate() throws Throwable {
		initialiseGraph("my_fun_if_immediate");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // if a():
		assertFalse("Only one statement expected in Block 1", nodes.hasNext());

		// Block 1 links to next block (if-stmt body) and END
		assertTrue("Doesn't link to END", block1.getSuccessors().contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		BasicBlock block2 = null;
		for (BasicBlock b : block1.getSuccessors()) { // find non-END successor
			if (b != end) {
				block2 = b;
				break;
			}
		}
		assertTrue("No non-END successor to Block 1", block2 != null);

		nodes = block2.iterator();
		assertFunctionNamed("b", nodes.next()); // b()
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links only to END
		assertTrue("Doesn't link to END", block2.getSuccessors().contains(end));
		assertEquals("Too many successors", 1, block2.getSuccessors().size());
	}

	@Test
	public void testIfElse() throws Throwable {
		initialiseGraph("my_fun_if_else");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 must not link to END - it has to get there either via
		// 'then' or 'else'
		assertTrue("Block 1 must not link to END", !block1.getSuccessors()
				.contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		// use first successor - we don't know (care) whether this is the
		// 'then' or the 'else' branch
		Iterator<BasicBlock> block1Successors = block1.getSuccessors().iterator();
		BasicBlock block2 = block1Successors.next();
		assertTrue("Block 1 has no successors", block2 != null);

		nodes = block2.iterator();
		SimpleNode node = nodes.next();
		assertTrue(
				// c() or d()
				"Neither 'c()' nor 'c()' found: doesn't match either branch",
				isFunctionNamed("c", node) || isFunctionNamed("d", node));

		boolean block2IsThenBranch = false;
		if (isFunctionNamed("c", node))
			block2IsThenBranch = true;
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links only to END
		assertTrue("Doesn't link to END", block2.getSuccessors().contains(end));
		assertEquals("Too many successors", 1, block2.getSuccessors().size());

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
		assertTrue("Doesn't link to END", block3.getSuccessors().contains(end));
		assertEquals("Too many successors", 1, block3.getSuccessors().size());
	}

	@Test
	public void testIfFallthru() throws Throwable {
		initialiseGraph("my_fun_if_fallthru");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 links to body of if-stmt and fallthrough block
		assertFalse("Block 1 must not link to END", block1.getSuccessors()
				.contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		// use first successor - we don't know (care) whether this
		// is the 'then' block or the fallthrough but it should not be END
		Iterator<BasicBlock> block1Successors = block1.getSuccessors().iterator();
		BasicBlock block2 = block1Successors.next();
		assertTrue("Block 1 has no successors", block2 != null);
		assertNotSame(end, block2);

		nodes = block2.iterator();
		SimpleNode node = nodes.next();
		assertTrue(
				// c() or d()
				"Neither 'c()' nor 'd()' found: doesn't match either "
						+ "the if-body or the fallthough block.",
				isFunctionNamed("c", node) || isFunctionNamed("d", node));

		boolean block2IsIfBody = false;
		if (isFunctionNamed("c", node))
			block2IsIfBody = true;
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links to the END if it is the fallthrough block
		if (block2IsIfBody)
			assertFalse("Mustn't link to END", block2.getSuccessors().contains(end));
		// Either way it should only have one successor
		assertEquals("Too many successors", 1, block2.getSuccessors().size());

		// Block 3
		// Use whichever successor we didn't use as Block 2 - we don't
		// know (care) whether this is the if-body or the fallthrough block
		// but we do care that whatever it is, it must be the *other* one.
		BasicBlock block3 = block1Successors.next();
		assertTrue("Couldn't find other branch branch", block3 != null);

		nodes = block3.iterator();
		node = nodes.next();
		if (block2IsIfBody) {
			// we are looking all the fallthru block
			assertFunctionNamed("d", node);
			assertTrue("Fallthru block must link to END", block3.getSuccessors()
					.contains(end));
		} else {
			assertFunctionNamed("c", node);
			assertTrue("If-body must link to fallthru block", block3
					.getSuccessors().contains(block2));
		}
		assertFalse("Only one statement expected in Block 3", nodes.hasNext());
		assertEquals("Too many successors", 1, block3.getSuccessors().size());
	}

	@Test
	public void testIfElseFallthru() throws Throwable {
		initialiseGraph("my_fun_if_else_fallthru");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 must not link to END - it has to get there either via
		// 'then' or 'else'
		assertFalse("Block 1 must not link to END", block1.getSuccessors()
				.contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		// use first successor - we don't know (care) whether this is the
		// 'then' or the 'else' branch
		Iterator<BasicBlock> block1Successors = block1.getSuccessors().iterator();
		BasicBlock block2 = block1Successors.next();
		assertTrue("Block 1 has no successors", block2 != null);

		nodes = block2.iterator();
		SimpleNode node = nodes.next();
		assertTrue(
				// c() or d()
				"Neither 'c()' nor 'd()' found: doesn't match either branch",
				isFunctionNamed("c", node) || isFunctionNamed("d", node));

		boolean block2IsThenBranch = false;
		if (isFunctionNamed("c", node))
			block2IsThenBranch = true;
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

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

		// Block 4
		// Should be the fallthrough block

		// Block 2 and Block 3 should only link to the fallthru (Block 4)
		assertEquals("Block 2 has too many successors", 1, block2.getSuccessors()
				.size());
		assertEquals("Block 3 has too many successors", 1, block3.getSuccessors()
				.size());
		BasicBlock block4 = block2.getSuccessors().iterator().next();
		assertEquals("Blocks 2 and 3 must point at the same fallthru block",
				block4, block3.getSuccessors().iterator().next());

		nodes = block4.iterator();
		assertFunctionNamed("e", nodes.next());
		assertFalse("Only one statement expected in Block 4", nodes.hasNext());

		assertTrue("Fallthru must link to END", block4.getSuccessors()
				.contains(end));
	}

	@Test
	public void testWhile() throws Throwable {
		initialiseGraph("my_fun_while");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFalse("Only one statements expected in Block 1", nodes.hasNext());

		// Block 1 links to while-test block
		assertEquals("Must be only one successor", 1, block1.getSuccessors().size());
		assertFalse("Successor must not be END", block1.getSuccessors().contains(
				end));

		// Block 2
		BasicBlock block2 = block1.getSuccessors().iterator().next();
		nodes = block2.iterator();
		assertFunctionNamed("b", nodes.next()); // b()
		assertFalse("Only one statements expected in Block 2", nodes.hasNext());

		// Block 2 links to while-body and END
		assertEquals("Wrong number of successors", 2, block2.getSuccessors().size());
		assertTrue("Must link to END", block2.getSuccessors().contains(end));

		// Block3
		BasicBlock block3 = null;
		for (BasicBlock b : block2.getSuccessors()) { // find non-END successor
			if (b != end) {
				block3 = b;
				break;
			}
		}
		assertTrue("No non-END successor to Block 2", block2 != null);

		nodes = block3.iterator();
		assertFunctionNamed("c", nodes.next()); // c()
		assertFalse("Only one statement expected in Block 3", nodes.hasNext());

		// Block 3 links only to while-test
		assertTrue("Doesn't link back to while-test (Block 2)", block3
				.getSuccessors().contains(block2));
		assertEquals("Too many successors", 1, block3.getSuccessors().size());
	}

	@Test
	public void testWhileImmediate() throws Throwable {
		initialiseGraph("my_fun_while_immediate");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFalse("Only one statements expected in Block 1", nodes.hasNext());

		// Block 1 links to while-body and END
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());
		assertTrue("Must link to END", block1.getSuccessors().contains(end));

		// Block3
		BasicBlock block2 = null;
		for (BasicBlock b : block1.getSuccessors()) { // find non-END successor
			if (b != end) {
				block2 = b;
				break;
			}
		}
		assertTrue("No non-END successor to Block 1", block1 != null);

		nodes = block2.iterator();
		assertFunctionNamed("b", nodes.next()); // b()
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		// Block 2 links only to while-test
		assertTrue("Doesn't link back to while-test (Block 1)", block2
				.getSuccessors().contains(block1));
		assertEquals("Too many successors", 1, block2.getSuccessors().size());
	}

	@Test
	public void testWhileFallthru() throws Throwable {
		initialiseGraph("my_fun_while_fallthru");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFalse("Only one statements expected in Block 1", nodes.hasNext());

		// Block 1 links to while-test block
		assertEquals("Must be only one successor", 1, block1.getSuccessors().size());
		assertFalse("Successor must not be END", block1.getSuccessors().contains(
				end));

		// Block 2
		BasicBlock block2 = block1.getSuccessors().iterator().next();
		nodes = block2.iterator();
		assertFunctionNamed("b", nodes.next()); // b()
		assertFalse("Only one statements expected in Block 2", nodes.hasNext());

		// Block 2 links to while-body and fallthru
		assertEquals("Wrong number of successors", 2, block2.getSuccessors().size());
		assertFalse("Must not link to END", block2.getSuccessors().contains(end));

		Iterator<BasicBlock> block2Successors = block2.getSuccessors().iterator();

		// Block3
		// May be while-body or fallthru - we don't yet know which
		BasicBlock block3 = block2Successors.next();
		nodes = block3.iterator();
		SimpleNode node = nodes.next();
		assertTrue(
				// c() or d()
				"Neither 'c()' nor 'd()' found: doesn't match either block",
				isFunctionNamed("c", node) || isFunctionNamed("d", node));
		assertEquals("Too many successors", 1, block3.getSuccessors().size());

		boolean block3IsFallthru = isFunctionNamed("d", node);
		if (block3IsFallthru) {
			assertTrue("Must link to END", block3.getSuccessors().contains(end));
		} else {
			assertTrue("Must link to while-test", block3.getSuccessors().contains(
					block2));
		}

		// Block4
		BasicBlock block4 = block2Successors.next();
		assertEquals("Too many successors", 1, block4.getSuccessors().size());
		nodes = block4.iterator();
		if (block3IsFallthru) {
			assertFunctionNamed("c", nodes.next());
			assertTrue("Must link to while-test", block4.getSuccessors().contains(
					block2));
		} else {
			assertFunctionNamed("d", nodes.next());
			assertTrue("Must link to END", block4.getSuccessors().contains(end));
		}

		assertFalse("Only one statement expected in Block 4", nodes.hasNext());
	}

	@Test
	public void testNestedIf() throws Throwable {
		initialiseGraph("my_fun_nested_if");

		// Block 1
		BasicBlock block1 = start.getSuccessors().iterator().next();
		Iterator<SimpleNode> nodes = block1.iterator();
		assertFunctionNamed("a", nodes.next()); // a()
		assertFunctionNamed("b", nodes.next()); // if b():
		assertFalse("Only two statements expected in Block 1", nodes.hasNext());

		// Block 1 links to body of if-stmt (inner if's test) and fallthrough
		assertFalse("Block 1 must not link to END", block1.getSuccessors()
				.contains(end));
		assertEquals("Wrong number of successors", 2, block1.getSuccessors().size());

		// Block 2
		// use first successor - we don't know (care) whether this
		// is the 'then' block or the fallthrough but it should not be END
		Iterator<BasicBlock> block1Successors = block1.getSuccessors().iterator();
		BasicBlock block2 = block1Successors.next();
		assertTrue("Block 1 has no successors", block2 != null);
		assertNotSame(end, block2);

		nodes = block2.iterator();
		SimpleNode node = nodes.next();
		assertTrue(
				// c() or e()
				"Neither 'c()' nor 'e()' found: doesn't match either "
						+ "the if-body or the fallthough block.",
				isFunctionNamed("c", node) || isFunctionNamed("e", node));

		boolean block2IsIfBody = false;
		if (isFunctionNamed("c", node))
			block2IsIfBody = true;
		assertFalse("Only one statement expected in Block 2", nodes.hasNext());

		if (!block2IsIfBody) {
			// Block 2 links to the END alone if it is the fallthrough block
			assertTrue("Must link to END", block2.getSuccessors().contains(end));
			assertEquals("Too many successors", 1, block2.getSuccessors().size());
		} else {
			// otherwise it links to the inner if's body (d()) and the fallthru
			assertFalse("Must not link to END", block2.getSuccessors()
					.contains(end));
			assertEquals(
					"If-body (inner if-test) should link to inner if-body "
							+ "and fallthru", 2, block2.getSuccessors().size());
		}

		// Block 3
		// Use whichever successor we didn't use as Block 2 - we don't
		// know (care) whether this is the if-body or the fallthrough block
		// but we do care that whatever it is, it must be the *other* one.
		BasicBlock block3 = block1Successors.next();
		assertTrue("Couldn't find other branch branch", block3 != null);

		nodes = block3.iterator();
		node = nodes.next();
		if (block2IsIfBody) {
			// we are looking all the fallthru block
			assertFunctionNamed("e", node);
			assertTrue("Fallthru block must link to END", block3.getSuccessors()
					.contains(end));
			assertEquals("Too many successors", 1, block3.getSuccessors().size());
		} else {
			assertFunctionNamed("c", node);
			assertTrue("If-body must link to fallthru block", block3
					.getSuccessors().contains(block2));
		}
		assertFalse("Only one statement expected in Block 3", nodes.hasNext());

		// Block 4
		// The inner if-body is one of the successors of either Block 2 or
		// Block 3 depending on which one is the outer if-body (the inner
		// if-test). We find it by looking for the successor that isn't the
		// fallthrough block.
		BasicBlock block4 = null;
		BasicBlock outerIfBody = (block2IsIfBody ? block2 : block3);
		BasicBlock fallthruBlock = (block2IsIfBody ? block3 : block2);
		for (BasicBlock b : outerIfBody.getSuccessors()) {
			if (b != fallthruBlock) {
				block4 = b;
				break;
			}
		}
		assertTrue("No non-fallthru successor to out if-body", block4 != null);

		nodes = block4.iterator();
		node = nodes.next();
		assertFunctionNamed("d", node);
		assertFalse("Only one statement expected in Block 4", nodes.hasNext());

		// Block 4 only links to fallthrough block
		assertEquals("Too many successors", 1, block4.getSuccessors().size());
		assertTrue("Must link to fallthru", block4.getSuccessors().contains(
				fallthruBlock));
	}

}
