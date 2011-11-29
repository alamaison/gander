package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
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

	private final RedundancyEliminator<Type> inferredType = new RedundancyEliminator<Type>();
	private final SubgoalManager goalManager;

	public Result<Type> partialSolution() {
		return inferredType.result();
	}

	QualifiedNameDefinitionsPartialSolution(SubgoalManager goalManager,
			final NamespaceName name) {
		assert goalManager != null;
		assert name != null;

		this.goalManager = goalManager;

		Result<ModelSite<? extends exprType>> namespacePositions = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(name
						.namespace().codeObject())));
		namespacePositions
				.actOnResult(new Processor<ModelSite<? extends exprType>>() {

					public void processInfiniteResult() {
						inferredType.add(TopT.INSTANCE);
					}

					public void processFiniteResult(
							Set<ModelSite<? extends exprType>> namespacePositions) {

						Set<ModelSite<Attribute>> qualifiedReferences = new NamedAttributeAccessFinder(
								namespacePositions, name.name()).accesses();

						addBindingsReferences(qualifiedReferences);
					}
				});
	}

	private void addBindingsReferences(
			Set<ModelSite<Attribute>> qualifiedReferences) {

		inferredType.add(new AttributeTypeSummariser(qualifiedReferences,
				goalManager).type());

	}

}
