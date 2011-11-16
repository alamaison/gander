package uk.ac.ic.doc.gander.analysis.signatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.signatures.PassedVariableFinder;
import uk.ac.ic.doc.gander.analysis.signatures.PassedVariableFinder.PassedVar;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;

public class PassedVariableFinderTest {

	private static final String TEST_PROJ = "../python_test_code/passed_var";
	private Set<BasicBlock> blocks;

	@Test
	public void testPassSinglePosition() throws Throwable {
		initialise("pass_single_position");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_single_parm", positions,
				keywords);
	}

	@Test
	public void testPassSinglePositionTwiceSameParm() throws Throwable {
		initialise("pass_single_position_twice_same_parm");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(2, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};

		for (PassedVar spec : calls) {
			checkSpec(spec, "func_with_single_parm", positions, keywords);
		}
	}

	@Test
	public void testPassSinglePositionTwiceDifferentParm() throws Throwable {
		initialise("pass_single_position_twice_different_parm");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_single_parm", positions,
				keywords);

		calls = new PassedVariableFinder("y", blocks).passes();
		assertEquals(1, calls.size());

		checkSpec(calls.iterator().next(), "func_with_single_parm", positions,
				keywords);
	}

	@Test
	public void testPassSinglePositionTwiceDifferentFunc() throws Throwable {
		initialise("pass_single_position_twice_different_func");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(2, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};
		Iterator<PassedVar> specs = calls.iterator();
		checkSpec(specs.next(), "func_with_single_parm", positions, keywords);
		checkSpec(specs.next(), "func_with_single_parm2", positions, keywords);
	}

	@Test
	public void testPassSinglePositionTwiceDifferentParmDifferentFunc()
			throws Throwable {
		initialise("pass_single_position_twice_different_parm_different_func");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_single_parm", positions,
				keywords);

		calls = new PassedVariableFinder("y", blocks).passes();
		assertEquals(1, calls.size());

		checkSpec(calls.iterator().next(), "func_with_single_parm2", positions,
				keywords);
	}

	@Test
	public void testIgnorePassingCompoundExpressions() throws Throwable {
		initialise("ignore_passing_compound_expressions");

		PassedVariableFinder finder = new PassedVariableFinder("y", blocks);
		Set<PassedVar> calls = finder.passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_single_parm2", positions,
				keywords);
	}

	@Test
	public void testPassTwoPosition() throws Throwable {
		initialise("pass_two_position");

		Set<PassedVar> calls = new PassedVariableFinder("y", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positionsy = { 1 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_two_parms", positionsy,
				keywords);

		calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positionsx = { 0 };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positionsx,
				keywords);
	}

	@Test
	public void testPassSameVarTwicePosition() throws Throwable {
		initialise("pass_same_var_twice_position");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0, 1 };
		String[] keywords = {};
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywords);
	}

	@Test
	public void testPassSingleKeyword() throws Throwable {
		initialise("pass_single_keyword");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = {};
		String[] keywords = { "z" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywords);
	}

	@Test
	public void testPassTwoKeywordsUsualOrder() throws Throwable {
		initialise("pass_two_keywords_usual_order");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = {};
		String[] keywordsx = { "p" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywordsx);

		calls = new PassedVariableFinder("y", blocks).passes();
		assertEquals(1, calls.size());

		String[] keywordsy = { "q" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywordsy);
	}

	@Test
	public void testPassTwoKeywordsOutOfOrder() throws Throwable {
		initialise("pass_two_keywords_out_of_order");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = {};
		String[] keywordsx = { "q" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywordsx);

		calls = new PassedVariableFinder("y", blocks).passes();
		assertEquals(1, calls.size());

		String[] keywordsy = { "p" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywordsy);
	}

	@Test
	public void testPassSameVarTwiceKeyword() throws Throwable {
		initialise("pass_same_var_twice_keyword");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = {};
		String[] keywords = { "q", "p" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywords);
	}

	@Test
	public void testPassSameVarTwiceMixed() throws Throwable {
		initialise("pass_same_var_twice_mixed");

		Set<PassedVar> calls = new PassedVariableFinder("x", blocks).passes();
		assertEquals(1, calls.size());

		Integer[] positions = { 0 };
		String[] keywords = { "q" };
		checkSpec(calls.iterator().next(), "func_with_two_parms", positions,
				keywords);
	}

	private void checkSpec(PassedVar spec, String functionName,
			Integer[] positions, String[] keywords) {
		Call call = spec.getCall();
		assertTrue("Function is not a simple variable name",
				call.func instanceof Name);
		assertEquals(new HashSet<Integer>(Arrays.asList(positions)), spec
				.getPositions());
		assertEquals(new HashSet<String>(Arrays.asList(keywords)), spec
				.getKeywords());
	}

	private MutableModel createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Hierarchy hierarchy = HierarchyFactory
				.createHierarchy(topLevelDirectory);
		return new DefaultModel(hierarchy);
	}

	private void initialise(String testFuncName) throws Throwable, Exception {
		MutableModel model = createTestModel(TEST_PROJ);
		Function function = model.loadModule("passed_var").getFunctions().get(
				testFuncName);
		assertTrue("No function " + testFuncName, function != null);

		blocks = function.getCfg().getBlocks();
	}
}
