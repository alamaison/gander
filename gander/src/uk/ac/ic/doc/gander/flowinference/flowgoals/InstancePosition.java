package uk.ac.ic.doc.gander.flowinference.flowgoals;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;

/**
 * Models a {@link Class} instance's flow in a single execution step.
 * 
 * A class instance can flow to an class constructor call in a single step.
 */
public final class InstancePosition implements FlowPosition {

	private final Class klass;
	private final Model model;

	public InstancePosition(Class klass, Model model) {
		this.klass = klass;
		this.model = model;
	}

	public FlowStepGoal nextStepGoal() {
		return new InstanceCreationStepGoal(klass, model);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
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
		InstancePosition other = (InstancePosition) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
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
		return "InstancePosition [klass=" + klass + "]";
	}

}
