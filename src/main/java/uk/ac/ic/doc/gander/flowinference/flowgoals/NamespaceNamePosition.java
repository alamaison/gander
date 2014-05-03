package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.NamespaceName;

/**
 * A flow position where the value has been bound to a name in a namespace.
 */
public final class NamespaceNamePosition implements FlowPosition {

	private final NamespaceName name;

	public NamespaceNamePosition(NamespaceName name) {
		if (name == null)
			throw new NullPointerException("A namespace name is not optional");
		this.name = name;
	}

	public FlowStepGoal nextStepGoal() {
		return new NamespaceNameFlowStepGoal(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamespaceNamePosition other = (NamespaceNamePosition) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceNamePosition [name=" + name + "]";
	}

}
