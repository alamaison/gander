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

		TestModule.assertResultIncludes(
				"Expression did not ignore tuple brackets.", test
						.printables("x flows through single-element tuple"),
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
				+ "parameters it wasn't passed to.", test.printables(
				"not b in g", "nor a in h"), result);
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
				+ "parameters it wasn't passed to.", test.printables(
				"not b in g", "nor a in h"), result);
	}

	@Test
	public void functionStararg() throws Throwable {

		TestModule test = newTestModule("function_stararg");

		Result<ModelSite<exprType>> result = solveBlastoff(test);

		TestModule.assertResultIsTop(
				"Call loses track of x as it flows into a tuple", result);
	}

	@Test
	public void constructorArgument() throws Throwable {

		TestModule test = newTestModule("constructor_argument");

		Result<ModelSite<exprType>> result = solveBlastoff(test);

		TestModule.assertResultIncludes("Call did not flow x to the expected "
				+ "parameters of the constructors in A, B and C.", test
				.printables("a in A", "a in B", "b in C"), result);

		TestModule
				.assertResultIncludes(
						"Call did not flow x to parameter "
								+ "of A's second constructor implementation that it aquired by "
								+ "assignment.",
						test.printables("also p in f"), result);

		TestModule.assertResultExcludes("Call shouldn't have flowed x to "
				+ "parameters it wasn't passed to.", test.printables(
				"not b in B", "nor a in C"), result);
	}

	@Test
	public void constructorKeywordArgument() throws Throwable {

		TestModule test = newTestModule("constructor_keyword_argument");

		Result<ModelSite<exprType>> result = solveBlastoff(test);

		TestModule.assertResultIncludes("Call did not flow x to the expected "
				+ "parameters of the constructors in A, B and C.", test
				.printables("a in A", "a in B", "b in C"), result);

		TestModule
				.assertResultIncludes(
						"Call did not flow x to parameter "
								+ "of A's second constructor implementation that it aquired by "
								+ "assignment.",
						test.printables("also a in f"), result);

		TestModule.assertResultExcludes("Call shouldn't have flowed x to "
				+ "parameters it wasn't passed to.", test.printables(
				"not b in B", "nor a in C"), result);
	}

	@Test
	public void constructorKeywordArgumentUntypable() throws Throwable {

		TestModule test = newTestModule("constructor_keyword_argument_untypable");

		Result<ModelSite<exprType>> result = solveBlastoff(test);

		TestModule.assertResultIncludes("Call did not flow x to the expected "
				+ "parameters of the decalred constructor.", test
				.printables("a in A"), result);

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
				+ "parameters of A's constructor.", test
				.printables("self in A"), result);

		TestModule.assertResultExcludes("Call shouldn't have flowed x to "
				+ "parameters it wasn't passed to.", test
				.printables("not a in A"), result);
	}

	@Test
	public void constructorArgumentSelfAssigned() throws Throwable {

		TestModule test = newTestModule("constructor_argument_self_assigned");

		Result<ModelSite<exprType>> result = solveBlastoff(test);

		TestModule.assertResultIncludes("Call did not flow instance to self "
				+ "parameters of A's constructors.", test.printables(
				"self in A", "also self in f"), result);

		TestModule.assertResultExcludes("Call shouldn't have flowed x to "
				+ "parameters it wasn't passed to.", test.printables(
				"not a in A", "nor p in f"), result);
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
