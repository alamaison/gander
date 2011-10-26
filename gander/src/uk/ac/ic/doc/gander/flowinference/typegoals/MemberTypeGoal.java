package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Infer the type of a named member of another type.
 * 
 * The given type is the type who's namespace contains the member in question.
 * For example, if the type were a m the member would be anything bound to that
 * name in the module namespace. If the type were a class instance, the member
 * would be anything bound to a that name in the dictionary of any class
 * instances or the metaclass.
 */
final class MemberTypeGoal implements TypeGoal {

	private final Type type;
	private final Model model;
	private final String memberName;

	MemberTypeGoal(Model model, Type type, String memberName) {
		this.model = model;
		this.type = type;
		this.memberName = memberName;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		if (type instanceof TNamespace) {
			Namespace namespace = ((TNamespace) type).getNamespaceInstance();
			return goalManager.registerSubgoal(new NamespaceMemberTypeGoal(
					model, namespace, memberName));
		} else if (type instanceof TObject) {
			Class klass = ((TObject) type).getClassInstance();
			return goalManager.registerSubgoal(new ObjectMemberTypeGoal(model,
					klass, memberName));
		} else {
			return new Top();
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		MemberTypeGoal other = (MemberTypeGoal) obj;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MemberTypeGoal [memberName=" + memberName + ", type=" + type
				+ "]";
	}

}
