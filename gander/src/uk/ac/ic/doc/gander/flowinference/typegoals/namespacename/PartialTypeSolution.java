package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Part of a type goal's overall solution.
 */
interface PartialTypeSolution {

	Result<Type> partialSolution();
}
