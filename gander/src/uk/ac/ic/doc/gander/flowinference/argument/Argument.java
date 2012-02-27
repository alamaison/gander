package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
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

	boolean isPassedAtPosition(int position);

	boolean isPassedByKeyword(String keyword);

	boolean mayExpandIntoPosition();

	boolean mayExpandIntoKeyword();

	/**
	 * FIXME: You can't really take the type of the argument, for example, what
	 * about when it is passed to a tuple parameter or it is an expanded
	 * iterable.
	 */
	Result<Type> type(SubgoalManager goalManager);

}
