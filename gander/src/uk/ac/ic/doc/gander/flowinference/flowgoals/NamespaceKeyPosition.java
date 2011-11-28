package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * A flow position where the value has been bound to a name in a namespace.
 */
public final class NamespaceKeyPosition implements FlowPosition {
	private final Variable nameBinding;

	public NamespaceKeyPosition(Variable nameBinding) {
		if (nameBinding == null)
			throw new NullPointerException("A namespace position without a "
					+ "key doesn't make sense");
		this.nameBinding = nameBinding;
	}

	public FlowStepGoal nextStepGoal() {
		return new NamespaceKeyFlowStepGoal(nameBinding);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nameBinding == null) ? 0 : nameBinding.hashCode());
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
		NamespaceKeyPosition other = (NamespaceKeyPosition) obj;
		if (nameBinding == null) {
			if (other.nameBinding != null)
				return false;
		} else if (!nameBinding.equals(other.nameBinding))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceKeyPosition [nameBinding=" + nameBinding + "]";
	}

}
