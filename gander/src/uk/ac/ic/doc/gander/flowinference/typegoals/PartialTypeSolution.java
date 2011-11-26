package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.types.Type;

/**
 * Part of a type goal's overall solution.
 */
interface PartialTypeSolution {

	Set<Type> partialSolution();
}
