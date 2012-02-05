package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassingStrategy;

/**
 * Model of the characteristics of passing arguments to a procedure using a
 * function-style mechanism.
 */
final class FunctionStylePassingStrategy implements ArgumentPassingStrategy {

	@Override
	public int realPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("Argument positions start at 0");
		}
		return position;
	}

	@Override
	public boolean passesHiddenSelf() {
		return false;
	}

	@Override
	public int selfPosition() {
		throw new AssertionError("Function-style passing doesn't " +
				"pass a hidden 'self' argument");
	}
	
}