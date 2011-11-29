package uk.ac.ic.doc.gander.flowinference.result;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FiniteResult<T> extends AbstractSet<T> implements Result<T> {

	private final Set<T> constituentResults; // unmodifiable

	public static <T> FiniteResult<T> bottom() {
		return new FiniteResult<T>(Collections.<T> emptySet());
	}

	public FiniteResult(Collection<? extends T> constituentResults) {
		assert constituentResults != null;
		assert !constituentResults.contains(null);

		this.constituentResults = Collections.unmodifiableSet(new HashSet<T>(
				constituentResults));
	}

	public void actOnResult(Result.Processor<T> action) {
		action.processFiniteResult(this);
	}

	public Iterator<T> iterator() {
		/*
		 * This will be read-only because constituentResults is.
		 */
		return constituentResults.iterator();
	}

	@Override
	public int size() {
		return constituentResults.size();
	}

	@Override
	public String toString() {
		return "FiniteResult[" + constituentResults + "]";
	}
}