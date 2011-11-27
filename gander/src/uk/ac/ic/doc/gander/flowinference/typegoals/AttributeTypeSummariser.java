package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.ResultConcentrator;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Typer collector for a set of attribute.
 * 
 * Builds a summarising type from the values that any of the given attributes
 * are defined with.
 */
final class AttributeTypeSummariser {

	private final ResultConcentrator<Type> typeSummary = new ResultConcentrator<Type>();
	private final SubgoalManager goalManager;

	AttributeTypeSummariser(Set<ModelSite<Attribute>> attributes,
			SubgoalManager goalManager) {
		assert attributes != null;
		assert goalManager != null;

		this.goalManager = goalManager;
		new AttributeDefinitionFinder(attributes, new DefinitionTyper());
	}

	public Set<Type> type() {
		return typeSummary.result();
	}

	private class DefinitionTyper implements AttributeDefinitionFinder.Event {
		public boolean attributeDefined(ModelSite<Attribute> attribute,
				ModelSite<exprType> value) {

			TypeJudgement valueType = goalManager
					.registerSubgoal(new ExpressionTypeGoal(value));
			if (valueType instanceof SetBasedTypeJudgement) {
				typeSummary.add(((SetBasedTypeJudgement) valueType)
						.getConstituentTypes());
			} else {
				typeSummary.add(null);
			}

			return typeSummary.isTop();
		}
	}

}
