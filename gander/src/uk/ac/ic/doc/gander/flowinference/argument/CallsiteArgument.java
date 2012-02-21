package uk.ac.ic.doc.gander.flowinference.argument;

public interface CallsiteArgument {

	/**
	 * Returns the real argument passed to the receivers.
	 * 
	 * @param argumentMapper
	 *            the strategy that maps call-site arguments to actual arguments
	 */
	Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper);
}
