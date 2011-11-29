package uk.ac.ic.doc.gander.flowinference.result;

import java.util.Set;

/**
 * Represents the locations an expression can flow to.
 */
public interface Result<D> {

	public interface Processor<I> {

		void processFiniteResult(Set<I> result);

		void processInfiniteResult();
	}

	/**
	 * Processing this result.
	 * 
	 * @param action
	 *            the processor that decides what to do with each datum in this
	 *            result and the infinite case
	 */
	void actOnResult(Processor<D> action);

}
