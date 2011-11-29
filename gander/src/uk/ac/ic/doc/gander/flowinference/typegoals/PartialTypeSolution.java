package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Part of a type goal's overall solution.
 */
interface PartialTypeSolution {

	Result<Type> partialSolution();
}
