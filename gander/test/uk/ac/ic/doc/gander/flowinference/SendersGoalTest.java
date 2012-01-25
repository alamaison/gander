package uk.ac.ic.doc.gander.flowinference;

import org.junit.Before;
import org.junit.Test;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.RelativeTestModelCreator;
import uk.ac.ic.doc.gander.flowinference.dda.GoalSolver;
import uk.ac.ic.doc.gander.flowinference.dda.KnowledgeBase;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public class SendersGoalTest {
	private static final String TEST_FOLDER = "python_test_code/senders_goal";

	private MutableModel model;

	@Test
	public void directFunctionCall() throws Throwable {

		TestModule test = newTestModule("direct_function_call");

		Result<ModelSite<Call>> result = solveSendersGoal(test
				.moduleLevelFunction("f"));

		TestModule.assertResultIncludes("Callsite not recognised as a sender.",
				test.printables("calls f"), result);

		TestModule.assertResultExcludes("Call to g incorrectly included as a "
				+ "possible caller of f.", test.printables("doesn't call f"),
				result);
	}

	@Test
	public void aliasedFunctionCall() throws Throwable {

		TestModule test = newTestModule("aliased_function_call");

		Result<ModelSite<Call>> result = solveSendersGoal(test
				.moduleLevelFunction("f"));

		TestModule.assertResultIncludes("Callsite not recognised as a sender.",
				test.printables("calls f"), result);

		TestModule.assertResultExcludes("Call to g incorrectly included as a "
				+ "possible caller of f.", test.printables("doesn't call f"),
				result);
	}

	@Test
	public void indirectFunctionCall() throws Throwable {

		TestModule test = newTestModule("indirect_function_call");

		Result<ModelSite<Call>> result = solveSendersGoal(test
				.moduleLevelFunction("f"));

		TestModule.assertResultExcludes("Call that *passes* f incorrectly "
				+ "included as call *to* f.",
				test.printables("this doesn't call f"), result);

		TestModule.assertResultIncludes("Call on parameter of function "
				+ "site not recognised as a sender to f.",
				test.printables("this calls f"), result);
	}

	private TestModule newTestModule(String testName) {
		return new TestModule(testName, model);
	}

	@Before
	public void setup() throws Throwable {
		model = new RelativeTestModelCreator(TEST_FOLDER, this).getModel();
	}

	private Result<ModelSite<Call>> solveSendersGoal(InvokableCodeObject callable) {

		FunctionSendersGoal rootGoal = new FunctionSendersGoal(callable);
		GoalSolver<Result<ModelSite<Call>>> solver = GoalSolver.newInstance(
				rootGoal, new KnowledgeBase());
		return solver.solve();
	}
}
