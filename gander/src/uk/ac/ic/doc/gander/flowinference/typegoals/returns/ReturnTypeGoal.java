package uk.ac.ic.doc.gander.flowinference.typegoals.returns;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class ReturnTypeGoal implements TypeGoal {

	private final ModelSite<Call> callSite;

	public ReturnTypeGoal(ModelSite<Call> callSite) {
		this.callSite = callSite;
	}

	public Result<PyObject> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<PyObject> recalculateSolution(final SubgoalManager goalManager) {
		return new ReturnTypeGoalSolver(goalManager, callSite).solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
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
		ReturnTypeGoal other = (ReturnTypeGoal) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReturnTypeGoal [callSite=" + callSite + "]";
	}

}
