package uk.ac.ic.doc.gander.flowinference.types.judgement;

/**
 * Type judgement representing all types.
 * 
 * XXX: Does it make sense to use Python class 'object' here or is the being too
 * concrete?
 */
public class Top implements TypeJudgement {
	
	public static final Top INSTANCE = new Top(); 

	public String getName() {
		return  "⊤";
	}

	@Override
	public boolean equals(Object obj) {
		// Top is always equal to itself regardless of the exact instance
		return obj instanceof Top;
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
