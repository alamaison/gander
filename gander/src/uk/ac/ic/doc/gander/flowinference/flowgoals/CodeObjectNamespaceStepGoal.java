package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Goal solving the flow of namespaces from a code object.
 * 
 * Code objects and namespace do not have a one-to-one relationship. For example
 * class objects' namespace flow to the LHS of each attribute access on the
 * class object as well as the LHS of each attribute access on instances of the
 * class.
 */
final class CodeObjectNamespaceStepGoal implements FlowStepGoal {

	private final Namespace codeObject;

	CodeObjectNamespaceStepGoal(Namespace codeObject) {
		this.codeObject = codeObject;
	}

	public Result<FlowPosition> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		Set<FlowPosition> positions = new HashSet<FlowPosition>();

		positions.add(new CodeObjectPosition(codeObject));

		if (codeObject instanceof Class) {
			positions.add(new InstancePosition((Class) codeObject));
		}

		return new FiniteResult<FlowPosition>(positions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
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
		CodeObjectNamespaceStepGoal other = (CodeObjectNamespaceStepGoal) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CodeObjectNamespaceStepGoal [codeObject=" + codeObject + "]";
	}

}
