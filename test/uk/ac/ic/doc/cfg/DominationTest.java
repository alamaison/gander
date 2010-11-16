package uk.ac.ic.doc.cfg;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.cfg.model.BasicBlock;
import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

import static org.junit.Assert.*;

public class DominationTest {

	private static final String DOMINATION_PROJ = "python_test_code/domination";

	private Domination domAnalyser;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialise(String testFuncName) throws Throwable, Exception {
		Model model = createTestModel(DOMINATION_PROJ);
		Function function = model.getTopLevelPackage().getModules()
				.get("my_module").getFunctions().get(testFuncName);
		assertTrue("No function " + testFuncName, function != null);

		Cfg graph = function.getCfg();

		BasicBlock start = graph.getStart();
		domAnalyser = new Domination(start);
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

	private void checkDomination(Map<String, Collection<String>> dominators) {

		Set<BasicBlock> domBlocks = new HashSet<BasicBlock>(); // for non-dom
																// test

		Set<BasicBlock> allBlocks = domAnalyser.getBlocks();
		BasicBlock start = findStartBlock(allBlocks);
		BasicBlock end = findEndBlock(allBlocks);

		assertNotSame(start, end);

		// START dominates all blocks and END dominates nothing
		for (BasicBlock block : allBlocks) {
			assertTrue("START must dominate all other blocks",
					domAnalyser.dominates(start, block));
			if (block != end)
				assertFalse("END must not dominate anything execpt itself",
						domAnalyser.dominates(end, block));
		}

		// Check dominators. Each dominator block should link to its
		// expected submissive blocks but no other
		for (String dom : dominators.keySet()) {
			BasicBlock domBlock = findBlockContainingCall(dom);

			Set<BasicBlock> nonSubBlocks = new HashSet<BasicBlock>(allBlocks);

			for (String sub : dominators.get(dom)) {
				BasicBlock subBlock;
				if (sub.equals("END"))
					subBlock = end;
				else
					subBlock = findBlockContainingCall(sub);

				assertTrue("Call to function " + dom + "() must dominate "
						+ "call to " + sub + "() but analysis says that it"
						+ " doesn't", domAnalyser.dominates(domBlock, subBlock));

				nonSubBlocks.remove(subBlock);
			}

			for (BasicBlock block : nonSubBlocks) {
				boolean hasDominanceRelation = domAnalyser.dominates(domBlock,
						block);
				if (block == domBlock)
					assertTrue("All blocks must dominate themselves",
							hasDominanceRelation);
				else
					assertFalse("Block containing call to function " + dom
							+ "() has an unexpected domination:\n" + domBlock
							+ " dominates " + block, hasDominanceRelation);
			}

			domBlocks.add(domBlock);
		}

		// Then check that all other blocks (except START) are non-dominating
		Set<BasicBlock> nonDomBlocks = new HashSet<BasicBlock>(allBlocks);
		nonDomBlocks.removeAll(domBlocks);
		nonDomBlocks.remove(start);
		for (BasicBlock nonDomBlock : nonDomBlocks) {
			// Shouldn't dominate anything but itself
			for (BasicBlock block : allBlocks) {
				boolean hasDominanceRelation = domAnalyser.dominates(
						nonDomBlock, block);
				if (block == nonDomBlock)
					assertTrue("All blocks must dominate themselves",
							hasDominanceRelation);
				else
					assertFalse("Unexpected domination:\n" + nonDomBlock
							+ " dominates " + block, hasDominanceRelation);

			}
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
			if (block.getOutSet().isEmpty()) {
				assertTrue("Multiple END blocks found", end == null);
				end = block;
			}
		}

		assertTrue("No END block found", end != null);

		return end;
	}

	// You can have more than one different tag in a basic block but not more
	// than one basic block containing the same tag.
	private BasicBlock findBlockContainingCall(String tag) {

		Collection<BasicBlock> blocks = domAnalyser.getBlocks();
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
	}

	@Test
	public void testDomIf() throws Throwable {
		initialise("dom_if");
		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "b", "c" },
				{ "a", "END" }, { "b", "END" } };
		checkDomination(dominators);
	}

	@Test
	public void testDomIfElse() throws Throwable {
		initialise("dom_if_else");
		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" }, { "a", "END" },
				{ "b", "END" } };
		checkDomination(dominators);
	}

	@Test
	public void testDomIfFallthru() throws Throwable {
		initialise("dom_if_fallthru");
		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "a", "END" }, { "b", "END" },
				{ "d", "END" } };
		checkDomination(dominators);
	}

	@Test
	public void testDomIfElseFallthru() throws Throwable {
		initialise("dom_if_else_fallthru");
		String[][] dominators = { { "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "b", "c" }, { "b", "d" }, { "b", "e" },
				{ "a", "END" }, { "b", "END" }, { "e", "END" } };
		checkDomination(dominators);
	}

	@Test
	public void testDomWhile() throws Throwable {
		initialise("dom_while");
		String[][] dominators = {
				{ "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" },
				{ "a", "END" }, { "b", "END" }, { "d", "END" } };
		checkDomination(dominators);
	}

	@Test
	public void testDomNested() throws Throwable {
		initialise("dom_nested");
		String[][] dominators = {
				{ "a", "b" }, { "a", "c" }, { "a", "d" },
				{ "a", "e" }, { "a", "f" }, { "a", "g" }, { "a", "h" },
				{ "b", "c" }, { "b", "d" }, { "b", "e" }, { "b", "f" },
				{ "b", "g" }, { "b", "h" },
				{ "c", "d" }, { "c", "d" }, { "c", "e" }, { "c", "f" },
				{ "d", "e" }, { "d", "f" },
				{ "a", "END" }, { "b", "END" }, { "h", "END" } };
		checkDomination(dominators);
	}
}
