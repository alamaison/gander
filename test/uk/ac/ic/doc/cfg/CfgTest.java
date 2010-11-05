package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Expr;
import org.python.pydev.parser.jython.ast.If;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.Num;
import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

public class CfgTest {

	private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	@Test
	public void testSimpleControlFlow() throws Throwable {
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Function function = model.getTopLevelPackage().getModules().get(
				"my_module").getFunctions().get("my_fun");
		Cfg graph = function.getCfg();
		
		// START block
		BasicBlock start = graph.getStart();
		assertFalse("START not empty", start.iterator().hasNext());
		assertEquals("START links incorrect", 1, start.getOutSet().size());
		
		// END block
		BasicBlock end = graph.getEnd();
		assertFalse("END not empty", end.iterator().hasNext());
		assertEquals("END links incorrect", 0, end.getOutSet().size());
		
		// test that the start and end are distinguishable from each other
		assertNotSame("START node indistinguishable from END", start, end);
		
		// Block 1
		BasicBlock block = start.getOutSet().iterator().next();
		int i = 0;
		for (stmtType node : block) {
			if (i == 0) {
				// x = 3
				Assign ass = (Assign)node;
				Num num = (Num)ass.value;
				assertTrue(num.num.equals("3"));
			
				assertEquals(1, ass.targets.length);
				Name name = (Name)ass.targets[0];
				assertTrue(name.id.equals("x"));
			} else if (i == 1) {
				// y.m()
				Expr expr = (Expr)node;
				Call call = (Call)expr.value;
				
				Attribute attr = (Attribute)call.func;
				Name objectName = (Name)attr.value;
				assertEquals("y", objectName.id);
				NameTok methodName = (NameTok)attr.attr;
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
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Function function = model.getTopLevelPackage().getModules().get(
				"my_module").getFunctions().get("my_fun_if");
		Cfg graph = function.getCfg();
		
		// START block
		BasicBlock start = graph.getStart();
		assertFalse("START not empty", start.iterator().hasNext());
		assertEquals("START links incorrect", 1, start.getOutSet().size());
		
		// END block
		BasicBlock end = graph.getEnd();
		assertFalse("END not empty", end.iterator().hasNext());
		assertEquals("END links incorrect", 0, end.getOutSet().size());
		
		// test that the start and end are distinguishable from each other
		assertNotSame("START node indistinguishable from END", start, end);
		
		// Block 1
		BasicBlock block1 = start.getOutSet().iterator().next();
		int i = 0;
		for (stmtType stmt : block1) {
			if (i == 0) {
				// x = 3
				Assign ass = (Assign)stmt;
				Num num = (Num)ass.value;
				assertTrue(num.num.equals("3"));
			
				assertEquals(1, ass.targets.length);
				Name name = (Name)ass.targets[0];
				assertTrue(name.id.equals("x"));
			} else if (i == 1) {
				// if True:
				If cond = (If)stmt;
				Name constTrue = (Name)cond.test;
				assertTrue(constTrue.id.equals("True"));
			} else {
				assertTrue("Too many statements", false);
			}

			i++;
		}
		assertEquals(2, i); // two statements expected in 1st block
		
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
		
		int i2 = 0;
		for (stmtType stmt : block2) {
			if (i2 == 0) {
				// y.m()
				Expr expr = (Expr)stmt;
				Call call = (Call)expr.value;
				
				Attribute attr = (Attribute)call.func;
				Name objectName = (Name)attr.value;
				assertEquals("y", objectName.id);
				NameTok methodName = (NameTok)attr.attr;
				assertEquals("m", methodName.id);
			} else {
				assertTrue(false);
			}

			i2++;
		}
		assertEquals(1, i2); // one statement expected in 2nd block		
		
		// Block 2 links only to END
		assertTrue("Doesn't link to END", block2.getOutSet().contains(end));
		assertEquals("Too many successors", 1, block2.getOutSet().size());
	}
}
