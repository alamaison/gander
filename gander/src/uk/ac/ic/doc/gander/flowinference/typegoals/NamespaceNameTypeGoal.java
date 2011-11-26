package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.HashSet;
import java.util.Set;

import javax.print.attribute.HashAttributeSet;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
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
			UnqualifiedNamePartialTypeGoalSolver typer = new UnqualifiedNamePartialTypeGoalSolver(
					goalManager, name);
			if (!(typer.solution() instanceof SetBasedTypeJudgement))
				return new Top();
			
			Set<Type> unqualifiedPart = ((SetBasedTypeJudgement)typer.solution()).getConstituentTypes();
			
			QualifiedNamePartialTypeGoalSolver typer2 = new QualifiedNamePartialTypeGoalSolver(
					goalManager, name);
			if (!(typer2.solution() instanceof SetBasedTypeJudgement))
				return new Top();
			
			Set<Type> qualifiedPart = ((SetBasedTypeJudgement)typer2.solution()).getConstituentTypes();
			
			Set<Type> completeType = new HashSet<Type>();
			completeType.addAll(unqualifiedPart);
			completeType.addAll(qualifiedPart);
			
			return new SetBasedTypeJudgement(completeType);
		}
	}

	private static Member lookForMemberInInheritanceChain(Class klass,
			String name, SubgoalManager goalManager) {
		for (exprType supertype : klass.inheritsFrom()) {
			TypeJudgement supertypeTypes = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(supertype, klass
									.getParentScope().codeObject())));
			if (supertypeTypes instanceof SetBasedTypeJudgement) {
				for (Type supertypeType : (((SetBasedTypeJudgement) supertypeTypes)
						.getConstituentTypes())) {
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
