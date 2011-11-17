package uk.ac.ic.doc.gander.flowinference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Concentrates set-based inference results that obey the convention that
 * {@code null} represents Top, the set of all results of that type.
 * 
 * @param T
 *            type of item in set-based result
 */
public final class ResultConcentrator<T> {

	private Set<T> result = new HashSet<T>();

	public void add(Set<T> partialResult) {

		if (isTop()) {
			return;
		} else if (partialResult == null) {
			result = null;
		} else {
			result.addAll(partialResult);

		}
	}

	public Set<T> result() {
		if (isTop())
			return null;
		else
			return Collections.unmodifiableSet(result);
	}

	public boolean isTop() {
		return result == null;
	}

	@Override
	public String toString() {
		return "ResultConcentrator [result=" + ((isTop()) ? "⊤" : result) + "]";
	}

}
