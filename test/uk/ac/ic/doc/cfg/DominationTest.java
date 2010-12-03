package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

public class DominationTest {

	private class DominationGraphTest extends GraphTest {

		private AbstractDomination analyser;

		public DominationGraphTest(Map<String, Collection<String>> links,
				Set<BasicBlock> allBlocks, BasicBlock start, BasicBlock end,
				AbstractDomination domAnalyser) {
			super(links, allBlocks, start, end, "Domination");
			this.analyser = domAnalyser;
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return analyser.dominates(source, target);
		}

		@Override
		protected BasicBlock tagToBlock(String tag) {
			return findBlockContainingCall(tag, analyser);
		}

		@Override
		protected boolean selfLinkRequired() {
			return true;
		}

	}

	private class PostdominationGraphTest extends GraphTest {

		private AbstractDomination analyser;

		public PostdominationGraphTest(Map<String, Collection<String>> links,
				Set<BasicBlock> allBlocks, BasicBlock start, BasicBlock end,
				AbstractDomination domAnalyser) {
			super(links, allBlocks, start, end, "Postdomination");
			this.analyser = domAnalyser;
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return analyser.dominates(source, target);
		}

		@Override
		protected BasicBlock tagToBlock(String tag) {
			return findBlockContainingCall(tag, analyser);
		}

		@Override
		protected boolean selfLinkRequired() {
			return true;
		}

	}

	private static final String DOMINATION_PROJ = "python_test_code/domination";

	private Domination domAnalyser;
	private Postdomination postdomAnalyser;

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

		Cfg graph = function.getCfg();

		domAnalyser = new Domination(graph.getBlocks(), graph.getStart());
		postdomAnalyser = new Postdomination(graph.getBlocks(), graph.getEnd());
	}

	private void checkDomination(String[][] dominators) {
		// Convert 2D-array with possible multiple 'keys' into map of lists
		Map<String, Collection<String>> m = new HashMap<String, Collection<String>>();

		for (String[] d : dominators) {
			if (!m.containsKey(d[0]))
				m.put(d[0], new ArrayList<String>());
			m.get(d[0]).add(d[1]);
		}

		checkDomination(m);
	}

	private void checkPostdomination(String[][] dominators) {
		// Convert 2D-array with possible multiple 'keys' into map of lists
		Map<String, Collection<String>> m = new HashMap<String, Collection<String>>();

		for (String[] d : dominators) {
			if (!m.containsKey(d[0]))
				m.put(d[0], new ArrayList<String>());
			m.get(d[0]).add(d[1]);
		}

		checkPostdomination(m);
	}

	private void checkDomination(Map<String, Collection<String>> dominators) {

		Set<BasicBlock> allBlocks = domAnalyser.getBlocks();
		BasicBlock start = findStartBlock(allBlocks);
		BasicBlock end = findEndBlock(allBlocks);

		checkStartEndDomination(allBlocks, start, end);

		GraphTest test = new DominationGraphTest(dominators, allBlocks, start,
				end, domAnalyser);
		test.run();
	}

	private void checkPostdomination(Map<String, Collection<String>> dominators) {

		Set<BasicBlock> allBlocks = postdomAnalyser.getBlocks();
		BasicBlock start = findStartBlock(allBlocks);
		BasicBlock end = findEndBlock(allBlocks);

		checkStartEndPostdomination(allBlocks, start, end);

		GraphTest test = new PostdominationGraphTest(dominators, allBlocks,
				start, end, postdomAnalyser);
		test.run();
	}

	/**
	 * START dominates all blocks and END dominates nothing.
	 */
	private void checkStartEndDomination(Set<BasicBlock> blocks,
			BasicBlock start, BasicBlock end) {
		assertNotSame(start, end);

		for (BasicBlock block : blocks) {
			assertTrue("START must dominate all other blocks", domAnalyser
					.dominates(start, block));
			if (block != end)
				assertFalse("END must not dominate anything execpt itself",
						domAnalyser.dominates(end, block));
		}
	}

	/**
	 * END postdominates all blocks and START postdominates nothing.
	 */
	private void checkStartEndPostdomination(Set<BasicBlock> blocks,
			BasicBlock start, BasicBlock end) {
		assertNotSame(start, end);

		for (BasicBlock block : blocks) {
			assertTrue("END must postdominate all other blocks",
					postdomAnalyser.dominates(end, block));
			if (block != start)
				assertFalse(
						"START must not postdominate anything execpt itself",
						postdomAnalyser.dominates(start, block));
		}
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

	// You can have more than one different tag in a basic block but not more
	// than one basic block containing the same tag.
	private static BasicBlock findBlockContainingCall(String tag,
			AbstractDomination analyser) {

		Collection<BasicBlock> blocks = analyser.getBlocks();
		BasicBlock block = null;
		for (BasicBlock b : blocks) {
			if (isBlockTaggedWithFunction(b, tag)) {
				assertTrue("Multiple nodes with same function tag: " + tag
						+ ". This violates the convention we are using for "
						+ "these tests.", block == null);
				block = b;
			}
		}

		return block;
	}

	private static boolean isBlockTaggedWithFunction(BasicBlock block,
			String tag) {
		boolean found = false;
		for (SimpleNode node : block) {
			if (isFunctionNamed(tag, node)) {
				assertFalse(
						"Multiple statements in the block with same function "
								+ "tag: " + tag + ". This violates the "
								+ "convention we are using for these tests.",
						found);
				found = true;
			}
		}
		return found;
	}

	private static boolean isFunctionNamed(String name, SimpleNode node) {
		Call call = (Call) node;
		Name funcName = (Name) call.func;
		return funcName.id.equals(name);
	}

	@Test
	public void testDom() throws Throwable {
		initialise("dom");

		String[][] dominators = { { "a", "b" }, { "a", "END" }, { "b", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "a", "START" },
				{ "b", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIf() throws Throwable {
		initialise("dom_if");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "b", "c" },
				{ "a", "END" }, { "b", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "a", "START" },
				{ "b", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIfElse() throws Throwable {
		initialise("dom_if_else");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" }, { "a", "END" },
				{ "b", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "a", "START" },
				{ "b", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIfFallthru() throws Throwable {
		initialise("dom_if_fallthru");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" }, { "b", "END" },
				{ "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "d", "c" }, { "d", "b" },
				{ "d", "a" }, { "a", "START" }, { "b", "START" },
				{ "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIfElseFallthru() throws Throwable {
		initialise("dom_if_else_fallthru");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" },
				{ "a", "END" }, { "b", "END" }, { "e", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "e", "d" }, { "e", "c" },
				{ "e", "b" }, { "e", "a" }, { "a", "START" }, { "b", "START" },
				{ "e", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomWhile() throws Throwable {
		initialise("dom_while");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" }, { "b", "END" },
				{ "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "b", "c" }, { "d", "c" },
				{ "d", "b" }, { "d", "a" }, { "a", "START" }, { "b", "START" },
				{ "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNested() throws Throwable {
		initialise("dom_nested");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "a", "f" }, { "a", "g" }, { "a", "h" },
				{ "b", "c" }, { "b", "d" }, { "b", "e" }, { "b", "f" },
				{ "b", "g" }, { "b", "h" }, { "c", "d" }, { "c", "d" },
				{ "c", "e" }, { "c", "f" }, { "d", "e" }, { "d", "f" },
				{ "a", "END" }, { "b", "END" }, { "h", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "d", "c" }, { "d", "e" },
				{ "f", "e" }, { "f", "d" }, { "f", "c" }, { "h", "a" },
				{ "h", "b" }, { "h", "c" }, { "h", "d" }, { "h", "e" },
				{ "h", "f" }, { "h", "g" }, { "a", "START" }, { "b", "START" },
				{ "h", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhileIf() throws Throwable {
		initialise("dom_nested_while_if");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" },
				{ "c", "d" }, { "c", "e" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "c", "b" }, { "e", "d" }, { "e", "c" },
				{ "e", "b" }, { "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhileIfBreak() throws Throwable {
		initialise("dom_nested_while_if_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" },
				{ "c", "d" }, { "c", "e" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "e" }, { "c", "b" },
				{ "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhileIfBreakElse() throws Throwable {
		initialise("dom_nested_while_if_break_else");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "a", "f" }, { "b", "c" }, { "b", "d" },
				{ "b", "e" }, { "b", "f" }, { "c", "d" }, { "c", "e" },
				{ "c", "f" }, { "e", "f" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "c", "b" }, { "a", "e" }, { "a", "f" },
				{ "f", "e" }, { "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhilesBreak() throws Throwable {
		initialise("dom_nested_whiles_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "c", "d" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "c", "b" }, { "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhilesIfBreak() throws Throwable {
		initialise("dom_nested_whiles_if_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" },
				{ "c", "d" }, { "c", "e" }, { "d", "e" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "c", "b" }, { "e", "d" }, { "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhilesBreakFall() throws Throwable {
		initialise("dom_nested_whiles_break_fall");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "c", "d" },
				{ "a", "END" }, { "e", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "c", "b" }, { "e", "a" }, { "e", "b" }, { "e", "c" },
				{ "e", "d" }, { "a", "START" }, { "e", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedIfsBreak() throws Throwable {
		initialise("dom_nested_ifs_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "c", "d" },
				{ "a", "END" }, { "e", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "d" }, { "e", "a" }, { "e", "b" },
				{ "e", "c" }, { "e", "d" }, { "a", "START" }, { "e", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIfElseBreak() throws Throwable {
		initialise("dom_if_else_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "a", "END" }, { "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "c" }, { "d", "a" }, { "d", "b" },
				{ "d", "c" }, { "a", "START" }, { "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomTwoprongedFallthoughToWhile() throws Throwable {
		initialise("dom_twopronged_fallthrough_to_while");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "d", "e" }, { "a", "END" }, { "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "d", "a" }, { "d", "b" }, { "d", "c" },
				{ "d", "e" }, { "a", "START" }, { "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomWhileIfContinue1() throws Throwable {
		initialise("dom_while_if_continue1");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "b", "c" },
				{ "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" },
				{ "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomWhileIfContinue2() throws Throwable {
		initialise("dom_while_if_continue2");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "START" } };
		checkPostdomination(postdominators);
	}
}
