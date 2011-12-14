package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 */
	Result<Type> returnType(SubgoalManager goalManager);

	/**
	 * Returns the offset by which passed arguments are shifted when passed to
	 * the formal parameters
	 */
	int passedArgumentOffset();
}
