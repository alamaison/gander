package uk.ac.ic.doc.gander.flowinference;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.flowinference.dda.Goal;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.MutableModel;

/**
 * Test of {@link FlowGoal} staring from an arbitrary expression.
 */
public class FlowGoalExpressionTest {
    private static final String TEST_FOLDER = "python_test_code/flow_goal_expression";

    private MutableModel model;

    @Test
    public void assignment() throws Throwable {

        TestModule test = newTestModule("assignment");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Assignment did not flow x to y.",
                test.printables("y"), result);

        TestModule.assertResultExcludes(
                "Assignment shouldn't have flowed x to z.",
                test.printables("z"), result);
    }

    @Test
    public void assignmentConditional() throws Throwable {

        TestModule test = newTestModule("assignment_conditional");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Assignment did not flow x to all "
                + "uses of y. It should because the analysis is "
                + "path-insensitive.", test.printables("y", "if", "else"),
                result);
    }

    @Test
    public void methodArgument() throws Throwable {

        TestModule test = newTestModule("method_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of method m in class A.", test.printables("a in A::m"),
                result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to a "
                + "in other methods.",
                test.printables("not a in A::n", "nor a in B"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x "
                + "to self anywhere.", test.printables("not self in A::m",
                "nor self in A::n", "nor self in B"), result);
    }

    @Test
    public void methodArgumentSelf() throws Throwable {

        TestModule test = newTestModule("method_argument_self");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Call did not flow instance of A to self "
                        + "parameter of its method m.",
                test.printables("self in A::m"), result);

        TestModule.assertResultIncludes(
                "Call did not flow instance of A to both "
                        + "parameters of its method n.",
                test.printables("self in A::n", "and, sneakily, p in A::n"),
                result);

        TestModule.assertResultExcludes(
                "Assignment shouldn't have flowed instance of "
                        + "A to p in A::m.", test.printables("not p in A::m"),
                result);

        TestModule.assertResultExcludes(
                "Assignment shouldn't have flowed instance of A "
                        + "to anywhere in B.",
                test.printables("not self in B", "nor p in B"), result);
    }

    @Test
    public void methodClosureArgument() throws Throwable {

        TestModule test = newTestModule("method_closure_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of method m in class A.", test.printables("a in A::m"),
                result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to a "
                + "in other methods.", test.printables("nor a in B"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x "
                + "to self anywhere.",
                test.printables("not self in A::m", "nor self in B"), result);
    }

    @Test
    public void methodClosureArgumentSelf() throws Throwable {

        TestModule test = newTestModule("method_closure_argument_self");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Call did not flow instance of A to self "
                        + "parameter of its method m.",
                test.printables("self in A::m"), result);

        TestModule.assertResultExcludes(
                "Assignment shouldn't have flowed instance of "
                        + "A to p in A::m.", test.printables("not p in A::m"),
                result);

        TestModule.assertResultExcludes(
                "Assignment shouldn't have flowed instance of A "
                        + "to anywhere in B.",
                test.printables("not self in B", "nor p in B"), result);
    }

    @Test
    public void methodInheritedArgument() throws Throwable {

        TestModule test = newTestModule("method_inherited_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of method m in inherited class A.",
                test.printables("a in A::m"), result);

        TestModule.assertResultExcludes(
                "Call shouldn't have flowed x to self.",
                test.printables("not self in A::m"), result);
    }

    @Test
    public void methodInheritedArgumentSelf() throws Throwable {

        TestModule test = newTestModule("method_inherited_argument_self");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Call did not flow instance of B to self "
                        + "parameter of A's method m.",
                test.printables("self in A::m"), result);

        TestModule.assertResultExcludes(
                "Shouldn't have flowed instance to a in A::m.",
                test.printables("not a in A::m"), result);
    }

    @Test
    public void methodInheritedArgumentRecursiveSuperName() throws Throwable {

        TestModule test = newTestModule("method_inherited_argument_recursive_super_name");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of method m in inherited class A.",
                test.printables("a in A::m"), result);

        TestModule.assertResultExcludes(
                "Call shouldn't have flowed x to self.",
                test.printables("not self in A::m"), result);
    }

    @Test
    public void tuple() throws Throwable {

        TestModule test = newTestModule("tuple");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering tuple", result);
    }

    @Test
    public void list() throws Throwable {

        TestModule test = newTestModule("list");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering list", result);
    }

    @Test
    public void listSubscript() throws Throwable {

        TestModule test = newTestModule("list_subscript");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering list", result);
    }

    // Only Python 2.7+ @Test
    public void set() throws Throwable {

        TestModule test = newTestModule("set");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering set", result);
    }

    @Test
    public void dictKey() throws Throwable {

        TestModule test = newTestModule("dict_key");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering dictionary", result);
    }

    @Test
    public void dictValue() throws Throwable {

        TestModule test = newTestModule("dict_value");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering dictionary", result);
    }

    @Test
    public void dictSubscriptKey() throws Throwable {

        TestModule test = newTestModule("dict_subscript_key");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering dictionary", result);
    }

    @Test
    public void dictSubscriptValue() throws Throwable {

        TestModule test = newTestModule("dict_subscript_value");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering dictionary", result);
    }

    @Test
    public void listComp() throws Throwable {

        TestModule test = newTestModule("list_comp");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering list comprehension",
                result);
    }

    @Test
    public void listCompNakedTuple() throws Throwable {

        TestModule test = newTestModule("list_comp_naked_tuple");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering list comprehension",
                result);
    }

    @Test
    public void listCompNakedTuple2() throws Throwable {

        TestModule test = newTestModule("list_comp_naked_tuple2");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on entering list comprehension",
                result);
    }

    @Test
    public void listCompIf() throws Throwable {

        /*
         * This test will fail until we find a better AST generator without the
         * listcomp bug that mistreats naked tuples.
         */

        TestModule test = newTestModule("list_comp_if");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsNotTop(
                "Flow should not have escaped.  x was only used in the "
                        + "if-condition.  It never enters the list", result);
    }

    @Test
    public void index() throws Throwable {

        TestModule test = newTestModule("index");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Flow should have escaped on being used as an index", result);
    }

    @Test
    public void indexNot() throws Throwable {

        TestModule test = newTestModule("index_not");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsNotTop(
                "Flow should NOT have escaped as something "
                        + "else entirely was being used as an index", result);

        TestModule.assertResultIncludes("Expression did not flow as expected.",
                test.printables("x flows here"), result);
    }

    @Test
    public void forLoop() throws Throwable {

        TestModule test = newTestModule("for_loop");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule
                .assertResultIsTop(
                        "Flow should have escaped on entering for-loop's tuple",
                        result);
    }

    @Test
    public void ifExpression() throws Throwable {

        TestModule test = newTestModule("if_expression");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Expression did not flow through "
                + "ternary if operator.", test.printables("x flows here"),
                result);
    }

    @Test
    public void raiseException() throws Throwable {

        TestModule test = newTestModule("raise_exception");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop("Flow should have escaped when raised",
                result);
    }

    @Test
    public void tupleSingleElement() throws Throwable {

        TestModule test = newTestModule("tuple_single_element");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule
                .assertResultIncludes(
                        "Expression did not ignore tuple brackets.",
                        test.printables("x flows through single-element tuple"),
                        result);
    }

    @Test
    public void tupleSingleElementTrailingComma() throws Throwable {

        TestModule test = newTestModule("tuple_single_element_trailing_comma");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule
                .assertResultIsTop("Expression ignore trailing comma", result);
    }

    @Test
    public void functionArgument() throws Throwable {

        TestModule test = newTestModule("function_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of functions f and g", test.printables("a in f", "a in g"),
                result);

        TestModule.assertResultIncludes("Call did not flow x to parameter b "
                + "of function h", test.printables("b in h"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not b in g", "nor a in h"), result);
    }

    @Test
    public void functionAsAttributeArgument() throws Throwable {

        TestModule test = newTestModule("function_as_attribute_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of function f", test.printables("a in f"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not a in g", "this method is a distraction"),
                result);
    }

    @Test
    public void functionKeywordArgument() throws Throwable {

        TestModule test = newTestModule("function_keyword_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to parameter a "
                + "of functions f and g", test.printables("a in f", "a in g"),
                result);

        TestModule.assertResultIncludes("Call did not flow x to parameter b "
                + "of function h", test.printables("b in h"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not b in g", "nor a in h"), result);
    }

    @Test
    public void functionParameterTuple() throws Throwable {

        TestModule test = newTestModule("function_parameter_tuple");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Call loses track of x as it flows into a tuple", result);
    }

    @Test
    public void functionParameterStarargs() throws Throwable {

        TestModule test = newTestModule("function_parameter_starargs");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Call loses track of x as it flows into a tuple", result);
    }

    @Test
    public void functionParameterKwargs() throws Throwable {

        TestModule test = newTestModule("function_parameter_kwargs");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "Call loses track of x as it flows into a dictionary", result);
    }

    @Test
    public void functionParameterExpandedIter() throws Throwable {

        TestModule test = newTestModule("function_parameter_expanded_iter");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultExcludes(
                "Iterable doesn't flow into function; only its contents",
                test.printables("not here", "nor here"), result);
    }

    @Test
    public void functionParameterExpandedMap() throws Throwable {

        TestModule test = newTestModule("function_parameter_expanded_map");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultExcludes(
                "Mapping doesn't flow into function; only its contents",
                test.printables("not here", "nor here"), result);
    }

    @Test
    public void constructorArgument() throws Throwable {

        TestModule test = newTestModule("constructor_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to the expected "
                + "parameters of the constructors in A, B and C.",
                test.printables("a in A", "a in B", "b in C"), result);

        TestModule
                .assertResultIncludes(
                        "Call did not flow x to parameter "
                                + "of A's second constructor implementation that it aquired by "
                                + "assignment.",
                        test.printables("also p in f"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not b in B", "nor a in C"), result);
    }

    /**
     * Test that inheriting from object doesn't do anything funny to the
     * constructor.
     */
    @Test
    public void constructorArgumentObject() throws Throwable {

        TestModule test = newTestModule("constructor_argument_object");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow instance to "
                + "parameters a of A's constructor.",
                test.printables("a in A"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "self parameter.", test.printables("not self in A"), result);
    }

    @Test
    public void constructorKeywordArgument() throws Throwable {

        TestModule test = newTestModule("constructor_keyword_argument");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to the expected "
                + "parameters of the constructors in A, B and C.",
                test.printables("a in A", "a in B", "b in C"), result);

        TestModule
                .assertResultIncludes(
                        "Call did not flow x to parameter "
                                + "of A's second constructor implementation that it aquired by "
                                + "assignment.",
                        test.printables("also a in f"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not b in B", "nor a in C"), result);
    }

    @Test
    public void constructorKeywordArgumentUntypable() throws Throwable {

        TestModule test = newTestModule("constructor_keyword_argument_untypable");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow x to the expected "
                + "parameters of the decalred constructor.",
                test.printables("a in A"), result);

        TestModule
                .assertResultIncludes(
                        "Call did not flow x to parameter "
                                + "of A's second constructor implementation that it aquired by "
                                + "assignment.",
                        test.printables("also p in f"), result);
    }

    @Test
    public void constructorArgumentSelf() throws Throwable {

        TestModule test = newTestModule("constructor_argument_self");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow instance to self "
                + "parameters of A's constructor.",
                test.printables("self in A"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not a in A"), result);
    }

    @Test
    public void constructorArgumentSelfAssigned() throws Throwable {

        TestModule test = newTestModule("constructor_argument_self_assigned");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes("Call did not flow instance to self "
                + "parameters of A's constructors.",
                test.printables("self in A", "also self in f"), result);

        TestModule.assertResultExcludes("Call shouldn't have flowed x to "
                + "parameters it wasn't passed to.",
                test.printables("not a in A", "nor p in f"), result);
    }

    @Test
    public void objectWithFunctionMember() throws Throwable {

        TestModule test = newTestModule("object_with_function_member");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultExcludes("Call must not flow the object into "
                + "function f", test.printables("a in f"), result);
    }

    @Test
    public void objectWithClassMember() throws Throwable {

        TestModule test = newTestModule("object_with_class_member");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultExcludes("Call must not flow the object into "
                + "the member class's constructor",
                test.printables("self in B::A", "a in B::A"), result);

        TestModule.assertResultExcludes("Call must not flow the object into "
                + "an unrelated class", test.printables("self in A", "a in A"),
                result);
    }

    @Test
    public void importStar() throws Throwable {

        TestModule test = newTestModule("import_star");
        TestModule test_aux = newTestModule("import_star_aux");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Star-imported class not flowed properly",
                test_aux.printables("imported by *"), result);

        TestModule.assertResultExcludes(
                "Star-imported class must not only flow to matching names",
                test_aux.printables("imported by * but different object"),
                result);
        TestModule.assertResultExcludes(
                "Star-imported class must not affect other "
                        + "non-star imported names",
                test_aux.printables("not imported by *"), result);
    }

    @Test
    public void importStarIndirect() throws Throwable {

        TestModule test = newTestModule("import_star_indirect");
        TestModule test_aux = newTestModule("import_star_indirect_aux");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Star-imported value not flowed properly",
                test_aux.printables("imported by *"), result);

        TestModule.assertResultExcludes(
                "Star-imported value must not only flow to matching names",
                test_aux.printables("imported by * but different object"),
                result);
        TestModule.assertResultExcludes(
                "Star-imported class must not affect other "
                        + "non-star imported names",
                test_aux.printables("not imported by *"), result);
    }

    @Test
    public void classNonLexicalBinding() throws Throwable {

        TestModule test = newTestModule("class_non_lexical_binding");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "Global should have flowed into class namespace.",
                test.printables("the global ends up in A's namespace"), result);

        TestModule.assertResultExcludes("Global should not have flowed into "
                + "class namespace as it wasn't assigned.", test
                .printables("global doesn't get into a "
                        + "class namespace unless it was assigned"), result);
    }

    @Test
    public void getattrMethod() throws Throwable {

        TestModule test = newTestModule("getattr_method");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "getattr should cause us to lose all ability to track the flow "
                        + "of an object's attributes.", result);
    }

    @Test
    public void getattrFunction() throws Throwable {

        TestModule test = newTestModule("getattr_function");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIsTop(
                "getattr should cause us to lose all ability to track the flow "
                        + "of an object's attributes.", result);
    }

    @Test
    public void builtIn() throws Throwable {

        TestModule test = newTestModule("built_in");

        Result<ModelSite<exprType>> result = solveBlastoff(test);

        TestModule.assertResultIncludes(
                "x did not flow through builtin function",
                test.printables("x flows here"), result);
    }

    private TestModule newTestModule(String testName) throws Throwable {
        return new TestModule(testName, model);
    }

    @Before
    public void setup() throws Throwable {
        model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
    }

    private Result<ModelSite<exprType>> solveFlowGoal(FlowPosition position) {

        Goal<Result<ModelSite<exprType>>> rootGoal = new FlowGoal(position);
        GoalSolver<Result<ModelSite<exprType>>> solver = GoalSolver
                .newInstance(rootGoal, new KnowledgeBase());
        return solver.solve();
    }

    private Result<ModelSite<exprType>> solveBlastoff(TestModule test)
            throws Throwable {
        return solveFlowGoal(new ExpressionPosition(test.printNode("blastoff")
                .site()));
    }

}
