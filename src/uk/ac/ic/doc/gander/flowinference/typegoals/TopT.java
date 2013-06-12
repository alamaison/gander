package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Top;

/**
 * Result representing all types.
 * 
 * Otherwise known as Top, this is used when insufficient information was
 * available to infer the type and is used as a conservative estimate in those
 * cases.
 */
public final class TopT extends Top<PyObject> {

	public static final TopT INSTANCE = new TopT();

	@Override
	public String toString() {
		return "⊤t";
	}

	private TopT() {
	}

}
