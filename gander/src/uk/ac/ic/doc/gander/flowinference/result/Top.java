package uk.ac.ic.doc.gander.flowinference.result;

/**
 * Result representing all possible results for the domain.
 * 
 * Otherwise known as TopT, this is model the infinite set of results; as result
 * whose data cannot be accessed by iteration.
 * 
 * @param T
 *            domain of results
 */
public abstract class Top<T> implements Result<T> {

	public final void actOnResult(Result.Processor<T> action) {
		action.processInfiniteResult();
	}

	@Override
	public abstract String toString();

}
