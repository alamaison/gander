package uk.ac.ic.doc.gander.flowinference.typegoals;

/**
 * Type judgement representing all types.
 * 
 * XXX: Does it make sense to use Python class 'object' here or is the being too
 * concrete?
 */
public enum Top implements TypeJudgement {
	INSTANCE;

	@Override
	public String toString() {
		return "⊤";
	}

}
