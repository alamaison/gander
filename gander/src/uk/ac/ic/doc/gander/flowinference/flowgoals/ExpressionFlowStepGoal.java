package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations.FlowSituation;
import uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations.FlowSituationFinder;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Finds the next step of an expression's flow based on its flow situation.
 */
final class ExpressionFlowStepGoal<T extends exprType> implements FlowStepGoal {

	private final ModelSite<T> expression;

	public ExpressionFlowStepGoal(ModelSite<T> expression) {
		this.expression = expression;
	}

	public Result<FlowPosition> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		return new ExpressionFlowStepGoalSolver<T>(expression, goalManager)
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
		ExpressionFlowStepGoal<?> other = (ExpressionFlowStepGoal<?>) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExpressionFlowStepGoal [expression=" + expression + "]";
	}

}

final class ExpressionFlowStepGoalSolver<T extends exprType> {

	private final RedundancyEliminator<FlowPosition> nextPositions = new RedundancyEliminator<FlowPosition>();

	public ExpressionFlowStepGoalSolver(ModelSite<T> expression,
			SubgoalManager goalManager) {

		Set<FlowSituation> situations = FlowSituationFinder
				.findFlowSituations(expression);

		for (FlowSituation flowSituation : situations) {
			nextPositions.add(flowSituation.nextFlowPositions(goalManager));
			if (nextPositions.isFinished())
				break;
		}
	}

	public Result<FlowPosition> solution() {
		return nextPositions.result();
	}

}
