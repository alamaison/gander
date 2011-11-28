package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TNamespace;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;

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
	private final String memberName;

	MemberTypeGoal(Type type, String memberName) {
		this.type = type;
		this.memberName = memberName;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		if (type instanceof TNamespace) {
			Namespace namespace = ((TNamespace) type).getNamespaceInstance();
			if (namespace == null) {
				/*
				 * If we get here it means the attribute references the contents
				 * of a module but the module couldn't be loaded.
				 */
				return Top.INSTANCE;
			} else {
				return goalManager.registerSubgoal(new NamespaceNameTypeGoal(
						new NamespaceName(memberName, namespace)));
			}
		} else if (type instanceof TObject) {
			Class klass = ((TObject) type).getClassInstance();
			return goalManager.registerSubgoal(new ObjectMemberTypeGoal(klass,
					memberName));
		} else {
			return Top.INSTANCE;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
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
