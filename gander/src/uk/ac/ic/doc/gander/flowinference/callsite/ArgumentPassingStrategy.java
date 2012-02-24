package uk.ac.ic.doc.gander.flowinference.callsite;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;

/**
 * Model of an argument being passed to a procedure.
 */
public interface ArgumentPassingStrategy {

	/**
	 * Returns the position that a positional argument at the callsite is
	 * received at by the receiving procedure.
	 */
	int realPosition(int position);

	/**
	 * Returns the position that a positional argument appears at at the
	 * callsite.
	 */
	int callsitePosition(int realPosition);

	/**
	 * Does a call to the code object in question, insert an instance of another
	 * object as a positional argument dispatched to the receiving procedure?
	 */
	boolean passesHiddenSelf();

	/**
	 * The argument containing an instance of another object which is inserted
	 * and dispatched to the receiving procedure
	 * 
	 * @throws {@link AssertionError} if {@code !passesHiddenSelf}.
	 */
	Argument selfArgument();

}
