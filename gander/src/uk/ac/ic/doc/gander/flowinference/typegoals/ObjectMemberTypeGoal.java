package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

/**
 * Infer the type of a member of an object's dictionary for an object that is an
 * instance of the given Class.
 */
final class ObjectMemberTypeGoal implements TypeGoal {

	private final ClassCO klass;
	private final String memberName;

	ObjectMemberTypeGoal(ClassCO klass, String memberName) {
		this.klass = klass;
		this.memberName = memberName;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	/**
	 * The type of an object's member is the union of the types assigned to that
	 * name. Because we don't track individual instances in this
	 * container-insensitive analysis, this is summarised to become the union of
	 * the types assigned to that member of any instance of the same class.
	 */
	public Result<Type> recalculateSolution(final SubgoalManager goalManager) {

		/*
		 * To decide on the type of the member we have to look at all
		 * assignments to an attribute whose target could be the given class or
		 * an instance thereof.
		 * 
		 * The type of our member is determined by the values assigned to an
		 * attribute of these instances with the same name so we filter them to
		 * find the places where they are subject to an attribute access which,
		 * itself, is bound to a value.
		 */

		RedundancyEliminator<Type> memberType = new RedundancyEliminator<Type>();
		memberType.add(new AttributeTypeSummariser(klass, memberName,
				goalManager).type());

		/*
		 * An object member may also refer to the member in the metaclass object
		 * so we have to add these types too.
		 */
		Result<Type> metaClassMemberTypes = goalManager
				.registerSubgoal(new NamespaceNameTypeGoal(new NamespaceName(
						memberName, klass.model().intrinsicNamespace(klass))));
		memberType.add(metaClassMemberTypes);

		return memberType.result();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
		result = prime * result
				+ ((memberName == null) ? 0 : memberName.hashCode());
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
		ObjectMemberTypeGoal other = (ObjectMemberTypeGoal) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ObjectMemberTypeGoal [memberName=" + memberName + ", klass="
				+ klass + "]";
	}

}
