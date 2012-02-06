package uk.ac.ic.doc.gander.flowinference;

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
	 * Does a call to the code object in question, insert an instance of another
	 * object as a positional argument dispatched to the receiving procedure?
	 */
	boolean passesHiddenSelf();

	/**
	 * The position at which an instance of another
	 * object is inserted and dispatched to the receiving procedure?
	 * 
	 * @throws {@link AssertionError} if {@code !passesHiddenSelf}.
	 */
	int selfPosition();

}
