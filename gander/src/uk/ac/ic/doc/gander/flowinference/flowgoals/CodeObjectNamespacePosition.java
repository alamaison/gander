package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Models the flow of namespaces from a code object.
 * 
 * Code objects and namespace do not have a one-to-one relationship. For example
 * class objects' namespace flow to the LHS of each attribute access on the
 * class object as well as the LHS of each attribute access on instances of the
 * class.
 */
public final class CodeObjectNamespacePosition implements FlowPosition {

	private final CodeObject codeObject;

	public CodeObjectNamespacePosition(CodeObject codeObject) {
		this.codeObject = codeObject;
	}

	public FlowStepGoal nextStepGoal() {
		return new CodeObjectNamespaceStepGoal(codeObject);
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
		CodeObjectNamespacePosition other = (CodeObjectNamespacePosition) obj;
		if (codeObject == null) {
			if (other.codeObject != null)
				return false;
		} else if (!codeObject.equals(other.codeObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CodeObjectNamespacePosition [codeObject=" + codeObject + "]";
	}

}
