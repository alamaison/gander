package uk.ac.ic.doc.gander.flowinference.abstractmachine;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfArgument;
import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;

/**
 * Model of the characteristics of passing arguments to a procedure using a
 * method-style mechanism.
 */
final class MethodStylePassingStrategy implements ArgumentPassingStrategy {

	private final PyInstance instance;

	MethodStylePassingStrategy(PyInstance instance) {
		if (instance == null)
			throw new NullPointerException(
					"Method calling requires an object instance");
		this.instance = instance;
	}

	@Override
	public int realPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("Argument positions start at 0");
		}
		return position + 1;
	}

	@Override
	public int callsitePosition(int realPosition) {
		if (realPosition < 1) {
			throw new IllegalArgumentException(
					"Real argument positions start at 1 because 'self' "
							+ "is at position 0");
		}
		return realPosition - 1;
	}

	@Override
	public boolean passesHiddenSelf() {
		return true;
	}

	@Override
	public Argument selfArgument() {
		return new SelfArgument(0, instance);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instance == null) ? 0 : instance.hashCode());
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
		MethodStylePassingStrategy other = (MethodStylePassingStrategy) obj;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodStylePassingStrategy [instance=" + instance + "]";
	}

}