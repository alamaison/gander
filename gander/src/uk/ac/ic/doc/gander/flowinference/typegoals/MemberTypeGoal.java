package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TCodeObject;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

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

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {

		CodeObject codeObject = null;
		if (type instanceof TUnresolvedImport) {
			/*
			 * The attribute references the contents of a module but the module
			 * couldn't be loaded.
			 */
			return TopT.INSTANCE;
		}

		if (type instanceof TCodeObject) {
			codeObject = ((TCodeObject) type).codeObject();
		} else if (type instanceof TObject) {
			codeObject = ((TObject) type).getClassInstance().codeObject();
		}

		assert codeObject != null;

		NamespaceName member = new NamespaceName(memberName, codeObject
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
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
