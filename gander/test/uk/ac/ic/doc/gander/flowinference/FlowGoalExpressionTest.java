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
	public void methodParameter() throws Throwable {

		TestModule test = newTestModule("method_parameter");

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
	public void methodParameterSelf() throws Throwable {

		TestModule test = newTestModule("method_parameter_self");

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

	private TestModule newTestModule(String testName) {
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
