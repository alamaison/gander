package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

final class NamespaceMemberTypeGoal implements TypeGoal {

	private final Namespace namespace;
	private final Model model;
	private final String attributeName;

	NamespaceMemberTypeGoal(Model model, Namespace namespace,
			String attributeName) {
		this.model = model;
		this.namespace = namespace;
		this.attributeName = attributeName;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		if (namespace != null) {

			/*
			 * TODO: If we can't find a matching entry in the namespace type
			 * definition then we will have to do something more complex to deal
			 * with the possibility that it is a field. Or even worse, that it
			 * is a method added at runtime. For the moment we return Top (don't
			 * know) in that case.
			 */
			return extractTokenTypeFromNamespace(model, namespace,
					attributeName, goalManager);
		} else {

			/*
			 * If we get here it means the attribute references the contents of
			 * a module but the module couldn't be loaded.
			 */
			return new Top();
		}
	}

	private static TypeJudgement extractTokenTypeFromNamespace(Model model,
			Namespace scope, String name, SubgoalManager goalManager) {
		Member member = scope.lookupMember(name);
		if (member != null) {
			return convertMemberToType(member);
		} else {
			NameTypeGoal typer = new NameTypeGoal(model, scope, name);
			return goalManager.registerSubgoal(typer);
		}
	}

	private static TypeJudgement convertMemberToType(Member member) {
		if (member instanceof Module) {
			return new SetBasedTypeJudgement(new TModule((Module) member));
		} else if (member instanceof Class) {
			return new SetBasedTypeJudgement(new TClass((Class) member));
		} else if (member instanceof Function) {
			return new SetBasedTypeJudgement(new TFunction((Function) member));
		} else {
			return new Top();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		NamespaceMemberTypeGoal other = (NamespaceMemberTypeGoal) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceMemberTypeGoal [attributeName=" + attributeName
				+ ", namespace=" + namespace + "]";
	}

}
