package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

/**
 * Find any callsites that could call the given function.
 */
public class FunctionSendersGoal implements SendersGoal {
	private final FunctionCO callable;

	public FunctionSendersGoal(FunctionCO callable) {
		this.callable = callable;
	}

	public Result<ModelSite<Call>> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		return new FunctionSendersGoalSolver(callable, goalManager).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callable == null) ? 0 : callable.hashCode());
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
		FunctionSendersGoal other = (FunctionSendersGoal) obj;
		if (callable == null) {
			if (other.callable != null)
				return false;
		} else if (!callable.equals(other.callable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionSendersGoal [callable=" + callable + "]";
	}

}

final class FunctionSendersGoalSolver {

	private Result<ModelSite<Call>> callSites;

	public FunctionSendersGoalSolver(FunctionCO callable,
			SubgoalManager goalManager) {

		Result<ModelSite<? extends exprType>> positions = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(callable)));

		positions.actOnResult(new Processor<ModelSite<? extends exprType>>() {

			public void processInfiniteResult() {
				callSites = TopS.INSTANCE;
			}

			public void processFiniteResult(
					Set<ModelSite<? extends exprType>> positions) {
				Set<ModelSite<Call>> callSitePositions = new HashSet<ModelSite<Call>>();
				for (ModelSite<? extends exprType> expression : positions) {
					SimpleNode parent = AstParentNodeFinder
							.findParent(expression.astNode(), expression
									.codeObject().ast());
					if (parent instanceof Call) {
						callSitePositions.add(new ModelSite<Call>(
								(Call) parent, expression.codeObject()));
					}
				}
				callSites = new FiniteResult<ModelSite<Call>>(callSitePositions);
			}
		});

	}

	Result<ModelSite<Call>> solution() {
		return callSites;
	}

}
