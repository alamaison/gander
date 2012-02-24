package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.callsite.ArgumentPassingStrategy;

/**
 * Models the magical self argument passed in the case of calling a bound object
 * member.
 */
public final class SelfCallsiteArgument implements PositionalCallsiteArgument {

	@Override
	public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {

		if (argumentMapper.passesHiddenSelf()) {

			return argumentMapper.selfArgument();

		} else {

			/*
			 * If the passing strategy doesn't pass a self argument, it won't
			 * have any new flow positions.
			 */
			return NullArgument.INSTANCE;
		}
	}
}
