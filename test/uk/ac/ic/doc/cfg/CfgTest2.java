package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

public class CfgTest2 {

	private class ControlFlowGraphTest extends AbstractTaggedGraphTest {

		public ControlFlowGraphTest(String[][] links, Set<BasicBlock> blocks,
				BasicBlock start, BasicBlock end) {
			super(links, blocks, start, end, "Control-flow");
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return source.getSuccessors().contains(target);
		}

		@Override
		protected boolean selfLinkRequired() {
			return false;
		}

		@Override
		protected Set<BasicBlock> getLinkToAllBlocks() {
			return new HashSet<BasicBlock>();
		}
	}

	private static final String DOMINATION_PROJ = "python_test_code/domination";

	private Cfg graph;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialise(String testFuncName) throws Throwable, Exception {
		Model model = createTestModel(DOMINATION_PROJ);
		Function function = model.getTopLevelPackage().getModules().get(
				"my_module").getFunctions().get(testFuncName);
		assertTrue("No function " + testFuncName, function != null);

		graph = function.getCfg();
	}

	private void checkControlFlow(String[][] dominators) {

		Set<BasicBlock> allBlocks = graph.getBlocks();
		BasicBlock start = findStartBlock(allBlocks);
		BasicBlock end = findEndBlock(allBlocks);

		new ControlFlowGraphTest(dominators, allBlocks, start, end).run();
	}

	private BasicBlock findStartBlock(Set<BasicBlock> blocks) {
		BasicBlock start = null;
		for (BasicBlock block : blocks) {
			if (block.getPredecessors().isEmpty()) {
				assertTrue("Multiple START blocks found", start == null);
				start = block;
			}
		}

		assertTrue("No START block found", start != null);

		return start;
	}

	private BasicBlock findEndBlock(Set<BasicBlock> blocks) {
		BasicBlock end = null;
		for (BasicBlock block : blocks) {
			if (block.getSuccessors().isEmpty()) {
				assertTrue("Multiple END blocks found", end == null);
				end = block;
			}
		}

		assertTrue("No END block found", end != null);

		return end;
	}

	@Test
	public void testCfg() throws Throwable {
		initialise("dom");

		String[][] graph = { { "START", "a" }, { "b", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIf() throws Throwable {
		initialise("dom_if");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "END" },
				{ "c", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElse() throws Throwable {
		initialise("dom_if_else");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "END" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfFallthru() throws Throwable {
		initialise("dom_if_fallthru");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "d" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElseFallthru() throws Throwable {
		initialise("dom_if_else_fallthru");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "e" }, { "d", "e" }, { "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhile() throws Throwable {
		initialise("dom_while");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "b" }, { "b", "d" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNested() throws Throwable {
		initialise("dom_nested");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "g" },
				{ "c", "d" }, { "d", "e" }, { "e", "d" }, { "d", "f" },
				{ "f", "h" }, { "g", "h" }, { "h", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIf() throws Throwable {
		initialise("dom_nested_while_if");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "e" }, { "e", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIfBreak() throws Throwable {
		initialise("dom_nested_while_if_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "END" }, { "e", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIfBreakElse() throws Throwable {
		initialise("dom_nested_while_if_break_else");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "END" }, { "e", "f" }, { "f", "a" },
				{ "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesBreak() throws Throwable {
		initialise("dom_nested_whiles_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "d", "a" }, { "c", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesIfBreak() throws Throwable {
		initialise("dom_nested_whiles_if_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "c", "a" }, { "e", "a" }, { "e", "c" },
				{ "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesBreakFall() throws Throwable {
		initialise("dom_nested_whiles_break_fall");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "c", "a" }, { "d", "a" }, { "a", "e" },
				{ "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedIfsBreak() throws Throwable {
		initialise("dom_nested_ifs_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "a" },
				{ "b", "c" }, { "c", "d" }, { "c", "e" }, { "d", "a" },
				{ "a", "e" }, { "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElseBreak() throws Throwable {
		initialise("dom_if_else_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "c", "a" }, { "a", "d" },
				{ "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgTwoprongedFallthoughToWhile() throws Throwable {
		initialise("dom_twopronged_fallthrough_to_while");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
				{ "b", "d" }, { "c", "d" }, { "d", "e" }, { "e", "d" },
				{ "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhileIfContinue1() throws Throwable {
		initialise("dom_while_if_continue1");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "b", "a" }, { "c", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhileIfContinue2() throws Throwable {
		initialise("dom_while_if_continue2");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "a" }, { "b", "d" }, { "d", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}
}
