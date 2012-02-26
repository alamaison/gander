package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Model how a value can flow purely by virtue of being the result of a call.
 * 
 * This does not include how it can flow by being assigned (or otherwise bound)
 * to another expression. It only includes flow that happens through the very
 * act of being a call result. The only example of this is if the call was a
 * constructor call when the value flows to the {@code self} parameter of the
 * class's methods.
 */
final class CallResultSituation implements FlowSituation {

	private final ModelSite<Call> expression;

	CallResultSituation(ModelSite<Call> expression) {
		this.expression = expression;
	}

	public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
		return new CallResultSituationSolver(expression, goalManager)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallResultSituation other = (CallResultSituation) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallResultSituation [expression=" + expression + "]";
	}

}

final class CallResultSituationSolver {

	private final Result<FlowPosition> solution;

	CallResultSituationSolver(ModelSite<Call> expression,
			SubgoalManager goalManager) {

		Result<Type> types = goalManager
				.registerSubgoal(new ExpressionTypeGoal(
						new ModelSite<exprType>(expression.astNode().func,
								expression.codeObject())));

		Concentrator<Type, FlowPosition> action = Concentrator.newInstance(
				new CallResultFlower(goalManager), TopFp.INSTANCE);
		types.actOnResult(action);
		solution = action.result();
	}

	public Result<FlowPosition> solution() {
		return solution;
	}
}

/**
 * Flows the value produced by the act of calling an object.
 */
final class CallResultFlower implements DatumProcessor<Type, FlowPosition> {

	private final SubgoalManager goalManager;

	CallResultFlower(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	public Result<FlowPosition> process(Type callable) {

		if (callable instanceof TCallable) {
			return ((TCallable) callable)
					.flowPositionsCausedByCalling(goalManager);
		} else {
			System.err.println("May call uncallable object:" + callable);
			return FiniteResult.bottom();
		}

	}
}
