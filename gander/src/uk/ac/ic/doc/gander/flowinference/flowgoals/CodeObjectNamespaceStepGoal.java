package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
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
	private final Model model;

	CodeObjectNamespaceStepGoal(Namespace codeObject, Model model) {
		this.codeObject = codeObject;
		this.model = model;
	}

	public Set<FlowPosition> initialSolution() {
		return Collections.emptySet();
	}

	public Set<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		Set<FlowPosition> positions = new HashSet<FlowPosition>();

		positions.add(new CodeObjectPosition(codeObject, model));

		if (codeObject instanceof Class) {
			positions.add(new InstancePosition((Class) codeObject, model));
		}

		return positions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeObject == null) ? 0 : codeObject.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CodeObjectNamespaceStepGoal [codeObject=" + codeObject + "]";
	}

}
