package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

/**
 * Models a {@link ClassCO} instance's flow in a single execution step.
 * 
 * A class instance can flow to an class constructor call in a single step.
 */
public final class InstanceCreationPosition implements FlowPosition {

	private final ClassCO klass;

	public InstanceCreationPosition(ClassCO klass) {
		this.klass = klass;
	}

	public FlowStepGoal nextStepGoal() {
		return new InstanceCreationStepGoal(klass);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
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
		InstanceCreationPosition other = (InstanceCreationPosition) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstanceCreationPosition [klass=" + klass + "]";
	}

}
