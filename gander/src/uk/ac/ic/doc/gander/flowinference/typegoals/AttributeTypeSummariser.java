package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectNamespacePosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Typer collector for attribute accesses on a code object.
 * 
 * Builds a summarising type from the values that any of the given attributes
 * are defined with.
 */
final class AttributeTypeSummariser {

	private final RedundancyEliminator<Type> typeSummary = new RedundancyEliminator<Type>();
	private final SubgoalManager goalManager;
	private final String attributeName;

	AttributeTypeSummariser(CodeObject codeObject, String attributeName,
			SubgoalManager goalManager) {
		assert codeObject != null;
		assert !attributeName.isEmpty();
		assert goalManager != null;

		this.attributeName = attributeName;
		this.goalManager = goalManager;

		Result<ModelSite<? extends exprType>> namespaceReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectNamespacePosition(
						codeObject)));

		namespaceReferences.actOnResult(new ReferenceProcessor());

	}

	Result<Type> type() {
		return typeSummary.result();
	}

	private class ReferenceProcessor implements
			Processor<ModelSite<? extends exprType>> {

		public void processInfiniteResult() {
			/*
			 * We have no idea where the namespace flowed to so we can't say
			 * what type the attribute might have.
			 */
			typeSummary.add(TopT.INSTANCE);
		}

		public void processFiniteResult(
				Set<ModelSite<? extends exprType>> namespaceReferences) {

			/*
			 * Collect the expressions that access our named attribute on an
			 * object whose attribute accesses access our code object's
			 * namespace.
			 */

			/*
			 * XXX: We only look at attributes referenced using a matching name.
			 * is this enough? What about fields of modules, for instance (yes I
			 * realise these never get here because NamespaceNameTypeGoal
			 * handles them but they are technically objects).
			 */

			Set<ModelSite<Attribute>> attributes = new NamedAttributeAccessFinder(
					namespaceReferences, attributeName).accesses();

			new AttributeDefinitionFinder(attributes, new DefinitionTyper());
		}
	}

	private class DefinitionTyper implements AttributeDefinitionFinder.Event {
		public boolean attributeDefined(ModelSite<Attribute> attribute,
				ModelSite<exprType> value) {

			Result<Type> valueType = goalManager
					.registerSubgoal(new ExpressionTypeGoal(value));

			typeSummary.add(valueType);

			return typeSummary.isFinished();
		}
	}

}
