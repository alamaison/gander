package uk.ac.ic.doc.gander.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.AbstractTaggedGraphTest;
import uk.ac.ic.doc.gander.analysis.dominance.AbstractDomination;
import uk.ac.ic.doc.gander.analysis.dominance.Domination;
import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;

public class DominationTest {

	class DominationGraphTest extends AbstractTaggedGraphTest {

		private AbstractDomination analyser;

		public DominationGraphTest(String[][] links, Cfg graph,
				AbstractDomination domAnalyser) {
			super(links, graph, "Domination");
			this.analyser = domAnalyser;
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return analyser.dominates(source, target);
		}

		@Override
		protected boolean selfLinkRequired() {
			return true;
		}

		@Override
		protected Set<BasicBlock> getLinkToAllBlocks() {
			Set<BasicBlock> startSet = new HashSet<BasicBlock>();
			startSet.add(getStart());
			return startSet;
		}

	}

	private class PostdominationGraphTest extends AbstractTaggedGraphTest {

		private AbstractDomination analyser;

		public PostdominationGraphTest(String[][] links, Cfg graph,
				AbstractDomination domAnalyser) {
			super(links, graph, "Postdomination");
			this.analyser = domAnalyser;
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return analyser.dominates(source, target);
		}

		@Override
		protected boolean selfLinkRequired() {
			return true;
		}

		@Override
		protected Set<BasicBlock> getLinkToAllBlocks() {
			Set<BasicBlock> endSet = new HashSet<BasicBlock>();
			endSet.add(getEnd());
			return endSet;
		}

	}

	private static final String DOMINATION_PROJ = "python_test_code/domination";

	private Cfg graph;
	private Domination domAnalyser;
	private Postdomination postdomAnalyser;

	private MutableModel createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		return new DefaultModel(hierarchy);
	}

	public void initialise(String testFuncName) throws Throwable, Exception {
		MutableModel model = createTestModel(DOMINATION_PROJ);
		Function function = model.loadModule("my_module").getFunctions().get(
				testFuncName);
		assertTrue("No function " + testFuncName, function != null);

		graph = function.getCfg();

		domAnalyser = new Domination(graph);
		postdomAnalyser = new Postdomination(graph);
	}

	private void checkDomination(String[][] dominators) {
		checkStartEndDomination();
		new DominationGraphTest(dominators, graph, domAnalyser).run();
	}

	private void checkPostdomination(String[][] dominators) {
		checkStartEndPostdomination();
		new PostdominationGraphTest(dominators, graph, postdomAnalyser).run();
	}

	/**
	 * START dominates all blocks and END dominates nothing.
	 */
	private void checkStartEndDomination() {
		assertNotSame(graph.getStart(), graph.getEnd());

		for (BasicBlock block : graph.getBlocks()) {
			if (block != graph.getException())
				assertTrue(
						"START must dominate all other blocks except EXCEPTION",
						domAnalyser.dominates(graph.getStart(), block));
			if (block != graph.getEnd())
				assertFalse("END must not dominate anything except itself",
						domAnalyser.dominates(graph.getEnd(), block));
		}
	}

	/**
	 * END postdominates all blocks and START postdominates nothing.
	 */
	private void checkStartEndPostdomination() {
		assertNotSame(graph.getStart(), graph.getEnd());

		for (BasicBlock block : graph.getBlocks()) {
			if (block != graph.getException())
				assertTrue(
						"END must postdominate all other blocks except EXCEPTION",
						postdomAnalyser.dominates(graph.getEnd(), block));
			if (block != graph.getStart())
				assertFalse(
						"START must not postdominate anything execpt itself",
						postdomAnalyser.dominates(graph.getStart(), block));
		}
	}

	@Test
	public void testDom() throws Throwable {
		initialise("test_basic");

		String[][] dominators = { { "a", "b" }, { "a", "END" }, { "b", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "a", "START" },
				{ "b", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIf() throws Throwable {
		initialise("test_if");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "b", "c" },
				{ "a", "END" }, { "b", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "b", "a" }, { "a", "START" },
				{ "b", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomIfElse() throws Throwable {
		initialise("test_if_else");

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
		initialise("test_if_fallthru");

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
		initialise("test_if_else_fallthru");

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
		initialise("test_while");

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
		initialise("test_nested");

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
		initialise("test_nested_while_if");

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
		initialise("test_nested_while_if_break");

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
		initialise("test_nested_while_if_break_else");

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
		initialise("test_nested_whiles_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "c", "d" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "c", "b" }, { "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomNestedWhilesIfBreak() throws Throwable {
		initialise("test_nested_whiles_if_break");

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
		initialise("test_nested_whiles_break_fall");

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
		initialise("test_nested_ifs_break");

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
		initialise("test_if_else_break");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "a", "END" }, { "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "c" }, { "d", "a" }, { "d", "b" },
				{ "d", "c" }, { "a", "START" }, { "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomTwoprongedFallthoughToWhile() throws Throwable {
		initialise("test_twopronged_fallthrough_to_while");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "d", "e" }, { "a", "END" }, { "d", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "d", "a" }, { "d", "b" }, { "d", "c" },
				{ "d", "e" }, { "a", "START" }, { "d", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomWhileIfContinue1() throws Throwable {
		initialise("test_while_if_continue1");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "b", "c" },
				{ "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" },
				{ "a", "START" } };
		checkPostdomination(postdominators);
	}

	@Test
	public void testDomWhileIfContinue2() throws Throwable {
		initialise("test_while_if_continue2");

		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" } };
		checkDomination(dominators);

		String[][] postdominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "START" } };
		checkPostdomination(postdominators);
	}
}
