package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result;

/**
 * Part of a type goal's overall solution.
 */
interface PartialTypeSolution {

	Result<PyObject> partialSolution();
}
