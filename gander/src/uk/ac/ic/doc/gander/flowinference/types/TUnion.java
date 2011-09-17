package uk.ac.ic.doc.gander.flowinference.types;

public class TUnion implements Type {

	private final Iterable<Type> constituentTypes;

	public TUnion(Iterable<Type> constituentTypes) {
		this.constituentTypes = constituentTypes;
	}

	public String getName() {
		return "<Union of types: " + constituentTypes.toString() + ">";
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
		if (obj instanceof TUnion) {
			TUnion other = (TUnion) obj;
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
		return getName();
	}
	
	
}
