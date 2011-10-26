package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

final class AttributeTypeGoal implements TypeGoal {

	private final Model model;
	private final Namespace scope;
	private final Attribute attribute;

	AttributeTypeGoal(Model model, Namespace scope, Attribute attribute) {
		this.model = model;
		this.scope = scope;
		this.attribute = attribute;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		ExpressionTypeGoal typer = new ExpressionTypeGoal(model, scope,
				attribute.value);
		TypeJudgement targetTypes = goalManager.registerSubgoal(typer);
		
		if (targetTypes instanceof SetBasedTypeJudgement) {
			TypeConcentrator types = new TypeConcentrator();
			String attributeName = ((NameTok) attribute.attr).id;
			
			for (Type targetType : ((SetBasedTypeJudgement) targetTypes)
					.getConstituentTypes()) {
				types.add(goalManager.registerSubgoal(new MemberTypeGoal(model,
						targetType, attributeName)));
				if (types.isFinished())
					break;
			}

			return types.getJudgement();
		} else {
			return new Top();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AttributeTypeGoal))
			return false;
		AttributeTypeGoal other = (AttributeTypeGoal) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeTypeGoal [attribute=" + attribute + ", scope=" + scope
				+ "]";
	}

}
