package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.ResultConcentrator;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Member;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;

final class NamespaceNameTypeGoal implements TypeGoal {

	private NamespaceName name;

	NamespaceNameTypeGoal(NamespaceName name) {
		if (name == null)
			throw new NullPointerException(
					"Can't find an name's type if we don't have a name");

		this.name = name;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {

		/*
		 * TODO: If we can't find a matching entry in the namespace type
		 * definition then we will have to do something more complex to deal
		 * with the possibility that it is a field. Or even worse, that it is a
		 * method added at runtime. For the moment we return Top (don't know) in
		 * that case.
		 */

		Member member = name.namespace().lookupMember(name.name());
		// HACK: we shouldn't have to do this here
		if (member == null && name.namespace() instanceof Class) {
			member = lookForMemberInInheritanceChain((Class) name.namespace(),
					name.name(), goalManager);
		}

		// FIXME: This should include *both* types of model members and vars
		if (member != null) {
			return convertMemberToType(member);
		} else {

			ResultConcentrator<Type> completeType = new ResultConcentrator<Type>();
			completeType.add(new UnqualifiedNameDefinitionsPartialSolution(
					goalManager, name).partialSolution());
			completeType.add(new QualifiedNameDefinitionsPartialSolution(
					goalManager, name).partialSolution());

			if (completeType.isTop())
				return Top.INSTANCE;
			else
				return new SetBasedTypeJudgement(completeType.result());
		}
	}

	private static Member lookForMemberInInheritanceChain(Class klass,
			String name, SubgoalManager goalManager) {
		for (exprType supertype : klass.inheritsFrom()) {
			TypeJudgement supertypeTypes = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(supertype, klass
									.getParentScope().codeObject())));
			if (supertypeTypes instanceof FiniteTypeJudgement) {
				for (Type supertypeType : (FiniteTypeJudgement) supertypeTypes) {
					if (supertypeType instanceof TClass) {
						Class superclass = ((TClass) supertypeType)
								.getClassInstance();

						// XXX: only fixes single-level recursion
						if (superclass.equals(klass))
							continue;

						Member member = superclass.lookupMember(name);
						if (member != null) {
							return member;
						} else {
							// FIXME: This searches depth-first rather than
							// breadth first as it should be
							//
							// FIXME: Also leads to infinite recursion
							member = lookForMemberInInheritanceChain(
									superclass, name, goalManager);
							if (member != null) {
								return member;
							}
						}

					}
				}
			}
		}
		return null;
	}

	private static TypeJudgement convertMemberToType(Member member) {
		if (member instanceof Module) {
			return new SetBasedTypeJudgement(Collections.singleton(new TModule(
					(Module) member)));
		} else if (member instanceof Class) {
			return new SetBasedTypeJudgement(Collections.singleton(new TClass(
					(Class) member)));
		} else if (member instanceof Function) {
			return new SetBasedTypeJudgement(Collections
					.singleton(new TFunction((Function) member)));
		} else {
			return Top.INSTANCE;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamespaceNameTypeGoal other = (NamespaceNameTypeGoal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NamespaceNameTypeGoal [name=" + name + "]";
	}

}