package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Result representing all types.
 * 
 * Otherwise known as Top, this is used when insufficient information was
 * available to infer the type and is used as a conservative estimate in those
 * cases.
 */
public final class TopT extends Top<Type> {

	public static final TopT INSTANCE = new TopT();

	@Override
	public String toString() {
		return "⊤t";
	}

	private TopT() {
	}

}
