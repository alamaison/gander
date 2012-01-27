package uk.ac.ic.doc.gander.flowinference;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

/**
 * Result representing the infinite set of all possible invokable code objects.
 * 
 * Otherwise known as Top, this is used when our analysis is unable to determine
 * which code is invoked in a particular situation.
 */
public final class TopI extends Top<InvokableCodeObject> {

	public static final TopI INSTANCE = new TopI();

	@Override
	public String toString() {
		return "⊤i";
	}

	private TopI() {
	}

}
