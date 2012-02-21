package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public interface Argument {

	/**
	 * Returns destination of this argument when passed to the given invokable
	 * object.
	 * 
	 * @param receiver
	 *            the invokable object receiving this argument
	 * @return model of passing this argument
	 */
	ArgumentDestination passArgumentAtCall(InvokableCodeObject receiver);

}
