package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfArgument;
import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;

/**
 * Model of the characteristics of passing arguments to a procedure using a
 * method-style mechanism.
 */
final class MethodStylePassingStrategy implements ArgumentPassingStrategy {

	private final TObject instance;

	MethodStylePassingStrategy(TObject instance) {
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
}