package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

final class ReturnSituation implements FlowSituation {

	private final ModelSite<?> expression;

	/**
	 * This situation doesn't keep keeps a reference to the node as it has no
	 * effect on the destination of the flow. All return statements result in
	 * the same flow destination: the callers of this callable.
	 */
	ReturnSituation(ModelSite<?> expression) {
		this.expression = expression;
	}

	/**
	 * In a single step of execution, a return value can flow to any caller of
	 * the enclosing callable.
	 */
	public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
		return new ReturnSituationSolver(expression, goalManager).solution();
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
		ReturnSituation other = (ReturnSituation) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReturnSituation [expression=" + expression + "]";
	}

}

final class ReturnSituationSolver {

	private Result<FlowPosition> solution;

	ReturnSituationSolver(ModelSite<?> expression, SubgoalManager goalManager) {

		Result<ModelSite<Call>> callers = goalManager
				.registerSubgoal(new FunctionSendersGoal((FunctionCO) expression.codeObject()));

		callers.actOnResult(new Processor<ModelSite<Call>>() {

			public void processInfiniteResult() {
				solution = TopFp.INSTANCE;
			}

			public void processFiniteResult(Set<ModelSite<Call>> callers) {

				Set<FlowPosition> positions = new HashSet<FlowPosition>();

				for (ModelSite<Call> callSite : callers) {
					positions.add(new ExpressionPosition<Call>(callSite));
				}

				solution = new FiniteResult<FlowPosition>(positions);

			}
		});

	}

	public Result<FlowPosition> solution() {
		return solution;
	}

}
