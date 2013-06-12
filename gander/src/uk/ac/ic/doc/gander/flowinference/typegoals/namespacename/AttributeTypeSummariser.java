package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.namespacename.AttributeDefinitionFinder.Event;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Typer collector for attribute accesses on a code object.
 * 
 * Builds a summarising type from the values that any of the given attributes
 * are defined with.
 */
final class AttributeTypeSummariser {

	private final RedundancyEliminator<PyObject> typeSummary = new RedundancyEliminator<PyObject>();
	private final SubgoalManager goalManager;
	private final String attributeName;

	AttributeTypeSummariser(Result<ModelSite<exprType>> namespaceReferences,
			String attributeName, SubgoalManager goalManager) {
		assert namespaceReferences != null;
		assert !attributeName.isEmpty();
		assert goalManager != null;

		this.attributeName = attributeName;
		this.goalManager = goalManager;

		namespaceReferences.actOnResult(new ReferenceProcessor());
	}

	Result<PyObject> type() {
		return typeSummary.result();
	}

	private class ReferenceProcessor implements Processor<ModelSite<exprType>> {

		public void processInfiniteResult() {
			/*
			 * We have no idea where the namespace flowed to so we can't say
			 * what type the attribute might have.
			 */
			typeSummary.add(TopT.INSTANCE);
		}

		public void processFiniteResult(
				Set<ModelSite<exprType>> namespaceReferences) {

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

			Result<PyObject> valueType = goalManager
					.registerSubgoal(new ExpressionTypeGoal(value));

			typeSummary.add(valueType);

			return typeSummary.isFinished();
		}
	}

}
