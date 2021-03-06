package uk.ac.ic.doc.gander.cfg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import uk.ac.ic.doc.gander.cfg.Cfg;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.MutableModel;

public class CfgTest2 {

    private static final String CONTROL_FLOW_PROJ = "python_test_code/control_flow";

    private Cfg graph;

    private MutableModel createTestModel(String projectPath) throws Throwable {
        URL topLevel = getClass().getResource(projectPath);

        File topLevelDirectory = new File(topLevel.toURI());
        Hierarchy hierarchy = HierarchyFactory
                .createHierarchy(topLevelDirectory);
        MutableModel model = new DefaultModel(hierarchy);
        return model;
    }

    public void initialise(String testFuncName) throws Throwable, Exception {
        MutableModel model = createTestModel(CONTROL_FLOW_PROJ);
        Function function = model.loadModule("my_module2").getFunctions().get(
                testFuncName);
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
    public void testIfPass() throws Throwable {
        initialise("test_if_pass");

        String[][] graph = { { "START", "a" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testIfElsePass() throws Throwable {
        initialise("test_if_else_pass");

        String[][] graph = { { "START", "a" }, { "a", "c" }, { "a", "b" },
                { "b", "c" }, { "c", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testIfElsePassPass() throws Throwable {
        initialise("test_if_else_pass_pass");

        String[][] graph = { { "START", "a" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testWhilePass() throws Throwable {
        initialise("test_while_pass");

        String[][] graph = { { "START", "a" }, { "a", "a" }, { "a", "END" } };
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

        String[][] graph = { { "START", "a" }, { "a", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testRaise3() throws Throwable {
        initialise("test_raise3");

        String[][] graph = { { "START", "a" }, { "b", "c" },
                { "c", "EXCEPTION" }, { "b", "d" }, { "d", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExcept1() throws Throwable {
        initialise("test_try_except1");

        String[][] graph = { { "START", "a" }, { "a", "b" },
                { "a", "EXCEPTION" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExcept2() throws Throwable {
        initialise("test_try_except2");

        String[][] graph = { { "START", "a" }, { "b", "c" },
                { "b", "EXCEPTION" }, { "c", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExcept3() throws Throwable {
        initialise("test_try_except3");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "END" },
                { "b", "EXCEPTION" }, { "b", "c" }, { "c", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExcept4() throws Throwable {
        initialise("test_try_except4");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "END" },
                { "b", "EXCEPTION" }, { "b", "c" }, { "b", "d" },
                { "c", "END" }, { "d", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptAll() throws Throwable {
        initialise("test_try_except_all");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "END" },
                { "b", "c" }, { "b", "d" }, { "c", "END" }, { "d", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptRaise() throws Throwable {
        initialise("test_try_except_raise");

        String[][] graph = { { "START", "a" }, { "a", "EXCEPTION" },
                { "a", "b" }, { "b", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptAllRaise() throws Throwable {
        initialise("test_try_except_all_raise");

        String[][] graph = { { "START", "a" }, { "a", "b" },
                { "b", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptElse1() throws Throwable {
        initialise("test_try_except_else1");

        String[][] graph = { { "START", "a" }, { "a", "EXCEPTION" },
                { "a", "b" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptElse2() throws Throwable {
        initialise("test_try_except_else2");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "d" },
                { "b", "EXCEPTION" }, { "b", "c" }, { "c", "END" },
                { "d", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptElse3() throws Throwable {
        initialise("test_try_except_else3");

        String[][] graph = { { "START", "a" }, { "a", "c" }, { "c", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptElseRaise() throws Throwable {
        initialise("test_try_except_else_raise");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "d" },
                { "b", "EXCEPTION" }, { "b", "c" }, { "c", "END" },
                { "d", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptEmpty() throws Throwable {
        initialise("test_try_except_empty");

        String[][] graph = { { "START", "b" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptEmptyRaise() throws Throwable {
        initialise("test_try_except_empty_raise");

        String[][] graph = { { "START", "a" }, { "START", "EXCEPTION" },
                { "a", "b" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryExceptEmptyElse() throws Throwable {
        initialise("test_try_except_empty_else");

        String[][] graph = { { "START", "b" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally1() throws Throwable {
        initialise("test_try_finally1");

        String[][] graph = { { "START", "a" }, { "a", "c" }, { "a", "b" },
                { "b", "c" }, { "c", "d" }, { "c", "e" }, { "d", "EXCEPTION" },
                { "d", "END" }, { "e", "EXCEPTION" }, { "e", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally2() throws Throwable {
        initialise("test_try_finally2");

        String[][] graph = { { "START", "a" }, { "a", "c" }, { "a", "b" },
                { "b", "c" }, { "c", "d" }, { "c", "e" }, { "d", "END" },
                { "e", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally3() throws Throwable {
        initialise("test_try_finally3");

        String[][] graph = { { "START", "a" }, { "a", "c" }, { "a", "b" },
                { "b", "c" }, { "c", "d" }, { "c", "e" }, { "d", "END" },
                { "e", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally4() throws Throwable {
        initialise("test_try_finally4");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
                { "b", "d" }, { "c", "d" }, { "d", "e" }, { "d", "f" },
                { "e", "END" }, { "e", "EXCEPTION" }, { "f", "END" },
                { "f", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally5() throws Throwable {
        initialise("test_try_finally5");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "b", "c" },
                { "c", "d" }, { "c", "e" }, { "d", "a" }, { "d", "END" },
                { "e", "a" }, { "e", "END" }, { "a", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTryFinally6() throws Throwable {
        initialise("test_try_finally6");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "c" },
                { "b", "d" }, { "c", "d" }, { "d", "e" }, { "d", "f" },
                { "e", "EXCEPTION" }, { "f", "EXCEPTION" } };
        checkControlFlow(graph);
    }

    @Test
    public void testTrickyTryExcept() throws Throwable {
        initialise("test_tricky_try_except");

        String[][] graph = { { "START", "a" }, { "a", "b" }, { "a", "d" },
                { "b", "c" }, { "b", "a" }, { "c", "d" }, { "d", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testDoubleEmptyBody() throws Throwable {
        initialise("test_double_empty_body");

        String[][] graph = { { "START", "a" }, { "a", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testPrint() throws Throwable {
        initialise("test_print");

        String[][] graph = { { "START", "a" }, { "a", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testMultiprint() throws Throwable {
        initialise("test_multiprint");

        String[][] graph = { { "START", "a" }, { "b", "END" } };
        checkControlFlow(graph);
    }

    @Test
    public void testPrintContinuingSameBlock() throws Throwable {
        initialise("test_print_continuing_same_block");

        String[][] graph = { { "START", "a" }, { "c", "END" } };
        checkControlFlow(graph);
    }
}
