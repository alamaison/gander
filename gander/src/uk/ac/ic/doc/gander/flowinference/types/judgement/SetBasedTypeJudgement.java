package uk.ac.ic.doc.gander.flowinference.types.judgement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.types.Type;

public class SetBasedTypeJudgement implements TypeJudgement {

	public static final SetBasedTypeJudgement BOTTOM = new SetBasedTypeJudgement(
			Collections.<Type> emptySet());

	private HashSet<Type> constituentTypes;

	public SetBasedTypeJudgement(Collection<? extends Type> constituentTypes) {
		assert !constituentTypes.contains(null);
		this.constituentTypes = new HashSet<Type>(constituentTypes);
	}

	public SetBasedTypeJudgement(Type constituentType) {
		this(Collections.singleton(constituentType));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constituentTypes == null) ? 0 : constituentTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SetBasedTypeJudgement) {
			SetBasedTypeJudgement other = (SetBasedTypeJudgement) obj;
			if (constituentTypes == null) {
				return other.constituentTypes == null;
			} else {
				return constituentTypes.equals(other.constituentTypes);
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "<Union of types: " + constituentTypes.toString() + ">";
	}

	public Set<Type> getConstituentTypes() {
		return Collections.unmodifiableSet(constituentTypes);
	}
}
