package uk.ac.ic.doc.gander.flowinference;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectDefinitionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Test of {@link FlowGoal} starting from a code object.
 */
public class FlowGoalCodeObjectTest {
	private static final String TEST_FOLDER = "python_test_code/flow_goal_code_object";

	private MutableModel model;

	@Test
	public void localScope() throws Throwable {

		TestModule test = newTestModule("local_scope");

		Result<ModelSite<exprType>> result = solveCodeObjectGoal(test
				.moduleLevelFunction("f"));

		TestModule.assertResultIncludes("f's declaration should flow to any "
				+ "variable x in its parent's binding scope.",
				test.printables("f flows here", "f also flows here"), result);

		TestModule.assertResultExcludes(
				"f's declaration mustn't flow to variables addressing "
						+ "a different code object.",
				test.printables("g flows here, not f"), result);
	}

	@Test
	public void passedAsArgument() throws Throwable {

		TestModule test = newTestModule("passed_as_argument");

		Result<ModelSite<exprType>> result = solveCodeObjectGoal(test
				.moduleLevelFunction("f"));

		TestModule.assertResultExcludes(
				"f's parameter mustn't be confused with f.", test.printables(
						"this is the result of f but is not f", "x is not f"),
				result);

		TestModule.assertResultExcludes(
				"f's declaration mustn't be confused with the "
						+ "function it is passed to.", test.printables(
						"this passes f but isn't f", "this is also not f"),
				result);

		TestModule
				.assertResultExcludes(
						"f's result mustn't be confused with f.",
						test.printables("this is the result of f but is not f"),
						result);

		TestModule.assertResultIncludes("f's declaration should flow "
				+ "only through the call to the parameter of 'call_callable'.",
				test.printables("only this parameter is f"), result);
	}

	private TestModule newTestModule(String testName) {
		return new TestModule(testName, model);
	}

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
	}

	private Result<ModelSite<exprType>> solveCodeObjectGoal(
			CodeObject codeObject) {

		FlowGoal rootGoal = new FlowGoal(new CodeObjectDefinitionPosition(
				codeObject));
		GoalSolver<Result<ModelSite<exprType>>> solver = GoalSolver
				.newInstance(rootGoal, new KnowledgeBase());
		return solver.solve();
	}

}
