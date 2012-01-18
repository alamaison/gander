package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;

/**
 * Infers the part of the type of a namespace name that comes from qualified
 * references to the name.
 * 
 * In other words, the part of the types that is implied by bindings to the name
 * via attribute access on an object.
 * 
 * This is not the complete type of the name as it doesn't include values bound
 * to the name by unqualified reference.
 */
final class QualifiedNameDefinitionsPartialSolution implements
		PartialTypeSolution {

	private final Result<Type> inferredType;

	public Result<Type> partialSolution() {
		return inferredType;
	}

	QualifiedNameDefinitionsPartialSolution(SubgoalManager goalManager,
			final NamespaceName name) {
		assert goalManager != null;
		assert name != null;

		/*
		 * FIXME: The AttributeTypeSummariser may end up using other namespaces
		 * that differ in their external accessibility
		 */

		Result<ModelSite<? extends exprType>> namespaceReferences = name
				.namespace().references(goalManager);

		inferredType = new AttributeTypeSummariser(namespaceReferences, name
				.name(), goalManager).type();
	}

}
