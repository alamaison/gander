package uk.ac.ic.doc.gander.flowinference.argument;

/**
 * Models the magical self argument passed in the case of calling a bound object
 * member.
 */
public final class SelfCallsiteArgument implements PositionalCallsiteArgument {

	@Override
	public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {

		if (argumentMapper.passesHiddenSelf()) {

			return new SelfArgument(argumentMapper.selfPosition());

		} else {

			/*
			 * If the passing strategy doesn't pass a self argument, it won't
			 * have any new flow positions.
			 */
			return new NullArgument();
		}
	}

}
