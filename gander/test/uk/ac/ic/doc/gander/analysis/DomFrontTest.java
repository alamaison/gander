package uk.ac.ic.doc.gander.analysis;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.AbstractTaggedGraphTest;
import uk.ac.ic.doc.gander.analysis.dominance.DomFront;
import uk.ac.ic.doc.gander.analysis.dominance.DomMethod;
import uk.ac.ic.doc.gander.analysis.dominance.DomFront.DomInfo;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.MutableModel;

public class DomFrontTest {

	class DominationGraphTest extends AbstractTaggedGraphTest {

		private Map<BasicBlock, DomInfo> doms;

		public DominationGraphTest(String[][] expectedLinks, Cfg graph) {
			super(expectedLinks, graph, "Domination front");
			this.doms = new DomFront(new DomMethod(graph)).run();
		}

		@Override
		protected boolean areLinked(BasicBlock source, BasicBlock target) {
			return doms.get(source).dominanceFrontiers.contains(target);
		}

		@Override
		protected boolean selfLinkRequired() {
			return false;
		}

		@Override
		protected Set<BasicBlock> getLinkToAllBlocks() {
			Set<BasicBlock> startSet = new HashSet<BasicBlock>();
			startSet.add(getStart());
			return startSet;
		}

	}

	private static final String TEST_FOLDER = "python_test_code/domination/";
	private Cfg graph;

	private void initialise(String caseName) throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		File topLevelDirectory = new File(topLevel.toURI());

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		graph = new MutableModel(hierarchy).loadModule("my_module").getFunctions()
				.get("test_" + caseName).getCfg();
	}

	@Test
	public void testBasic() throws Throwable {
		initialise("basic");
		String[][] links = {};
		checkFrontier(links);
	}

	@Test
	public void testIf() throws Throwable {
		initialise("if");
		String[][] links = { { "c", "END" } };
		checkFrontier(links);
	}

	@Test
	public void testIfElse() throws Throwable {
		initialise("if_else");
		String[][] links = { { "c", "END" }, { "d", "END" } };
		checkFrontier(links);
	}

	@Test
	public void testIfFallthru() throws Throwable {
		initialise("if_else_fallthru");
		String[][] links = { { "c", "e" }, { "d", "e" } };
		checkFrontier(links);
	}

	@Test
	public void testWhile() throws Throwable {
		initialise("while");
		String[][] links = { { "b", "b" }, { "c", "b" } };
		checkFrontier(links);
	}

	@Test
	public void testNested() throws Throwable {
		initialise("nested");
		String[][] links = { { "c", "h" }, { "d", "d" }, { "d", "h" },
				{ "e", "d" }, { "f", "h" }, { "g", "h" } };
		checkFrontier(links);
	}

	@Test
	public void testNestedWhileIf() throws Throwable {
		initialise("nested_while_if");
		String[][] links = { { "a", "a" }, { "b", "a" }, { "d", "e" },
				{ "e", "a" } };
		checkFrontier(links);
	}

	@Test
	public void testNestedWhileIfBreak() throws Throwable {
		initialise("nested_while_if_break");
		String[][] links = { { "a", "a" }, { "b", "a" }, { "b", "END" },
				{ "d", "END" }, { "e", "a" } };
		checkFrontier(links);
	}

	@Test
	public void testTryExcept1() throws Throwable {
		initialise("try_except1");
		String[][] links = {};
		checkFrontier(links);
	}

	@Test
	public void testTryExcept3() throws Throwable {
		initialise("try_except3");
		String[][] links = { { "b", "END" }, { "c", "END" } };
		checkFrontier(links);
	}

	private void checkFrontier(String[][] expectedLinks) throws Exception {
		new DominationGraphTest(expectedLinks, graph).run();
	}
}
