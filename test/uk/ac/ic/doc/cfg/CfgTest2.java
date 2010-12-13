package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.cfg.model.Cfg;
import uk.ac.ic.doc.cfg.model.Function;

public class CfgTest2 {

	private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";

	private Cfg graph;

	private Model createTestModel(String projectPath) throws Throwable {
		URL topLevel = getClass().getResource(projectPath);

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		return model;
	}

	public void initialise(String testFuncName) throws Throwable, Exception {
		Model model = createTestModel(CONTROL_FLOW_PROJ);
		Function function = model.getTopLevelPackage().getModules()
				.get("my_module2").getFunctions().get(testFuncName);
		assertTrue("No function " + testFuncName, function != null);

		graph = function.getCfg();
	}

	private void checkControlFlow(String[][] dominators) {

		new ControlFlowGraphTest(dominators, graph).run();
	}

	@Test
	public void testCfg() throws Throwable {
		initialise("test_basic");

		String[][] graph = { { "START", "a" }, { "b", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIf() throws Throwable {
		initialise("test_if");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "END" },
				{ "c", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElse() throws Throwable {
		initialise("test_if_else");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "END" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfFallthru() throws Throwable {
		initialise("test_if_fallthru");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "d" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElseFallthru() throws Throwable {
		initialise("test_if_else_fallthru");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "e" }, { "d", "e" }, { "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhile() throws Throwable {
		initialise("test_while");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "b" }, { "b", "d" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNested() throws Throwable {
		initialise("test_nested");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "g" },
				{ "c", "d" }, { "d", "e" }, { "e", "d" }, { "d", "f" },
				{ "f", "h" }, { "g", "h" }, { "h", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIf() throws Throwable {
		initialise("test_nested_while_if");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "e" }, { "e", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIfBreak() throws Throwable {
		initialise("test_nested_while_if_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "END" }, { "e", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhileIfBreakElse() throws Throwable {
		initialise("test_nested_while_if_break_else");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "d" },
				{ "c", "e" }, { "d", "END" }, { "e", "f" }, { "f", "a" },
				{ "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesBreak() throws Throwable {
		initialise("test_nested_whiles_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "d", "a" }, { "c", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesIfBreak() throws Throwable {
		initialise("test_nested_whiles_if_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "c", "a" }, { "e", "a" }, { "e", "c" },
				{ "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedWhilesBreakFall() throws Throwable {
		initialise("test_nested_whiles_break_fall");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "d" }, { "c", "a" }, { "d", "a" }, { "a", "e" },
				{ "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgNestedIfsBreak() throws Throwable {
		initialise("test_nested_ifs_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "a" },
				{ "b", "c" }, { "c", "d" }, { "c", "e" }, { "d", "a" },
				{ "a", "e" }, { "e", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgIfElseBreak() throws Throwable {
		initialise("test_if_else_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "d" },
				{ "b", "c" }, { "b", "d" }, { "c", "a" }, { "a", "d" },
				{ "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgTwoprongedFallthoughToWhile() throws Throwable {
		initialise("test_twopronged_fallthrough_to_while");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
				{ "b", "d" }, { "c", "d" }, { "d", "e" }, { "e", "d" },
				{ "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhileIfContinue1() throws Throwable {
		initialise("test_while_if_continue1");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "b", "a" }, { "c", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCfgWhileIfContinue2() throws Throwable {
		initialise("test_while_if_continue2");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "a" }, { "b", "d" }, { "d", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testReturn() throws Throwable {
		initialise("test_return");

		String[][] graph = { { "START", "a" }, { "b", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testReturnVal() throws Throwable {
		initialise("test_return_val");

		String[][] graph = { { "START", "a" }, { "b", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testCondReturn() throws Throwable {
		initialise("test_cond_return");

		String[][] graph = { { "START", "a" }, { "b", "c" }, { "b", "d" },
				{ "c", "END" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testWhileReturn() throws Throwable {
		initialise("test_while_return");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "b", "d" }, { "c", "END" }, { "d", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testWhileCondReturn() throws Throwable {
		initialise("test_while_cond_return");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "b", "f" }, { "d", "END" }, { "d", "e" }, { "e", "b" },
				{ "f", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testMultipleReturn() throws Throwable {
		initialise("test_multiple_return");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
				{ "b", "END" }, { "c", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testMultipleReturn2() throws Throwable {
		initialise("test_multiple_return2");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
				{ "b", "END" }, { "c", "d" }, { "e", "END" }, { "e", "c" },
				{ "c", "f" }, { "f", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testFor() throws Throwable {
		initialise("test_for");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
				{ "c", "b" }, { "b", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testForBreak() throws Throwable {
		initialise("test_for_break");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "c", "a" },
				{ "c", "END" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testForContinue() throws Throwable {
		initialise("test_for_continue");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "a" },
				{ "b", "c" }, { "c", "a" }, { "a", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testYield1() throws Throwable {
		initialise("test_yield1");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "END" },
				{ "b", "c" }, { "d", "b" } };

		// TODO: Use correct graph
		// String[][] graph = { { "START", "a" }, { "START", "d" }, { "a", "b"
		// },
		// { "b", "END" }, { "b", "c" }, { "c", "END" }, { "d", "b" } };
		checkControlFlow(graph);
	}

	@Test
	public void testYield2() throws Throwable {
		initialise("test_yield2");

		String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "END" },
				{ "b", "c" }, { "c", "b" } };

		// TODO: Use correct graph
		// String[][] graph = { { "START", "a" }, { "START", "b" }, { "a", "b"
		// },
		// { "b", "END" }, { "b", "c" }, { "c", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testPass() throws Throwable {
		initialise("test_pass");

		String[][] graph = { { "START", "END" } };
		checkControlFlow(graph);
	}

	@Test
	public void testRaise1() throws Throwable {
		initialise("test_raise1");

		String[][] graph = { { "START", "EXCEPTION" } };
		checkControlFlow(graph);
	}

	@Test
	public void testRaise2() throws Throwable {
		initialise("test_raise2");

		String[][] graph = { { "START", "a" }, { "b", "EXCEPTION" },
				{ "b", "c" }, { "c", "END" } };
		checkControlFlow(graph);
	}
}
