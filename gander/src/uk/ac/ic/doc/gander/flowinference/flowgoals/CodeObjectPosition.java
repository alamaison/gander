package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Model of a code object's flow in a single execution step.
 * 
 * Code objects (currently namespaces) such as modules, classes and functions
 * are Python objects like any other. Their 'value' can flow into any named
 * reference in their enclosing code block as well as anywhere they are
 * explicitly imported.
 */
public final class CodeObjectPosition implements FlowPosition {

	private final Namespace codeObject;
	private final Model model;

	public CodeObjectPosition(Namespace codeObject, Model model) {
		this.codeObject = codeObject;
		this.model = model;
	}

	public FlowStepGoal nextStepGoal() {
		return new CodeObjectStepGoal(codeObject, model);
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
		CodeObjectPosition other = (CodeObjectPosition) obj;
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
		return "CodeObjectPosition [codeObject=" + codeObject + "]";
	}

}
