package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassingStrategy;

/**
 * Model of the characteristics of passing arguments to a procedure using a
 * method-style mechanism.
 */
final class MethodStylePassingStrategy implements ArgumentPassingStrategy {

	@Override
	public int realPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("Argument positions start at 0");
		}
		return position + 1;
	}

	@Override
	public boolean passesHiddenSelf() {
		return true;
	}

	@Override
	public int selfPosition() {
		return 0;
	}
}