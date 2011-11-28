package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.types.Type;

public class SetBasedTypeJudgement extends AbstractSet<Type> implements
		FiniteTypeJudgement {

	public static final FiniteTypeJudgement BOTTOM = new SetBasedTypeJudgement(
			Collections.<Type> emptySet());

	private final Set<Type> constituentTypes;

	public SetBasedTypeJudgement(Collection<? extends Type> constituentTypes) {
		assert constituentTypes != null;
		assert !constituentTypes.contains(null);
		
		this.constituentTypes = Collections.unmodifiableSet(new HashSet<Type>(
				constituentTypes));
	}

	@Override
	public String toString() {
		return "<Union of types: " + constituentTypes.toString() + ">";
	}

	public Iterator<Type> iterator() {
		/*
		 * This will be read-only because constituentTypes is.
		 */
		return constituentTypes.iterator();
	}

	@Override
	public int size() {
		return constituentTypes.size();
	}
}
