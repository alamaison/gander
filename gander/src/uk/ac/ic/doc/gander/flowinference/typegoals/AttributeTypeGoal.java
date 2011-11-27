package uk.ac.ic.doc.gander.flowinference.typegoals;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.ModelSite;

final class AttributeTypeGoal implements TypeGoal {

	private final ModelSite<Attribute> attribute;

	AttributeTypeGoal(ModelSite<Attribute> attribute) {
		this.attribute = attribute;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		ModelSite<exprType> lhs = new ModelSite<exprType>(
				attribute.astNode().value, attribute.codeObject());
		ExpressionTypeGoal typer = new ExpressionTypeGoal(lhs);
		TypeJudgement targetTypes = goalManager.registerSubgoal(typer);

		if (targetTypes instanceof SetBasedTypeJudgement) {
			TypeConcentrator types = new TypeConcentrator();
			String attributeName = ((NameTok) attribute.astNode().attr).id;

			for (Type targetType : ((SetBasedTypeJudgement) targetTypes)
					.getConstituentTypes()) {
				types.add(goalManager.registerSubgoal(new MemberTypeGoal(
						targetType, attributeName)));
				if (types.isFinished())
					break;
			}

			return types.getJudgement();
		} else {
			return Top.INSTANCE;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attribute == null) ? 0 : attribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeTypeGoal other = (AttributeTypeGoal) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeTypeGoal [attribute=" + attribute + "]";
	}

}
