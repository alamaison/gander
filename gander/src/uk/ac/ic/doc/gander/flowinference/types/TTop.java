package uk.ac.ic.doc.gander.flowinference.types;

/**
 * Type representing all types.
 * 
 * XXX: Does it make sense to use class object here or is the being too
 * concrete?
 */
public class TTop implements Type {

	public String getName() {
		return  "⊤";
	}

	@Override
	public boolean equals(Object obj) {
		// Top is always equal to itself regardless of the exact instance
		return obj instanceof TTop;
	}

	@Override
	public int hashCode() {
		return 42;
	}

	@Override
	public String toString() {
		return getName();
	}

}
