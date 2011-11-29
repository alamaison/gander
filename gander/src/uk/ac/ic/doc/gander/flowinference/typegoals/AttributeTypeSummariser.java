package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Typer collector for a set of attribute.
 * 
 * Builds a summarising type from the values that any of the given attributes
 * are defined with.
 */
final class AttributeTypeSummariser {

	private final RedundancyEliminator<Type> typeSummary = new RedundancyEliminator<Type>();
	private final SubgoalManager goalManager;

	AttributeTypeSummariser(Set<ModelSite<Attribute>> attributes,
			SubgoalManager goalManager) {
		assert attributes != null;
		assert goalManager != null;

		this.goalManager = goalManager;
		new AttributeDefinitionFinder(attributes, new DefinitionTyper());
	}

	public Result<Type> type() {
		return typeSummary.result();
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
