package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;

/**
 * Model of the characteristics of passing arguments to a procedure using a
 * function-style mechanism.
 */
public final class FunctionStylePassingStrategy implements
		ArgumentPassingStrategy {

	@Override
	public int realPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("Argument positions start at 0");
		}
		return position;
	}

	@Override
	public int callsitePosition(int realPosition) {
		if (realPosition < 0) {
			throw new IllegalArgumentException("Argument positions start at 0");
		}
		return realPosition;
	}

	@Override
	public boolean passesHiddenSelf() {
		return false;
	}

	@Override
	public Argument selfArgument() {
		throw new AssertionError("Function-style passing doesn't "
				+ "pass a hidden 'self' argument");
	}
}