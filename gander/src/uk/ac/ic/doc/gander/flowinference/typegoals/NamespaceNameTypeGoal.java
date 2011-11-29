package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
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

	private static final class MemberFinder implements Processor<Type> {
		private final String name;
		private final Class klass;
		private final SubgoalManager goalManager;

		private Member member = null;

		private MemberFinder(String name, Class klass,
				SubgoalManager goalManager) {
			this.name = name;
			this.klass = klass;
			this.goalManager = goalManager;
		}

		public void processInfiniteResult() {
			// do nothing, no member found
		}

		public void processFiniteResult(Set<Type> result) {
			for (Type supertypeType : result) {
				if (supertypeType instanceof TClass) {
					Class superclass = ((TClass) supertypeType)
							.getClassInstance();

					// XXX: only fixes single-level recursion
					if (superclass.equals(klass))
						continue;

					member = superclass.lookupMember(name);
					if (member != null) {
						return;
					} else {
						// FIXME: This searches depth-first rather than
						// breadth first as it should be
						//
						// FIXME: Also leads to infinite recursion
						member = lookForMemberInInheritanceChain(superclass,
								name, goalManager);
						if (member != null) {
							return;
						}
					}

				}
			}
		}
	}

	private NamespaceName name;

	NamespaceNameTypeGoal(NamespaceName name) {
		if (name == null)
			throw new NullPointerException(
					"Can't find an name's type if we don't have a name");

		this.name = name;
	}

	public Result<Type> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<Type> recalculateSolution(SubgoalManager goalManager) {

		/*
		 * TODO: If we can't find a matching entry in the namespace type
		 * definition then we will have to do something more complex to deal
		 * with the possibility that it is a field. Or even worse, that it is a
		 * method added at runtime. For the moment we return TopT (don't know)
		 * in that case.
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

			RedundancyEliminator<Type> completeType = new RedundancyEliminator<Type>();

			completeType.add(new UnqualifiedNameDefinitionsPartialSolution(
					goalManager, name).partialSolution());
			completeType.add(new QualifiedNameDefinitionsPartialSolution(
					goalManager, name).partialSolution());

			return completeType.result();
		}
	}

	private static Member lookForMemberInInheritanceChain(final Class klass,
			final String name, final SubgoalManager goalManager) {

		for (exprType supertype : klass.inheritsFrom()) {

			Result<Type> supertypeTypes = goalManager
					.registerSubgoal(new ExpressionTypeGoal(
							new ModelSite<exprType>(supertype, klass
									.getParentScope().codeObject())));

			MemberFinder memberFinder = new MemberFinder(name, klass,
					goalManager);
			supertypeTypes.actOnResult(memberFinder);
			if (memberFinder.member != null)
				return memberFinder.member;

		}
		return null;
	}

	private static Result<Type> convertMemberToType(Member member) {
		if (member instanceof Module) {
			return new FiniteResult<Type>(Collections.singleton(new TModule(
			(Module) member)));
		} else if (member instanceof Class) {
			return new FiniteResult<Type>(Collections.singleton(new TClass(
			(Class) member)));
		} else if (member instanceof Function) {
			return new FiniteResult<Type>(Collections.singleton(new TFunction(
			(Function) member)));
		} else {
			return TopT.INSTANCE;
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
